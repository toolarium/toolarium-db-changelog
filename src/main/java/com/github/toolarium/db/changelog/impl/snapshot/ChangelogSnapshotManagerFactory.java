/*
 * ChangelogSnapshotManagerFactory.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog.impl.snapshot;

import com.github.toolarium.db.changelog.impl.snapshot.impl.ChangelogSnapshotManagerImpl;


/**
 * Singleton factory for creating changelog snapshot manager instances.
 *
 * @author patrick
 */
public final class ChangelogSnapshotManagerFactory {

    /** Default snapshot table name. */
    public static final String DEFAULT_SNAPSHOT_TABLE = "DB_CHANGELOG_SNAPSHOT";


    /**
     * Private class, the only instance of the singleton which will be created by accessing the holder class.
     *
     * @author patrick
     */
    private static final class HOLDER {
        static final ChangelogSnapshotManagerFactory INSTANCE = new ChangelogSnapshotManagerFactory();
    }


    /**
     * Private constructor
     */
    private ChangelogSnapshotManagerFactory() {
    }


    /**
     * Get the instance.
     *
     * @return the instance
     */
    public static ChangelogSnapshotManagerFactory getInstance() {
        return HOLDER.INSTANCE;
    }


    /**
     * Get a snapshot manager with the default table name.
     *
     * @return the snapshot manager
     */
    public IChangelogSnapshotManager getSnapshotManager() {
        return new ChangelogSnapshotManagerImpl(DEFAULT_SNAPSHOT_TABLE);
    }


    /**
     * Get a snapshot manager with a custom table name.
     *
     * @param tableName the snapshot table name
     * @return the snapshot manager
     */
    public IChangelogSnapshotManager getSnapshotManager(String tableName) {
        return new ChangelogSnapshotManagerImpl(tableName);
    }
}
