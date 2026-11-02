/*
 * ChangelogExecutorFactoryTest.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog.impl.executor;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import com.github.toolarium.db.changelog.impl.checksum.ChangelogChecksumGeneratorFactory;
import com.github.toolarium.db.changelog.impl.tracking.ChangelogTrackerFactory;
import org.junit.jupiter.api.Test;


/**
 * Tests for ChangelogExecutorFactory.
 *
 * @author patrick
 */
public class ChangelogExecutorFactoryTest {

    /**
     * Test singleton and instance creation.
     */
    @Test
    void testFactory() {
        assertSame(ChangelogExecutorFactory.getInstance(), ChangelogExecutorFactory.getInstance());
        assertNotNull(ChangelogExecutorFactory.getInstance().getExecutor(
                ChangelogTrackerFactory.getInstance().getTracker("TEST_TABLE", "0.0.1"),
                ChangelogChecksumGeneratorFactory.getInstance().getChecksumGenerator()));
    }
}
