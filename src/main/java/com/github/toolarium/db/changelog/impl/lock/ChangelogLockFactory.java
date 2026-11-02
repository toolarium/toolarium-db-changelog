/*
 * ChangelogLockFactory.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog.impl.lock;

import com.github.toolarium.db.changelog.impl.lock.impl.ChangelogLeaderElectionLockImpl;
import com.github.toolarium.db.changelog.impl.lock.impl.ChangelogNoOpLockImpl;
import javax.sql.DataSource;


/**
 * Singleton factory for creating changelog lock instances.
 *
 * @author patrick
 */
public final class ChangelogLockFactory {

    /**
     * Private class, the only instance of the singleton which will be created by accessing the holder class.
     *
     * @author patrick
     */
    private static final class HOLDER {
        static final ChangelogLockFactory INSTANCE = new ChangelogLockFactory();
    }


    /**
     * Private constructor
     */
    private ChangelogLockFactory() {
    }


    /**
     * Get the instance.
     *
     * @return the instance
     */
    public static ChangelogLockFactory getInstance() {
        return HOLDER.INSTANCE;
    }


    /**
     * Get a no-op lock (single-instance, no concurrency protection).
     *
     * @return the lock instance
     */
    public IChangelogLock getChangelogLock() {
        return new ChangelogNoOpLockImpl();
    }


    /**
     * Get a leader-election based lock using the DATABASE strategy.
     * Uses the same DataSource as the changelog and creates a lock table.
     *
     * @param dataSource the data source
     * @param lockTableName the lock table name (e.g., DB_CHANGELOG_LOCK)
     * @return the lock instance
     */
    public IChangelogLock getChangelogLock(DataSource dataSource, String lockTableName) {
        return new ChangelogLeaderElectionLockImpl(dataSource, lockTableName);
    }
}
