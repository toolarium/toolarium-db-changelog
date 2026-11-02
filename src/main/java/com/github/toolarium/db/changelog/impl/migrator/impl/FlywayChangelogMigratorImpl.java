/*
 * FlywayChangelogMigratorImpl.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog.impl.migrator.impl;

import com.github.toolarium.db.changelog.config.ChangelogConfiguration;
import com.github.toolarium.db.changelog.exception.ChangelogException;
import com.github.toolarium.db.changelog.impl.migrator.IChangelogMigrator;
import com.github.toolarium.db.changelog.impl.parser.flyway.FlywaySqlChangelogParser;
import com.github.toolarium.db.changelog.impl.resolver.impl.ChangelogParameterResolverImpl;
import com.github.toolarium.db.changelog.impl.writer.ChangelogWriterFactory;
import com.github.toolarium.db.changelog.impl.writer.IChangelogWriter;
import com.github.toolarium.db.changelog.model.Changelog;
import com.github.toolarium.db.changelog.model.IChangelog;
import com.github.toolarium.db.changelog.model.IChangelogChangeSet;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Migrates Flyway SQL migration files (V*__*.sql, R__*.sql) from a directory
 * to a single native DB-Changelog format file.
 *
 * <p>Versioned migrations are sorted by version number and converted to regular changesets.
 * Repeatable migrations are converted to changesets with runOnChange = true.</p>
 *
 * @author patrick
 */
public class FlywayChangelogMigratorImpl implements IChangelogMigrator {
    private static final Logger LOG = LoggerFactory.getLogger(FlywayChangelogMigratorImpl.class);
    private static final String BRACKET_DOT = "].";
    private final File migrationDirectory;
    private final String outputFormat;
    private final FlywaySqlChangelogParser parser;


    /**
     * Constructor for FlywayChangelogMigratorImpl
     *
     * @param migrationDirectory the directory containing Flyway SQL files
     * @param outputFormat the output file extension (e.g., ".db.yaml", ".db.xml", ".db.json")
     */
    public FlywayChangelogMigratorImpl(File migrationDirectory, String outputFormat) {
        this.migrationDirectory = migrationDirectory;
        this.parser = new FlywaySqlChangelogParser();
        if (outputFormat == null) {
            this.outputFormat = ChangelogConfiguration.FILE_EXTENSION_DB_YAML;
        } else {
            this.outputFormat = outputFormat;
        }
    }


    /**
     * @see com.github.toolarium.db.changelog.impl.migrator.IChangelogMigrator#migrate(OutputStream)
     */
    @Override
    public void migrate(OutputStream output) throws ChangelogException {
        IChangelog changelog = parse();
        IChangelogWriter writer = getWriter();
        writer.write(changelog, output);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Migrated " + changelog.getChangelogSets().size() + " Flyway migration(s) to [" + outputFormat + BRACKET_DOT);
        }
    }


    /**
     * @see com.github.toolarium.db.changelog.impl.migrator.IChangelogMigrator#migrate(Writer)
     */
    @Override
    public void migrate(Writer writer) throws ChangelogException {
        IChangelog changelog = parse();
        IChangelogWriter changelogWriter = getWriter();
        changelogWriter.write(changelog, writer);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Migrated " + changelog.getChangelogSets().size() + " Flyway migration(s) to [" + outputFormat + BRACKET_DOT);
        }
    }


    /**
     * @see com.github.toolarium.db.changelog.impl.migrator.IChangelogMigrator#parse()
     */
    @Override
    public IChangelog parse() throws ChangelogException {
        if (!migrationDirectory.isDirectory()) {
            throw new ChangelogException("Flyway migration directory does not exist [" + migrationDirectory.getAbsolutePath() + BRACKET_DOT);
        }

        File[] sqlFiles = migrationDirectory.listFiles((dir, name) -> name.endsWith(".sql") && parser.isFlywayMigration(name));
        if (sqlFiles == null || sqlFiles.length == 0) {
            throw new ChangelogException("No Flyway migration files found in [" + migrationDirectory.getAbsolutePath() + BRACKET_DOT);
        }

        // Sort files: versioned first (by version number), then repeatable (by name)
        Arrays.sort(sqlFiles, (first, second) -> {
            boolean firstVersioned = first.getName().startsWith("V");
            boolean secondVersioned = second.getName().startsWith("V");
            if (firstVersioned && !secondVersioned) {
                return -1;
            }
            if (!firstVersioned && secondVersioned) {
                return 1;
            }
            if (firstVersioned) {
                return compareVersions(extractVersion(first.getName()), extractVersion(second.getName()));
            }
            return first.getName().compareTo(second.getName());
        });

        List<IChangelogChangeSet> allChangeSets = new ArrayList<>();
        for (File sqlFile : sqlFiles) {
            try (InputStream input = new FileInputStream(sqlFile)) {
                IChangelog parsed = parser.parse(input, sqlFile.getPath(), new ChangelogParameterResolverImpl());
                allChangeSets.addAll(parsed.getChangelogSets());
            } catch (ChangelogException e) {
                throw e;
            } catch (Exception e) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Failed to parse Flyway migration [" + e.getMessage(), e);
                }
                throw new ChangelogException("Failed to parse Flyway migration [" + sqlFile.getName() + BRACKET_DOT, e);
            }
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Parsed " + allChangeSets.size() + " changeset(s) from " + sqlFiles.length + " Flyway migration file(s).");
        }

        return new Changelog(migrationDirectory.getPath(), allChangeSets);
    }


    /**
     * Get the writer for the configured output format.
     *
     * @return the changelog writer
     * @throws ChangelogException if the output format is not supported
     */
    private IChangelogWriter getWriter() throws ChangelogException {
        IChangelogWriter writer = ChangelogWriterFactory.getInstance().getWriter(outputFormat);
        if (writer == null) {
            throw new ChangelogException("Unsupported output format [" + outputFormat + BRACKET_DOT);
        }
        return writer;
    }


    /**
     * Extract the version string from a Flyway versioned migration filename.
     *
     * @param fileName the file name (e.g., "V1.2.3__description.sql")
     * @return the version string (e.g., "1.2.3")
     */
    private String extractVersion(String fileName) {
        int separatorIdx = fileName.indexOf("__");
        if (separatorIdx > 1) {
            return fileName.substring(1, separatorIdx);
        }
        return fileName;
    }


    /**
     * Compare two Flyway version strings numerically segment by segment.
     *
     * @param version1 the first version
     * @param version2 the second version
     * @return negative, zero, or positive
     */
    private int compareVersions(String version1, String version2) {
        String[] parts1 = version1.split("[._]");
        String[] parts2 = version2.split("[._]");
        int length = Math.max(parts1.length, parts2.length);
        for (int i = 0; i < length; i++) {
            int num1 = 0;
            int num2 = 0;
            if (i < parts1.length) {
                try {
                    num1 = Integer.parseInt(parts1[i]);
                } catch (NumberFormatException e) {
                    String other = "";
                    if (i < parts2.length) {
                        other = parts2[i];
                    }
                    return parts1[i].compareTo(other);
                }
            }
            if (i < parts2.length) {
                try {
                    num2 = Integer.parseInt(parts2[i]);
                } catch (NumberFormatException e) {
                    String other = "";
                    if (i < parts1.length) {
                        other = parts1[i];
                    }
                    return other.compareTo(parts2[i]);
                }
            }
            if (num1 != num2) {
                return Integer.compare(num1, num2);
            }
        }
        return 0;
    }
}
