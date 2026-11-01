/*
 * LiquibaseSqlChangelogParser.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog.impl.parser.liquibase;


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
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Parses the external formatted SQL changelog format.
 * Recognizes --changeset author:id directives and --rollback comments.
 *
 * @author patrick
 */
public class LiquibaseSqlChangelogParser implements IChangelogParser {
    private static final Logger LOG = LoggerFactory.getLogger(LiquibaseSqlChangelogParser.class);
    private static final String CHANGESET_PREFIX = "--changeset ";
    private static final String ROLLBACK_PREFIX = "--rollback ";
    private static final String FORMATTED_SQL_HEADER = "--liquibase formatted sql";


    /**
     * @see com.github.toolarium.db.changelog.impl.parser.IChangelogParser#parse(InputStream, String, IChangelogParameterResolver)
     */
    @Override
    public IChangelog parse(InputStream input, String physicalPath, IChangelogParameterResolver parameterResolver) throws ChangelogParseException {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8));
            List<IChangelogChangeSet> changeSets = new ArrayList<>();

            String currentId = null;
            String currentAuthor = null;
            StringBuilder sqlBuilder = new StringBuilder();
            StringBuilder rollbackBuilder = new StringBuilder();
            boolean inRollback = false;

            String line;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();

                if (trimmed.toLowerCase().startsWith(CHANGESET_PREFIX) || trimmed.startsWith(CHANGESET_PREFIX)) {
                    // Save previous changeset if any
                    if (currentId != null) {
                        changeSets.add(buildChangeSet(currentId, currentAuthor, sqlBuilder, rollbackBuilder,
                                physicalPath, parameterResolver));
                    }

                    // Parse new changeset directive: --changeset author:id
                    String directive = trimmed.substring(CHANGESET_PREFIX.length()).trim();
                    int colonIndex = directive.indexOf(':');
                    if (colonIndex <= 0) {
                        throw new ChangelogParseException("Invalid changeset directive [" + trimmed + "] in [" + physicalPath + "].");
                    }
                    currentAuthor = directive.substring(0, colonIndex).trim();
                    currentId = directive.substring(colonIndex + 1).trim();

                    // Strip inline attributes (e.g., context:test, runAlways:true)
                    int spaceIndex = currentId.indexOf(' ');
                    if (spaceIndex > 0) {
                        currentId = currentId.substring(0, spaceIndex);
                    }

                    sqlBuilder = new StringBuilder();
                    rollbackBuilder = new StringBuilder();
                    inRollback = false;
                } else if (trimmed.toLowerCase().startsWith(ROLLBACK_PREFIX) || trimmed.startsWith(ROLLBACK_PREFIX)) {
                    String rollbackSql = trimmed.substring(ROLLBACK_PREFIX.length()).trim();
                    rollbackBuilder.append(rollbackSql).append("\n");
                    inRollback = true;
                } else if (trimmed.equalsIgnoreCase(FORMATTED_SQL_HEADER)) {
                    // Skip the header line
                    continue;
                } else if (trimmed.startsWith("--") || trimmed.isEmpty()) {
                    // Skip comments and empty lines, but reset rollback flag
                    if (!trimmed.startsWith("--rollback")) {
                        inRollback = false;
                    }
                } else {
                    if (inRollback) {
                        rollbackBuilder.append(line).append("\n");
                    } else {
                        sqlBuilder.append(line).append("\n");
                    }
                }
            }

            // Save last changeset
            if (currentId != null) {
                changeSets.add(buildChangeSet(currentId, currentAuthor, sqlBuilder, rollbackBuilder,
                        physicalPath, parameterResolver));
            }

            if (LOG.isDebugEnabled()) {
                LOG.debug("Parsed " + changeSets.size() + " changeset(s) from [" + physicalPath + "].");
            }

            return new Changelog(physicalPath, changeSets);
        } catch (ChangelogParseException e) {
            throw e;
        } catch (Exception e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Failed to parse SQL changelog [" + e.getMessage(), e);
            }
            throw new ChangelogParseException("Failed to parse SQL changelog [" + physicalPath + "].", e);
        }
    }


    /**
     * @see com.github.toolarium.db.changelog.impl.parser.IChangelogParser#supports(String)
     */
    @Override
    public boolean supports(String fileExtension) {
        return ".sql".equals(fileExtension);
    }


    /**
     * Build a changeset from accumulated SQL and rollback text.
     *
     * @param id the changeset id
     * @param author the author
     * @param sqlBuilder the accumulated SQL
     * @param rollbackBuilder the accumulated rollback SQL
     * @param physicalPath the file path
     * @param parameterResolver the parameter resolver
     * @return the changeset
     */
    private IChangelogChangeSet buildChangeSet(String id, String author, StringBuilder sqlBuilder,
                                               StringBuilder rollbackBuilder, String physicalPath,
                                               IChangelogParameterResolver parameterResolver) {
        String sql = parameterResolver.resolve(sqlBuilder.toString().trim());
        ChangelogChangeSetIdentity identity = new ChangelogChangeSetIdentity(id, author, physicalPath);

        List<IChangelogEntry> entries = new ArrayList<>();
        if (!sql.isEmpty()) {
            entries.add(new SqlChangelogEntry(sql));
        }

        List<IChangelogEntry> rollbackEntries = Collections.emptyList();
        String rollbackSql = parameterResolver.resolve(rollbackBuilder.toString().trim());
        if (!rollbackSql.isEmpty()) {
            rollbackEntries = new ArrayList<>();
            rollbackEntries.add(new SqlChangelogEntry(rollbackSql));
        }

        return new ChangelogChangeSet(identity, entries, rollbackEntries, null,
                false, false, true, true, Collections.emptyList(), Collections.emptyList());
    }
}
