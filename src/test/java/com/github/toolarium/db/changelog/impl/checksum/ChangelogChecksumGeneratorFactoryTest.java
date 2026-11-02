/*
 * ChangelogChecksumGeneratorFactoryTest.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog.impl.checksum;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;


/**
 * Tests for ChangelogChecksumGeneratorFactory.
 *
 * @author patrick
 */
public class ChangelogChecksumGeneratorFactoryTest {

    /**
     * Test singleton and instance creation.
     */
    @Test
    void testFactory() {
        assertSame(ChangelogChecksumGeneratorFactory.getInstance(), ChangelogChecksumGeneratorFactory.getInstance());
        assertNotNull(ChangelogChecksumGeneratorFactory.getInstance().getChecksumGenerator());
    }
}
