/*
 * ChangelogTrackerFactory.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog.impl.tracking;

import com.github.toolarium.db.changelog.impl.tracking.impl.ChangelogTrackerImpl;


/**
 * Singleton factory for creating changelog tracker instances.
 *
 * @author patrick
 */
public final class ChangelogTrackerFactory {

    /**
     * Private class, the only instance of the singleton which will be created by accessing the holder class.
     *
     * @author patrick
     */
    private static final class HOLDER {
        static final ChangelogTrackerFactory INSTANCE = new ChangelogTrackerFactory();
    }


    /**
     * Private constructor
     */
    private ChangelogTrackerFactory() {
    }


    /**
     * Get the instance.
     *
     * @return the instance
     */
    public static ChangelogTrackerFactory getInstance() {
        return HOLDER.INSTANCE;
    }


    /**
     * Create a JDBC-based changelog tracker.
     *
     * @param tableName the tracking table name
     * @param libraryVersion the library version
     * @return the tracker instance
     */
    public IChangelogTracker getTracker(String tableName, String libraryVersion) {
        return new ChangelogTrackerImpl(tableName, libraryVersion);
    }
}
