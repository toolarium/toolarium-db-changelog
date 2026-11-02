/*
 * IChangelogExecutor.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog;

import com.github.toolarium.db.changelog.exception.ChangelogException;
import com.github.toolarium.db.changelog.model.IChangelog;
import java.sql.Connection;
import java.time.Instant;
import java.util.List;


/**
 * Executes changelog operations (update and rollback) against a database connection.
 *
 * @author patrick
 */
public interface IChangelogExecutor {

    /**
     * Execute all pending changesets from the changelog.
     *
     * @param connection the database connection
     * @param changelog the changelog to execute
     * @param context the execution context with active contexts and labels
     * @throws ChangelogException if execution fails
     */
    void update(Connection connection, IChangelog changelog, IChangelogContext context) throws ChangelogException;


    /**
     * Execute up to count pending changesets from the changelog.
     *
     * @param connection the database connection
     * @param changelog the changelog to execute
     * @param count the maximum number of changesets to execute
     * @param context the execution context with active contexts and labels
     * @throws ChangelogException if execution fails
     */
    void update(Connection connection, IChangelog changelog, int count, IChangelogContext context) throws ChangelogException;


    /**
     * Rollback the last count applied changesets.
     *
     * @param connection the database connection
     * @param changelog the changelog (needed to find rollback entries)
     * @param count the number of changesets to rollback
     * @throws ChangelogException if rollback fails
     */
    void rollback(Connection connection, IChangelog changelog, int count) throws ChangelogException;


    /**
     * Rollback all changesets applied after the given tag.
     *
     * @param connection the database connection
     * @param changelog the changelog
     * @param tag the tag name
     * @throws ChangelogException if rollback fails
     */
    void rollbackToTag(Connection connection, IChangelog changelog, String tag) throws ChangelogException;


    /**
     * Rollback all changesets applied after the given date.
     *
     * @param connection the database connection
     * @param changelog the changelog
     * @param dateTime the point in time
     * @throws ChangelogException if rollback fails
     */
    void rollbackToDate(Connection connection, IChangelog changelog, Instant dateTime) throws ChangelogException;


    /**
     * Generate rollback SQL without executing.
     *
     * @param connection the database connection
     * @param changelog the changelog
     * @param count the number of changesets
     * @return the list of rollback SQL statements
     * @throws ChangelogException if generation fails
     */
    List<String> generateRollbackSql(Connection connection, IChangelog changelog, int count) throws ChangelogException;
}
