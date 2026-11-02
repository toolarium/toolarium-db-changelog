/*
 * IChangelogSnapshotManager.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog.impl.snapshot;

import com.github.toolarium.db.changelog.exception.ChangelogException;
import com.github.toolarium.db.changelog.model.IChangelogChangeSetIdentity;
import java.sql.Connection;


/**
 * Manages pre-execution snapshots of database metadata for rollback support.
 * Captures table structure via JDBC DatabaseMetaData before changeset execution
 * and stores it in the DB_CHANGELOG_SNAPSHOT table.
 *
 * @author patrick
 */
public interface IChangelogSnapshotManager {

    /**
     * Ensure the snapshot table exists.
     *
     * @param connection the database connection
     * @throws ChangelogException if the table cannot be created
     */
    void ensureSnapshotTable(Connection connection) throws ChangelogException;


    /**
     * Capture a snapshot of the affected table before a changeset is executed.
     *
     * @param connection the database connection
     * @param identity the changeset identity
     * @param tableName the table to snapshot
     * @throws ChangelogException if the snapshot cannot be captured
     */
    void captureSnapshot(Connection connection, IChangelogChangeSetIdentity identity, String tableName) throws ChangelogException;


    /**
     * Get the snapshot for a changeset and table.
     *
     * @param connection the database connection
     * @param identity the changeset identity
     * @param tableName the table name
     * @return the snapshot data as a string, or null if no snapshot exists
     * @throws ChangelogException if the snapshot cannot be read
     */
    String getSnapshot(Connection connection, IChangelogChangeSetIdentity identity, String tableName) throws ChangelogException;


    /**
     * Remove the snapshot for a changeset (called after successful rollback).
     *
     * @param connection the database connection
     * @param identity the changeset identity
     * @throws ChangelogException if the snapshot cannot be removed
     */
    void removeSnapshot(Connection connection, IChangelogChangeSetIdentity identity) throws ChangelogException;


    /**
     * Remove all snapshots older than the given retention count.
     *
     * @param connection the database connection
     * @param retentionCount the number of most recent snapshots to keep
     * @throws ChangelogException if cleanup fails
     */
    void cleanupSnapshots(Connection connection, int retentionCount) throws ChangelogException;
}
