/*
 * ChangelogTrackerFactoryTest.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog.impl.tracking;


import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;


/**
 * Tests for ChangelogTrackerFactory.
 *
 * @author patrick
 */
public class ChangelogTrackerFactoryTest {

    /**
     * Test singleton and instance creation.
     */
    @Test
    void testFactory() {
        assertSame(ChangelogTrackerFactory.getInstance(), ChangelogTrackerFactory.getInstance());
        assertNotNull(ChangelogTrackerFactory.getInstance().getTracker("DB_CHANGELOG", "0.0.1"));
    }
}
