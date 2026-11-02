/*
 * ChangelogConfigurationTest.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.Test;


/**
 * Tests for ChangelogConfiguration.
 *
 * @author patrick
 */
public class ChangelogConfigurationTest {
    private static final String TEST_FIELD = "test";

    /**
     * Test default values.
     */
    @Test
    void testDefaults() {
        ChangelogConfiguration config = new ChangelogConfiguration();

        assertEquals("DB_CHANGELOG", config.getTrackingTableName());
        assertEquals("DB_CHANGELOG_LOCK", config.getLockTableName());
        assertFalse(config.isLeaderElectionEnabled());
        assertNotNull(config.getDefaultAuthor());
    }


    /**
     * Test setters and getters.
     */
    @Test
    void testSettersGetters() {
        ChangelogConfiguration config = new ChangelogConfiguration();
        config.setTrackingTableName("CUSTOM_TABLE");
        config.setLockTableName("CUSTOM_LOCK");
        config.setLeaderElectionEnabled(true);
        config.setChangelogPath("db/changelog.db.yaml");
        config.setDefaultAuthor("testuser");
        config.setContexts(Arrays.asList("dev"));
        config.setLabels(Arrays.asList("v1.0"));

        assertEquals("CUSTOM_TABLE", config.getTrackingTableName());
        assertEquals("CUSTOM_LOCK", config.getLockTableName());
        assertTrue(config.isLeaderElectionEnabled());
        assertEquals("db/changelog.db.yaml", config.getChangelogPath());
        assertEquals("testuser", config.getDefaultAuthor());
        assertEquals(1, config.getContexts().size());
        assertEquals(1, config.getLabels().size());
    }


    /**
     * Test validation fails without DataSource.
     */
    @Test
    void testValidationFailsWithoutDataSource() {
        ChangelogConfiguration config = new ChangelogConfiguration();
        assertThrows(IllegalArgumentException.class, () -> config.validate());
    }


    /**
     * Test validation passes with DataSource.
     */
    @Test
    void testValidationPassesWithDataSource() {
        JdbcDataSource ds = new JdbcDataSource();
        ds.setURL("jdbc:h2:mem:test");

        ChangelogConfiguration config = new ChangelogConfiguration();
        config.setDataSource(ds);
        config.validate();
    }


    /**
     * Test toString.
     */
    @Test
    void testToString() {
        ChangelogConfiguration config = new ChangelogConfiguration();
        assertNotNull(config.toString());
        assertTrue(config.toString().contains("DB_CHANGELOG"));
    }


    /**
     * Test identifier validation accepts valid names.
     */
    @Test
    void testValidIdentifiers() {
        ChangelogConfiguration.validateIdentifier("DB_CHANGELOG", TEST_FIELD);
        ChangelogConfiguration.validateIdentifier("my_table", TEST_FIELD);
        ChangelogConfiguration.validateIdentifier("_private", TEST_FIELD);
        ChangelogConfiguration.validateIdentifier("Table123", TEST_FIELD);
    }


    /**
     * Test identifier validation rejects invalid names.
     */
    @Test
    void testInvalidIdentifiers() {
        assertThrows(IllegalArgumentException.class,
                () -> ChangelogConfiguration.validateIdentifier(null, TEST_FIELD));
        assertThrows(IllegalArgumentException.class,
                () -> ChangelogConfiguration.validateIdentifier("", TEST_FIELD));
        assertThrows(IllegalArgumentException.class,
                () -> ChangelogConfiguration.validateIdentifier("123table", TEST_FIELD));
        assertThrows(IllegalArgumentException.class,
                () -> ChangelogConfiguration.validateIdentifier("my table", TEST_FIELD));
        assertThrows(IllegalArgumentException.class,
                () -> ChangelogConfiguration.validateIdentifier("drop;--", TEST_FIELD));
        assertThrows(IllegalArgumentException.class,
                () -> ChangelogConfiguration.validateIdentifier("table.name", TEST_FIELD));
    }


    /**
     * Test validation rejects invalid table names.
     */
    @Test
    void testValidationRejectsInvalidTableName() {
        JdbcDataSource ds = new JdbcDataSource();
        ds.setURL("jdbc:h2:mem:test");

        ChangelogConfiguration config = new ChangelogConfiguration();
        config.setDataSource(ds);
        config.setTrackingTableName("invalid;name");
        assertThrows(IllegalArgumentException.class, () -> config.validate());
    }


    /**
     * Test IDENTIFIER_PATTERN matches expected inputs.
     */
    @Test
    void testIdentifierPattern() {
        assertTrue(ChangelogConfiguration.IDENTIFIER_PATTERN.matcher("DB_CHANGELOG").matches());
        assertTrue(ChangelogConfiguration.IDENTIFIER_PATTERN.matcher("_test").matches());
        assertTrue(ChangelogConfiguration.IDENTIFIER_PATTERN.matcher("a").matches());
        assertFalse(ChangelogConfiguration.IDENTIFIER_PATTERN.matcher("1abc").matches());
        assertFalse(ChangelogConfiguration.IDENTIFIER_PATTERN.matcher("").matches());
        assertFalse(ChangelogConfiguration.IDENTIFIER_PATTERN.matcher("a b").matches());
        assertFalse(ChangelogConfiguration.IDENTIFIER_PATTERN.matcher("a;b").matches());
    }


    /**
     * Test equals and hashCode.
     */
    @Test
    void testEqualsHashCode() {
        ChangelogConfiguration c1 = new ChangelogConfiguration();
        ChangelogConfiguration c2 = new ChangelogConfiguration();

        assertEquals(c1, c2);
        assertEquals(c1.hashCode(), c2.hashCode());
        assertEquals(c1, c1);
    }
}
