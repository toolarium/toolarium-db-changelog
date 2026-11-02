/*
 * LiquibaseXmlChangelogParser.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog.impl.parser.liquibase;

import com.github.toolarium.db.changelog.exception.ChangelogParseException;
import com.github.toolarium.db.changelog.impl.parser.IChangelogParser;
import com.github.toolarium.db.changelog.impl.resolver.IChangelogParameterResolver;
import com.github.toolarium.db.changelog.model.Changelog;
import com.github.toolarium.db.changelog.model.ChangelogChangeSet;
import com.github.toolarium.db.changelog.model.ChangelogChangeSetIdentity;
import com.github.toolarium.db.changelog.model.IChangelog;
import com.github.toolarium.db.changelog.model.IChangelogChangeSet;
import com.github.toolarium.db.changelog.model.IChangelogEntry;
import com.github.toolarium.db.changelog.model.sql.SqlChangelogEntry;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.xml.XMLConstants;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Parses external XML changelog format (databaseChangeLog namespace).
 *
 * @author patrick
 */
public class LiquibaseXmlChangelogParser implements IChangelogParser {
    private static final Logger LOG = LoggerFactory.getLogger(LiquibaseXmlChangelogParser.class);
    private static final String ELEMENT_CHANGE_SET = "changeSet";
    private static final String ELEMENT_SQL = "sql";
    private static final String ELEMENT_ROLLBACK = "rollback";


    /**
     * @see com.github.toolarium.db.changelog.impl.parser.IChangelogParser#parse(InputStream, String, IChangelogParameterResolver)
     */
    @Override
    public IChangelog parse(InputStream input, String physicalPath, IChangelogParameterResolver parameterResolver) throws ChangelogParseException {
        XMLStreamReader reader = null;
        try {
            XMLInputFactory factory = XMLInputFactory.newInstance();
            factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE);
            factory.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.FALSE);
            factory.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, Boolean.FALSE);
            factory.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            factory.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
            reader = factory.createXMLStreamReader(input);

            List<IChangelogChangeSet> changeSets = new ArrayList<>();

            while (reader.hasNext()) {
                int event = reader.next();
                if (event == XMLStreamConstants.START_ELEMENT) {
                    if (ELEMENT_CHANGE_SET.equals(reader.getLocalName())) {
                        changeSets.add(parseChangeSet(reader, physicalPath, parameterResolver));
                    }
                }
            }

            if (LOG.isDebugEnabled()) {
                LOG.debug("Parsed " + changeSets.size() + " changeset(s) from external XML [" + physicalPath + "].");
            }

            return new Changelog(physicalPath, changeSets);
        } catch (ChangelogParseException e) {
            throw e;
        } catch (Exception e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Failed to parse external XML changelog: " + e.getMessage(), e);
            }
            throw new ChangelogParseException("Failed to parse external XML changelog [" + physicalPath + "].", e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception e) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Failed to close XML stream reader: " + e.getMessage(), e);
                    }
                }
            }
        }
    }


    /**
     * @see com.github.toolarium.db.changelog.impl.parser.IChangelogParser#supports(String)
     */
    @Override
    public boolean supports(String fileExtension) {
        return ".xml".equals(fileExtension);
    }


    /**
     * Parse a changeSet element from external XML format.
     *
     * @param reader the XML stream reader positioned at the changeSet element
     * @param physicalPath the file path
     * @param parameterResolver the parameter resolver
     * @return the parsed changeset
     * @throws Exception if parsing fails
     */
    private IChangelogChangeSet parseChangeSet(XMLStreamReader reader, String physicalPath,
                                               IChangelogParameterResolver parameterResolver) throws Exception {
        String id = reader.getAttributeValue(null, "id");
        String author = reader.getAttributeValue(null, "author");
        if (id == null) {
            id = "unknown";
        }
        if (author == null) {
            author = System.getProperty("user.name", "default");
        }

        ChangelogChangeSetIdentity identity = new ChangelogChangeSetIdentity(id, author, physicalPath);
        List<IChangelogEntry> entries = new ArrayList<>();
        List<IChangelogEntry> rollbackEntries = new ArrayList<>();
        boolean inRollback = false;

        while (reader.hasNext()) {
            int event = reader.next();
            if (event == XMLStreamConstants.START_ELEMENT) {
                String name = reader.getLocalName();
                if (ELEMENT_ROLLBACK.equals(name)) {
                    inRollback = true;
                } else if (ELEMENT_SQL.equals(name)) {
                    String sql = parameterResolver.resolve(reader.getElementText().trim());
                    if (inRollback) {
                        rollbackEntries.add(new SqlChangelogEntry(sql));
                    } else {
                        entries.add(new SqlChangelogEntry(sql));
                    }
                }
            } else if (event == XMLStreamConstants.END_ELEMENT) {
                String name = reader.getLocalName();
                if (ELEMENT_ROLLBACK.equals(name)) {
                    inRollback = false;
                } else if (ELEMENT_CHANGE_SET.equals(name)) {
                    break;
                }
            }
        }

        return new ChangelogChangeSet(identity, entries, rollbackEntries, null,
                false, false, true, true, Collections.emptyList(), Collections.emptyList(), null);
    }
}
