/*
 * IChangelogRunner.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog;


import com.github.toolarium.db.changelog.dto.IChangelogChangeSetStatus;
import com.github.toolarium.db.changelog.dto.IChangelogTrackingEntry;
import com.github.toolarium.db.changelog.exception.ChangelogException;
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
