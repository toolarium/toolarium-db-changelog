/*
 * ChangelogNoOpLockTest.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog.impl.lock;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.toolarium.db.changelog.impl.lock.impl.ChangelogNoOpLockImpl;
import org.junit.jupiter.api.Test;


/**
 * Tests for ChangelogNoOpLockImpl.
 *
 * @author patrick
 */
public class ChangelogNoOpLockTest {

    /**
     * Test acquire, isLocked, release cycle.
     *
     * @throws Exception on error
     */
    @Test
    void testLockCycle() throws Exception {
        ChangelogNoOpLockImpl lock = new ChangelogNoOpLockImpl();

        assertFalse(lock.isLocked());
        lock.acquireLock();
        assertTrue(lock.isLocked());
        lock.releaseLock();
        assertFalse(lock.isLocked());
    }


    /**
     * Test close releases lock.
     *
     * @throws Exception on error
     */
    @Test
    void testCloseReleasesLock() throws Exception {
        ChangelogNoOpLockImpl lock = new ChangelogNoOpLockImpl();
        lock.acquireLock();
        assertTrue(lock.isLocked());
        lock.close();
        assertFalse(lock.isLocked());
    }
}
