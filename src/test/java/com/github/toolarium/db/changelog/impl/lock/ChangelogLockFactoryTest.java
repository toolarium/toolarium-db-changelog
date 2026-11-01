/*
 * ChangelogLockFactoryTest.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog.impl.lock;


import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;


/**
 * Tests for ChangelogLockFactory.
 *
 * @author patrick
 */
public class ChangelogLockFactoryTest {

    /**
     * Test singleton and instance creation.
     */
    @Test
    void testFactory() {
        assertSame(ChangelogLockFactory.getInstance(), ChangelogLockFactory.getInstance());
        assertNotNull(ChangelogLockFactory.getInstance().getChangelogLock());
    }
}
