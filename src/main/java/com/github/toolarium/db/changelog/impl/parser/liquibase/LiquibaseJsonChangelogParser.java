/*
 * LiquibaseJsonChangelogParser.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog.impl.parser.liquibase;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Parses external JSON changelog format (databaseChangeLog root key).
 *
 * @author patrick
 */
public class LiquibaseJsonChangelogParser implements IChangelogParser {
    private static final Logger LOG = LoggerFactory.getLogger(LiquibaseJsonChangelogParser.class);
    private static final String KEY_SQL = "sql";
    private final ObjectMapper objectMapper = new ObjectMapper();


    /**
     * @see com.github.toolarium.db.changelog.impl.parser.IChangelogParser#parse(InputStream, String, IChangelogParameterResolver)
     */
    @Override
    public IChangelog parse(InputStream input, String physicalPath, IChangelogParameterResolver parameterResolver) throws ChangelogParseException {
        try {
            JsonNode root = objectMapper.readTree(input);
            JsonNode changeLogNode = root.get("databaseChangeLog");

            if (changeLogNode == null || !changeLogNode.isArray()) {
                throw new ChangelogParseException("Not a valid external JSON changelog [" + physicalPath + "].");
            }

            List<IChangelogChangeSet> changeSets = new ArrayList<>();
            for (JsonNode entry : changeLogNode) {
                JsonNode csNode = entry.get("changeSet");
                if (csNode != null) {
                    changeSets.add(parseChangeSet(csNode, physicalPath, parameterResolver));
                }
            }

            if (LOG.isDebugEnabled()) {
                LOG.debug("Parsed " + changeSets.size() + " changeset(s) from external JSON [" + physicalPath + "].");
            }

            return new Changelog(physicalPath, changeSets);
        } catch (ChangelogParseException e) {
            throw e;
        } catch (Exception e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Failed to parse external JSON changelog: " + e.getMessage(), e);
            }
            throw new ChangelogParseException("Failed to parse external JSON changelog [" + physicalPath + "].", e);
        }
    }


    /**
     * @see com.github.toolarium.db.changelog.impl.parser.IChangelogParser#supports(String)
     */
    @Override
    public boolean supports(String fileExtension) {
        return ".json".equals(fileExtension);
    }


    /**
     * Parse a single changeset from an external JSON node.
     *
     * @param csNode the changeset JSON node
     * @param physicalPath the file path
     * @param parameterResolver the parameter resolver
     * @return the parsed changeset
     */
    private IChangelogChangeSet parseChangeSet(JsonNode csNode, String physicalPath,
                                               IChangelogParameterResolver parameterResolver) {
        String id = "unknown";
        if (csNode.has("id")) {
            id = csNode.get("id").asText();
        }
        String author = System.getProperty("user.name", "default");
        if (csNode.has("author")) {
            author = csNode.get("author").asText();
        }

        ChangelogChangeSetIdentity identity = new ChangelogChangeSetIdentity(id, author, physicalPath);
        List<IChangelogEntry> entries = new ArrayList<>();

        JsonNode changesNode = csNode.get("changes");
        if (changesNode != null && changesNode.isArray()) {
            for (JsonNode change : changesNode) {
                extractSqlEntry(change, parameterResolver, entries);
            }
        }

        List<IChangelogEntry> rollbackEntries = Collections.emptyList();
        JsonNode rollbackNode = csNode.get("rollback");
        if (rollbackNode != null && rollbackNode.isArray()) {
            rollbackEntries = new ArrayList<>();
            for (JsonNode rb : rollbackNode) {
                extractSqlEntry(rb, parameterResolver, rollbackEntries);
            }
        }

        return new ChangelogChangeSet(identity, entries, rollbackEntries, null,
                false, false, true, true, Collections.emptyList(), Collections.emptyList(), null);
    }


    /**
     * Extract a SQL entry from a JSON change node and add it to the list.
     *
     * @param changeNode the change JSON node
     * @param parameterResolver the parameter resolver
     * @param entries the list to add to
     */
    private void extractSqlEntry(JsonNode changeNode, IChangelogParameterResolver parameterResolver,
                                 List<IChangelogEntry> entries) {
        JsonNode sqlNode = changeNode.get(KEY_SQL);
        if (sqlNode == null) {
            return;
        }
        String sql;
        if (sqlNode.has(KEY_SQL)) {
            sql = sqlNode.get(KEY_SQL).asText();
        } else {
            sql = sqlNode.asText();
        }
        entries.add(new SqlChangelogEntry(parameterResolver.resolve(sql)));
    }
}
