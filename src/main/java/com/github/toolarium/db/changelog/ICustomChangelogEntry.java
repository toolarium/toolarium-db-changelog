/*
 * ICustomChangelogEntry.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog;

import com.github.toolarium.db.changelog.exception.ChangelogException;
import java.sql.Connection;


/**
 * User-provided migration logic. Implement this interface to execute custom Java code
 * as part of a changeset. The class is referenced by name in the changelog and
 * instantiated via the default constructor.
 *
 * <p>Example usage in YAML:</p>
 * <pre>
 * changesets:
 *   - id: custom-migration
 *     changes:
 *       - customChange:
 *           class: com.example.MyCustomMigration
 * </pre>
 *
 * @author patrick
 */
public interface ICustomChangelogEntry {

    /**
     * Execute the custom migration logic.
     *
     * @param connection the database connection (same transaction as the changeset if runInTransaction is true)
     * @throws ChangelogException if the migration fails
     */
    void execute(Connection connection) throws ChangelogException;


    /**
     * Execute the rollback logic. Called when the changeset is rolled back.
     * Return without action if rollback is not supported.
     *
     * @param connection the database connection
     * @throws ChangelogException if the rollback fails
     */
    void rollback(Connection connection) throws ChangelogException;


    /**
     * Get a description of this custom change for logging and tracking.
     *
     * @return the description
     */
    String getDescription();


    /**
     * Check if this custom change supports rollback.
     *
     * @return true if rollback is supported
     */
    boolean supportsRollback();
}
