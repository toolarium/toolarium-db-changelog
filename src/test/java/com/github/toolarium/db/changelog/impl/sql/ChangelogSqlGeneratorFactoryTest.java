/*
 * ChangelogSqlGeneratorFactoryTest.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog.impl.sql;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;


/**
 * Tests for ChangelogSqlGeneratorFactory.
 *
 * @author patrick
 */
public class ChangelogSqlGeneratorFactoryTest {

    /**
     * Test singleton and instance creation.
     */
    @Test
    void testFactory() {
        assertSame(ChangelogSqlGeneratorFactory.getInstance(), ChangelogSqlGeneratorFactory.getInstance());
        assertNotNull(ChangelogSqlGeneratorFactory.getInstance().getSqlGenerator());
    }
}
