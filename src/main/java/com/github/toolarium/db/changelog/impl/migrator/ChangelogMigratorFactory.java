/*
 * ChangelogMigratorFactory.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog.impl.migrator;

import com.github.toolarium.db.changelog.impl.migrator.impl.FlywayChangelogMigratorImpl;
import com.github.toolarium.db.changelog.impl.migrator.impl.LiquibaseChangelogMigratorImpl;
import java.io.File;
import java.io.InputStream;


/**
 * Singleton factory for changelog migrators.
 *
 * @author patrick
 */
public final class ChangelogMigratorFactory {

    /**
     * Private class, the only instance of the singleton which will be created by accessing the holder class.
     *
     * @author patrick
     */
    private static final class HOLDER {
        static final ChangelogMigratorFactory INSTANCE = new ChangelogMigratorFactory();
    }


    /**
     * Private constructor
     */
    private ChangelogMigratorFactory() {
    }


    /**
     * Get the instance.
     *
     * @return the instance
     */
    public static ChangelogMigratorFactory getInstance() {
        return HOLDER.INSTANCE;
    }


    /**
     * Get a Liquibase changelog migrator.
     *
     * @param sourceInput the input stream of the Liquibase changelog
     * @param sourcePath the source file path (used for parser selection)
     * @param outputFormat the output file extension (e.g., ".db.yaml", ".db.xml", ".db.json")
     * @return the migrator
     */
    public IChangelogMigrator getLiquibaseMigrator(InputStream sourceInput, String sourcePath, String outputFormat) {
        return new LiquibaseChangelogMigratorImpl(sourceInput, sourcePath, outputFormat);
    }


    /**
     * Get a Flyway changelog migrator.
     *
     * @param migrationDirectory the directory containing Flyway SQL files
     * @param outputFormat the output file extension (e.g., ".db.yaml", ".db.xml", ".db.json")
     * @return the migrator
     */
    public IChangelogMigrator getFlywayMigrator(File migrationDirectory, String outputFormat) {
        return new FlywayChangelogMigratorImpl(migrationDirectory, outputFormat);
    }
}
