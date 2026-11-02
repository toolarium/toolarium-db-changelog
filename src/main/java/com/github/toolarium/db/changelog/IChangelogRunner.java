/*
 * IChangelogRunner.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog;

import com.github.toolarium.db.changelog.exception.ChangelogException;
import com.github.toolarium.db.changelog.model.IChangelogChangeSetStatus;
import com.github.toolarium.db.changelog.model.IChangelogTrackingEntry;
import java.time.Instant;
import java.util.List;


/**
 * Main entry point for database changelog operations.
 * 
 * @author patrick
 */
public interface IChangelogRunner {

    /**
     * Apply all pending changesets.
     *
     * @throws ChangelogException if execution fails
     */
    void update() throws ChangelogException;

    /**
     * Apply up to count pending changesets.
     *
     * @param count the maximum number of changesets to apply
     * @throws ChangelogException if execution fails
     */
    void update(int count) throws ChangelogException;

    /**
     * Rollback the last count applied changesets.
     *
     * @param count the number of changesets to rollback
     * @throws ChangelogException if rollback fails
     */
    void rollback(int count) throws ChangelogException;


    /**
     * Rollback all changesets applied after the given tag.
     *
     * @param tag the tag name to rollback to
     * @throws ChangelogException if rollback fails
     */
    void rollback(String tag) throws ChangelogException;


    /**
     * Rollback all changesets applied after the given date.
     *
     * @param dateTime the point in time to rollback to
     * @throws ChangelogException if rollback fails
     */
    void rollback(Instant dateTime) throws ChangelogException;


    /**
     * Generate the rollback SQL for the last count changesets without executing.
     *
     * @param count the number of changesets
     * @return the list of rollback SQL statements
     * @throws ChangelogException if generation fails
     */
    List<String> generateRollbackSql(int count) throws ChangelogException;


    /**
     * Tag the current database state.
     *
     * @param tagName the tag name
     * @throws ChangelogException if tagging fails
     */
    void tag(String tagName) throws ChangelogException;

    /**
     * Mark all pending changesets as executed without running them.
     *
     * @throws ChangelogException if sync fails
     */
    void sync() throws ChangelogException;

    /**
     * List pending (not yet applied) changesets.
     *
     * @return the list of pending changeset statuses
     * @throws ChangelogException if status check fails
     */
    List<IChangelogChangeSetStatus> status() throws ChangelogException;

    /**
     * Get the history of applied changesets.
     *
     * @return the list of applied changeset entries
     * @throws ChangelogException if the query fails
     */
    List<IChangelogTrackingEntry> history() throws ChangelogException;

    /**
     * Clear all stored checksums. Forces recalculation on next run.
     *
     * @throws ChangelogException if clearing fails
     */
    void clearChecksums() throws ChangelogException;
}
