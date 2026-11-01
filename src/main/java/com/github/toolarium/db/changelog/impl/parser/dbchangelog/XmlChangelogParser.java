/*
 * XmlChangelogParser.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog.impl.parser.dbchangelog;


import com.github.toolarium.db.changelog.dto.Changelog;
import com.github.toolarium.db.changelog.dto.ChangelogChangeSet;
import com.github.toolarium.db.changelog.dto.ChangelogChangeSetIdentity;
import com.github.toolarium.db.changelog.dto.IChangelog;
import com.github.toolarium.db.changelog.dto.IChangelogChangeSet;
import com.github.toolarium.db.changelog.dto.IChangelogEntry;
import com.github.toolarium.db.changelog.dto.SqlChangelogEntry;
import com.github.toolarium.db.changelog.exception.ChangelogParseException;
import com.github.toolarium.db.changelog.impl.parser.IChangelogParser;
import com.github.toolarium.db.changelog.impl.resolver.IChangelogParameterResolver;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Parses the native DB-Changelog XML format (.db.xml) using StAX.
 *
 * @author patrick
 */
public class XmlChangelogParser implements IChangelogParser {
    private static final Logger LOG = LoggerFactory.getLogger(XmlChangelogParser.class);
    private static final String FORMAT_PREFIX = "toolarium-db-changelog/";
    private static final String ELEMENT_DB_CHANGELOG = "dbChangelog";
    private static final String ELEMENT_CHANGE_SET = "changeSet";
    private static final String ELEMENT_PROPERTY = "property";
    private static final String ELEMENT_SQL = "sql";
    private static final String ELEMENT_ROLLBACK = "rollback";


    /**
     * @see com.github.toolarium.db.changelog.impl.parser.IChangelogParser#parse(InputStream, String, IChangelogParameterResolver)
     */
    @Override
    public IChangelog parse(InputStream input, String physicalPath, IChangelogParameterResolver parameterResolver) throws ChangelogParseException {
        try {
            XMLInputFactory factory = XMLInputFactory.newInstance();
            factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE);
            factory.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.FALSE);
            XMLStreamReader reader = factory.createXMLStreamReader(input);

            List<IChangelogChangeSet> changeSets = new ArrayList<>();
            boolean formatValidated = false;

            while (reader.hasNext()) {
                int event = reader.next();
                if (event == XMLStreamConstants.START_ELEMENT) {
                    String name = reader.getLocalName();

                    if (ELEMENT_DB_CHANGELOG.equals(name)) {
                        String format = reader.getAttributeValue(null, "format");
                        validateFormat(format, physicalPath);
                        formatValidated = true;
                    } else if (ELEMENT_PROPERTY.equals(name) && formatValidated) {
                        String propName = reader.getAttributeValue(null, "name");
                        String propValue = reader.getAttributeValue(null, "value");
                        if (propName != null && propValue != null) {
                            parameterResolver.setProperty(propName, propValue);
                        }
                    } else if (ELEMENT_CHANGE_SET.equals(name) && formatValidated) {
                        changeSets.add(parseChangeSet(reader, physicalPath, parameterResolver));
                    }
                }
            }

            if (!formatValidated) {
                throw new ChangelogParseException("Missing <dbChangelog> root element in [" + physicalPath + "].");
            }

            if (LOG.isDebugEnabled()) {
                LOG.debug("Parsed " + changeSets.size() + " changeset(s) from [" + physicalPath + "].");
            }

            return new Changelog(physicalPath, changeSets);
        } catch (ChangelogParseException e) {
            throw e;
        } catch (Exception e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Failed to parse XML changelog [" + e.getMessage(), e);
            }
            throw new ChangelogParseException("Failed to parse XML changelog [" + physicalPath + "].", e);
        }
    }


    /**
     * @see com.github.toolarium.db.changelog.impl.parser.IChangelogParser#supports(String)
     */
    @Override
    public boolean supports(String fileExtension) {
        return ".db.xml".equals(fileExtension);
    }


    /**
     * Validate the format attribute.
     *
     * @param format the format string
     * @param physicalPath the file path for error messages
     * @throws ChangelogParseException if the format is invalid
     */
    private void validateFormat(String format, String physicalPath) throws ChangelogParseException {
        if (format == null || !format.startsWith(FORMAT_PREFIX)) {
            throw new ChangelogParseException("Invalid or missing format attribute on <dbChangelog> in [" + physicalPath + "].");
        }
    }


    /**
     * Parse a changeSet element.
     *
     * @param reader the XML stream reader positioned at the changeSet start element
     * @param physicalPath the file path
     * @param parameterResolver the parameter resolver
     * @return the parsed changeset
     * @throws ChangelogParseException if a required attribute is missing
     * @throws Exception if parsing fails
     */
    private IChangelogChangeSet parseChangeSet(XMLStreamReader reader, String physicalPath,
                                               IChangelogParameterResolver parameterResolver) throws Exception {
        Map<String, String> attrs = readChangeSetAttributes(reader, physicalPath);
        ChangelogChangeSetIdentity identity = new ChangelogChangeSetIdentity(
                attrs.get("id"), attrs.get("author"), physicalPath);

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

        return new ChangelogChangeSet(identity, entries, rollbackEntries, attrs.get("comment"),
                Boolean.parseBoolean(attrs.getOrDefault("runAlways", "false")),
                Boolean.parseBoolean(attrs.getOrDefault("runOnChange", "false")),
                !"false".equals(attrs.getOrDefault("failOnError", "true")),
                !"false".equals(attrs.getOrDefault("runInTransaction", "true")),
                parseCommaSeparated(attrs.get("context")), Collections.emptyList());
    }


    /**
     * Read and validate changeSet attributes from the current XML element.
     *
     * @param reader the XML stream reader
     * @param physicalPath the file path for error messages
     * @return the attributes map
     * @throws ChangelogParseException if id is missing
     */
    private Map<String, String> readChangeSetAttributes(XMLStreamReader reader, String physicalPath) throws ChangelogParseException {
        Map<String, String> attrs = new LinkedHashMap<>();
        String[] attrNames = {"id", "author", "comment", "context", "runAlways", "runOnChange", "failOnError", "runInTransaction"};
        for (String attrName : attrNames) {
            String value = reader.getAttributeValue(null, attrName);
            if (value != null) {
                attrs.put(attrName, value);
            }
        }

        if (!attrs.containsKey("id")) {
            throw new ChangelogParseException("Missing 'id' attribute on <changeSet> in [" + physicalPath + "].");
        }
        if (!attrs.containsKey("author")) {
            attrs.put("author", System.getProperty("user.name", "default"));
        }

        return attrs;
    }


    /**
     * Parse a comma-separated string into a list.
     *
     * @param value the comma-separated string, or null
     * @return the list of trimmed values, or empty list
     */
    private List<String> parseCommaSeparated(String value) {
        if (value == null || value.trim().isEmpty()) {
            return Collections.emptyList();
        }

        List<String> result = new ArrayList<>();
        for (String part : value.split(",")) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                result.add(trimmed);
            }
        }
        return result;
    }
}
