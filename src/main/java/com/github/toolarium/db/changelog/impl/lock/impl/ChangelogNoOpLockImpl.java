/*
 * ChangelogNoOpLockImpl.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog.impl.lock.impl;

import com.github.toolarium.db.changelog.IChangelogLock;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * No-op lock implementation used when leader election is disabled.
 * Provides no distributed concurrency protection; suitable for single-instance deployments only.
 *
 * @author patrick
 */
public class ChangelogNoOpLockImpl implements IChangelogLock {
    private final AtomicBoolean locked = new AtomicBoolean(false);


    /**
     * @see com.github.toolarium.db.changelog.IChangelogLock#acquireLock()
     */
    @Override
    public void acquireLock() {
        locked.set(true);
    }


    /**
     * @see com.github.toolarium.db.changelog.IChangelogLock#releaseLock()
     */
    @Override
    public void releaseLock() {
        locked.set(false);
    }


    /**
     * @see com.github.toolarium.db.changelog.IChangelogLock#isLocked()
     */
    @Override
    public boolean isLocked() {
        return locked.get();
    }


    /**
     * @see java.lang.AutoCloseable#close()
     */
    @Override
    public void close() {
        releaseLock();
    }
}
