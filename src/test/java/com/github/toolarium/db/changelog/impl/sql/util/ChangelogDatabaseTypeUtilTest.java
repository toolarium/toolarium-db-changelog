/*
 * ChangelogDatabaseTypeUtilTest.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog.impl.sql.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

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


    /**
     * Test column sizes return default (non-Firebird) values for H2.
     */
    @Test
    void testColumnSizesH2() {
        ChangelogDatabaseTypeUtil util = ChangelogDatabaseTypeUtil.getInstance();
        assertEquals(200, util.getIdColumnSize(h2Connection));
        assertEquals(200, util.getAuthorColumnSize(h2Connection));
        assertEquals(350, util.getFilepathColumnSize(h2Connection));
    }


    /**
     * Test total primary key size fits within MySQL 3072 byte limit (4 bytes per char in utf8mb4).
     */
    @Test
    void testPrimaryKeySizeWithinMySQLLimit() {
        ChangelogDatabaseTypeUtil util = ChangelogDatabaseTypeUtil.getInstance();
        int total = util.getIdColumnSize(h2Connection)
                + util.getAuthorColumnSize(h2Connection)
                + util.getFilepathColumnSize(h2Connection);
        assertTrue(total * 4 <= 3072, "PK size " + (total * 4) + " exceeds MySQL 3072 byte limit");
    }
}
