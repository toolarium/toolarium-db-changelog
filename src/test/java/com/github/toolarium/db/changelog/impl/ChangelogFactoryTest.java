/*
 * ChangelogFactoryTest.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog.impl;


import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import com.github.toolarium.db.changelog.impl.checksum.ChangelogChecksumGeneratorFactory;
import com.github.toolarium.db.changelog.impl.executor.ChangelogExecutorFactory;
import com.github.toolarium.db.changelog.impl.lock.ChangelogLockFactory;
import com.github.toolarium.db.changelog.impl.resolver.ChangelogParameterResolverFactory;
import com.github.toolarium.db.changelog.impl.tracking.ChangelogTrackerFactory;
import org.junit.jupiter.api.Test;


/**
 * Tests for singleton factory instances.
 *
 * @author patrick
 */
public class ChangelogFactoryTest {

    /**
     * Test ChangelogLockFactory singleton and instance creation.
     */
    @Test
    void testLockFactory() {
        assertSame(ChangelogLockFactory.getInstance(), ChangelogLockFactory.getInstance());
        assertNotNull(ChangelogLockFactory.getInstance().getChangelogLock());
    }


    /**
     * Test ChangelogChecksumGeneratorFactory singleton and instance creation.
     */
    @Test
    void testChecksumFactory() {
        assertSame(ChangelogChecksumGeneratorFactory.getInstance(), ChangelogChecksumGeneratorFactory.getInstance());
        assertNotNull(ChangelogChecksumGeneratorFactory.getInstance().getChecksumGenerator());
    }


    /**
     * Test ChangelogExecutorFactory singleton and instance creation.
     */
    @Test
    void testExecutorFactory() {
        assertSame(ChangelogExecutorFactory.getInstance(), ChangelogExecutorFactory.getInstance());
        assertNotNull(ChangelogExecutorFactory.getInstance().getExecutor(
                ChangelogTrackerFactory.getInstance().getTracker("TEST_TABLE", "0.0.1"),
                ChangelogChecksumGeneratorFactory.getInstance().getChecksumGenerator()));
    }


    /**
     * Test ChangelogParameterResolverFactory singleton and instance creation.
     */
    @Test
    void testParameterResolverFactory() {
        assertSame(ChangelogParameterResolverFactory.getInstance(), ChangelogParameterResolverFactory.getInstance());
        assertNotNull(ChangelogParameterResolverFactory.getInstance().getParameterResolver());
    }


    /**
     * Test ChangelogTrackerFactory singleton and instance creation.
     */
    @Test
    void testTrackerFactory() {
        assertSame(ChangelogTrackerFactory.getInstance(), ChangelogTrackerFactory.getInstance());
        assertNotNull(ChangelogTrackerFactory.getInstance().getTracker("DB_CHANGELOG", "0.0.1"));
    }
}
