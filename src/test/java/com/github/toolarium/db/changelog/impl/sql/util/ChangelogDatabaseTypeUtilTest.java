/*
 * ChangelogDatabaseTypeUtilTest.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog.impl.sql.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.sql.Connection;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


/**
 * Tests for ChangelogDatabaseTypeUtil.
 *
 * @author patrick
 */
public class ChangelogDatabaseTypeUtilTest {
    private Connection h2Connection;


    /**
     * Set up an H2 connection for testing.
     *
     * @throws Exception on error
     */
    @BeforeEach
    void setUp() throws Exception {
        JdbcDataSource ds = new JdbcDataSource();
        ds.setURL("jdbc:h2:mem:typeutil_" + System.nanoTime() + ";DB_CLOSE_DELAY=-1");
        ds.setUser("sa");
        ds.setPassword("");
        h2Connection = ds.getConnection();
    }


    /**
     * Test singleton instance.
     */
    @Test
    void testSingleton() {
        assertSame(ChangelogDatabaseTypeUtil.getInstance(), ChangelogDatabaseTypeUtil.getInstance());
        assertNotNull(ChangelogDatabaseTypeUtil.getInstance());
    }


    /**
     * Test getDateTimeType returns TIMESTAMP for H2 (not SQL Server).
     */
    @Test
    void testDateTimeTypeH2() {
        assertEquals("TIMESTAMP", ChangelogDatabaseTypeUtil.getInstance().getDateTimeType(h2Connection));
    }


    /**
     * Test getClobType returns CLOB for H2 (not MySQL/MariaDB).
     */
    @Test
    void testClobTypeH2() {
        assertEquals("CLOB", ChangelogDatabaseTypeUtil.getInstance().getClobType(h2Connection));
    }
}
