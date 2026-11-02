/*
 * LiquibaseChangelogMigratorImpl.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog.impl.migrator.impl;

import com.github.toolarium.db.changelog.config.ChangelogConfiguration;
import com.github.toolarium.db.changelog.exception.ChangelogException;
import com.github.toolarium.db.changelog.impl.migrator.IChangelogMigrator;
import com.github.toolarium.db.changelog.impl.parser.ChangelogParserFactory;
import com.github.toolarium.db.changelog.impl.parser.IChangelogParser;
import com.github.toolarium.db.changelog.impl.resolver.impl.ChangelogParameterResolverImpl;
import com.github.toolarium.db.changelog.impl.writer.ChangelogWriterFactory;
import com.github.toolarium.db.changelog.impl.writer.IChangelogWriter;
import com.github.toolarium.db.changelog.model.IChangelog;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Migrates Liquibase changelogs (XML, YAML, JSON, SQL) to the native DB-Changelog format.
 * Uses the existing Liquibase parsers to read the source and the changelog writers to produce output.
 *
 * @author patrick
 */
public class LiquibaseChangelogMigratorImpl implements IChangelogMigrator {
    private static final Logger LOG = LoggerFactory.getLogger(LiquibaseChangelogMigratorImpl.class);
    private final InputStream sourceInput;
    private final String sourcePath;
    private final String outputFormat;
    private IChangelog cachedChangelog;


    /**
     * Constructor for LiquibaseChangelogMigratorImpl
     *
     * @param sourceInput the input stream of the Liquibase changelog
     * @param sourcePath the source file path (used for parser selection and changeset identity)
     * @param outputFormat the output file extension (e.g., ".db.yaml", ".db.xml", ".db.json")
     */
    public LiquibaseChangelogMigratorImpl(InputStream sourceInput, String sourcePath, String outputFormat) {
        this.sourceInput = sourceInput;
        this.sourcePath = sourcePath;
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
            LOG.debug("Migrated Liquibase changelog [" + sourcePath + "] to [" + outputFormat + "].");
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
            LOG.debug("Migrated Liquibase changelog [" + sourcePath + "] to [" + outputFormat + "].");
        }
    }


    /**
     * @see com.github.toolarium.db.changelog.impl.migrator.IChangelogMigrator#parse()
     */
    @Override
    public IChangelog parse() throws ChangelogException {
        if (cachedChangelog != null) {
            return cachedChangelog;
        }

        try {
            IChangelogParser parser = ChangelogParserFactory.getInstance().getParser(sourcePath);
            cachedChangelog = parser.parse(sourceInput, sourcePath, new ChangelogParameterResolverImpl());
            return cachedChangelog;
        } catch (ChangelogException e) {
            throw e;
        } catch (Exception e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Failed to parse Liquibase changelog [" + sourcePath + "]: " + e.getMessage(), e);
            }
            throw new ChangelogException("Failed to parse Liquibase changelog [" + sourcePath + "].", e);
        }
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
            throw new ChangelogException("Unsupported output format [" + outputFormat + "].");
        }
        return writer;
    }
}
