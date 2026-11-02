/*
 * IChangelogMigrator.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog.impl.migrator;

import com.github.toolarium.db.changelog.exception.ChangelogException;
import com.github.toolarium.db.changelog.model.IChangelog;
import java.io.OutputStream;
import java.io.Writer;


/**
 * Migrates changelogs from an external format (Liquibase, Flyway) to the native DB-Changelog format.
 *
 * @author patrick
 */
public interface IChangelogMigrator {

    /**
     * Migrate the source changelog to the native format and write to the given output stream.
     *
     * @param output the output stream to write the migrated changelog to
     * @throws ChangelogException if migration fails
     */
    void migrate(OutputStream output) throws ChangelogException;


    /**
     * Migrate the source changelog to the native format and write to the given writer.
     *
     * @param writer the writer to write the migrated changelog to
     * @throws ChangelogException if migration fails
     */
    void migrate(Writer writer) throws ChangelogException;


    /**
     * Parse the source changelog into the internal model without writing.
     *
     * @return the parsed changelog model
     * @throws ChangelogException if parsing fails
     */
    IChangelog parse() throws ChangelogException;
}
