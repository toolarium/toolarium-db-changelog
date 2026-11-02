/*
 * IChangelogTracker.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog.impl.tracking;

import com.github.toolarium.db.changelog.exception.ChangelogException;
import com.github.toolarium.db.changelog.model.ChangelogExecutionType;
import com.github.toolarium.db.changelog.model.IChangelogChangeSet;
import com.github.toolarium.db.changelog.model.IChangelogChangeSetIdentity;
import com.github.toolarium.db.changelog.model.IChangelogTrackingEntry;
import java.sql.Connection;
import java.util.List;


/**
 * Manages the DB_CHANGELOG tracking table.
 *
 * @author patrick
 */
public interface IChangelogTracker {

    /**
     * Ensure the tracking table exists, creating it if necessary.
     *
     * @param connection the database connection
     * @throws ChangelogException if the table cannot be created
     */
    void ensureTrackingTable(Connection connection) throws ChangelogException;

    /**
     * Get all applied changesets from the tracking table.
     *
     * @param connection the database connection
     * @return the list of applied changeset entries
     * @throws ChangelogException if the query fails
     */
    List<IChangelogTrackingEntry> getAppliedChangeSets(Connection connection) throws ChangelogException;

    /**
     * Record a changeset as executed in the tracking table.
     *
     * @param connection the database connection
     * @param changeSet the changeset that was executed
     * @param checksum the computed checksum
     * @param executionType the execution type
     * @param deploymentId the deployment id grouping this run
     * @throws ChangelogException if the insert fails
     */
    void markChangeSetExecuted(Connection connection, IChangelogChangeSet changeSet, String checksum,
                               ChangelogExecutionType executionType, String deploymentId) throws ChangelogException;

    /**
     * Remove a changeset entry from the tracking table.
     *
     * @param connection the database connection
     * @param identity the changeset identity to remove
     * @throws ChangelogException if the delete fails
     */
    void removeChangeSetEntry(Connection connection, IChangelogChangeSetIdentity identity) throws ChangelogException;

    /**
     * Tag the last applied changeset.
     *
     * @param connection the database connection
     * @param tagName the tag name
     * @throws ChangelogException if the update fails
     */
    void tagLastApplied(Connection connection, String tagName) throws ChangelogException;

    /**
     * Clear all stored checksums (set to null). Forces recalculation on next run.
     *
     * @param connection the database connection
     * @throws ChangelogException if the update fails
     */
    void clearChecksums(Connection connection) throws ChangelogException;
}
