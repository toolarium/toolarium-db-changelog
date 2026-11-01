/*
 * IChangelogRollbackExecutor.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog.impl.executor;


import com.github.toolarium.db.changelog.dto.IChangelog;
import com.github.toolarium.db.changelog.exception.ChangelogException;
import java.sql.Connection;
import java.time.Instant;
import java.util.List;


/**
 * Executes rollback operations against a database.
 *
 * @author patrick
 */
public interface IChangelogRollbackExecutor {

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
