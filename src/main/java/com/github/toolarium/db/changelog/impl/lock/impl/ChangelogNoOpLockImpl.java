/*
 * ChangelogNoOpLockImpl.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog.impl.lock.impl;

import com.github.toolarium.db.changelog.impl.lock.IChangelogLock;

/**
 * No-op lock implementation used when leader election is disabled.
 * Provides no concurrency protection; suitable for single-instance deployments.
 *
 * @author patrick
 */
public class ChangelogNoOpLockImpl implements IChangelogLock {
    private volatile boolean locked;

    /**
     * @see com.github.toolarium.db.changelog.impl.lock.IChangelogLock#acquireLock()
     */
    @Override
    public void acquireLock() {
        locked = true;
    }

    /**
     * @see com.github.toolarium.db.changelog.impl.lock.IChangelogLock#releaseLock()
     */
    @Override
    public void releaseLock() {
        locked = false;
    }

    /**
     * @see com.github.toolarium.db.changelog.impl.lock.IChangelogLock#isLocked()
     */
    @Override
    public boolean isLocked() {
        return locked;
    }

    /**
     * @see java.lang.AutoCloseable#close()
     */
    @Override
    public void close() {
        releaseLock();
    }
}
