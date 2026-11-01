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
