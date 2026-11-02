/*
 * IChangelogLock.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog;

import com.github.toolarium.db.changelog.exception.ChangelogException;


/**
 * Migration locking interface.
 *
 * @author patrick
 */
public interface IChangelogLock extends AutoCloseable {

    /**
     * Acquire the migration lock.
     *
     * @throws ChangelogException if the lock cannot be acquired
     */
    void acquireLock() throws ChangelogException;

    /**
     * Release the migration lock.
     *
     * @throws ChangelogException if the lock cannot be released
     */
    void releaseLock() throws ChangelogException;

    /**
     * Check if the lock is currently held.
     *
     * @return true if locked
     */
    boolean isLocked();
}
