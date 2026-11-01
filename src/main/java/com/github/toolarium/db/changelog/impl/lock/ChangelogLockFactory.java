/*
 * ChangelogLockFactory.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog.impl.lock;


import com.github.toolarium.db.changelog.impl.lock.impl.ChangelogNoOpLockImpl;


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
     * Create a no-op lock (single-instance, no concurrency protection).
     *
     * @return the lock instance
     */
    public IChangelogLock getChangelogLock() {
        return new ChangelogNoOpLockImpl();
    }
}
