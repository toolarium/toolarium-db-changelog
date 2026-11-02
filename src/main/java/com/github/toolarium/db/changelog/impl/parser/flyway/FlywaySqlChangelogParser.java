/*
 * FlywaySqlChangelogParser.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog.impl.parser.flyway;

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
 * Parses Flyway SQL migration files into the internal changelog model.
 * Supports versioned migrations (V{version}__{description}.sql) and
 * repeatable migrations (R__{description}.sql).
 *
 * <p>Versioned migrations produce a single changeset with id = version and description as comment.
 * Repeatable migrations produce a changeset with runOnChange = true.</p>
 *
 * @author patrick
 */
public class FlywaySqlChangelogParser implements IChangelogParser {
    private static final Logger LOG = LoggerFactory.getLogger(FlywaySqlChangelogParser.class);
    private static final String DEFAULT_AUTHOR = "flyway";


    /**
     * @see com.github.toolarium.db.changelog.impl.parser.IChangelogParser#parse(InputStream, String, IChangelogParameterResolver)
     */
    @Override
    public IChangelog parse(InputStream input, String physicalPath, IChangelogParameterResolver parameterResolver) throws ChangelogParseException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
            String fileName = extractFileName(physicalPath);

            StringBuilder sqlBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sqlBuilder.append(line).append("\n");
            }

            String sql = parameterResolver.resolve(sqlBuilder.toString().trim());
            if (sql.isEmpty()) {
                throw new ChangelogParseException("Empty Flyway migration file [" + physicalPath + "].");
            }

            FlywayMigrationInfo info = parseMigrationInfo(fileName, physicalPath);
            List<IChangelogEntry> entries = new ArrayList<>();
            entries.add(new SqlChangelogEntry(sql));

            ChangelogChangeSetIdentity identity = new ChangelogChangeSetIdentity(info.getId(), DEFAULT_AUTHOR, physicalPath);
            ChangelogChangeSet changeSet = new ChangelogChangeSet(identity, entries, Collections.emptyList(),
                    info.getDescription(), false, info.isRepeatable(), true, true,
                    Collections.emptyList(), Collections.emptyList(), null);

            List<IChangelogChangeSet> changeSets = new ArrayList<>();
            changeSets.add(changeSet);

            if (LOG.isDebugEnabled()) {
                LOG.debug("Parsed Flyway migration [" + fileName + "] as changeset [" + info.id + "].");
            }

            return new Changelog(physicalPath, changeSets);
        } catch (ChangelogParseException e) {
            throw e;
        } catch (Exception e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Failed to parse Flyway migration [" + e.getMessage(), e);
            }
            throw new ChangelogParseException("Failed to parse Flyway migration [" + physicalPath + "].", e);
        }
    }


    /**
     * @see com.github.toolarium.db.changelog.impl.parser.IChangelogParser#supports(String)
     */
    @Override
    public boolean supports(String fileExtension) {
        // Flyway uses .sql extension but the parser is selected explicitly by the migrator,
        // not by the generic parser factory (to avoid conflict with LiquibaseSqlChangelogParser).
        return false;
    }


    /**
     * Check if a file name matches the Flyway migration naming convention.
     *
     * @param fileName the file name (without path)
     * @return true if the file matches V*__*.sql or R__*.sql
     */
    public boolean isFlywayMigration(String fileName) {
        if (fileName == null) {
            return false;
        }
        return isVersionedMigration(fileName) || isRepeatableMigration(fileName);
    }


    /**
     * Extract the file name from a path.
     *
     * @param path the file path
     * @return the file name
     */
    private String extractFileName(String path) {
        int lastSlash = Math.max(path.lastIndexOf('/'), path.lastIndexOf('\\'));
        if (lastSlash >= 0) {
            return path.substring(lastSlash + 1);
        }
        return path;
    }


    /**
     * Parse migration info from a Flyway file name.
     *
     * @param fileName the file name
     * @param physicalPath the full path for error messages
     * @return the migration info
     * @throws ChangelogParseException if the file name is not a valid Flyway migration
     */
    private FlywayMigrationInfo parseMigrationInfo(String fileName, String physicalPath) throws ChangelogParseException {
        if (isVersionedMigration(fileName)) {
            return parseVersionedMigration(fileName, physicalPath);
        }
        if (isRepeatableMigration(fileName)) {
            return parseRepeatableMigration(fileName);
        }
        throw new ChangelogParseException("File [" + physicalPath + "] does not match Flyway naming convention (V*__*.sql or R__*.sql).");
    }


    /**
     * Check if a file name matches the versioned migration pattern (V*__*.sql).
     *
     * @param fileName the file name
     * @return true if versioned migration
     */
    private boolean isVersionedMigration(String fileName) {
        if (!fileName.startsWith("V") || !fileName.endsWith(".sql")) {
            return false;
        }
        int separatorIdx = fileName.indexOf("__");
        if (separatorIdx <= 1) {
            return false;
        }
        // Version part (between V and __) must contain only digits, dots, or underscores
        String version = fileName.substring(1, separatorIdx);
        for (int i = 0; i < version.length(); i++) {
            char ch = version.charAt(i);
            if (!Character.isDigit(ch) && ch != '.' && ch != '_') {
                return false;
            }
        }
        return true;
    }


    /**
     * Check if a file name matches the repeatable migration pattern (R__*.sql).
     *
     * @param fileName the file name
     * @return true if repeatable migration
     */
    private boolean isRepeatableMigration(String fileName) {
        return fileName.startsWith("R__") && fileName.endsWith(".sql") && fileName.length() > 7;
    }


    /**
     * Parse a versioned migration file name (V{version}__{description}.sql).
     *
     * @param fileName the file name
     * @param physicalPath the full path for error messages
     * @return the migration info
     * @throws ChangelogParseException if parsing fails
     */
    private FlywayMigrationInfo parseVersionedMigration(String fileName, String physicalPath) throws ChangelogParseException {
        // Remove the .sql extension
        String name = fileName.substring(0, fileName.length() - 4);

        int separatorIndex = name.indexOf("__");
        if (separatorIndex <= 1) {
            throw new ChangelogParseException("Invalid Flyway versioned migration name [" + fileName + "] in [" + physicalPath + "].");
        }

        String version = name.substring(1, separatorIndex);
        String description = name.substring(separatorIndex + 2).replace('_', ' ');

        FlywayMigrationInfo info = new FlywayMigrationInfo();
        info.id = version;
        info.description = description;
        info.repeatable = false;
        return info;
    }


    /**
     * Parse a repeatable migration file name (R__{description}.sql).
     *
     * @param fileName the file name
     * @return the migration info
     */
    private FlywayMigrationInfo parseRepeatableMigration(String fileName) {
        // Remove "R__" prefix and ".sql" extension
        String name = fileName.substring(3, fileName.length() - 4);
        String description = name.replace('_', ' ');

        FlywayMigrationInfo info = new FlywayMigrationInfo();
        info.id = "R__" + name;
        info.description = description;
        info.repeatable = true;
        return info;
    }


    /**
     * Internal holder for parsed Flyway migration info.
     *
     * @author patrick
     */
    private static final class FlywayMigrationInfo {
        private String id;
        private String description;
        private boolean repeatable;


        /**
         * Get the id.
         *
         * @return the id
         */
        String getId() {
            return id;
        }


        /**
         * Get the description.
         *
         * @return the description
         */
        String getDescription() {
            return description;
        }


        /**
         * Check if repeatable.
         *
         * @return true if repeatable
         */
        boolean isRepeatable() {
            return repeatable;
        }
    }
}
