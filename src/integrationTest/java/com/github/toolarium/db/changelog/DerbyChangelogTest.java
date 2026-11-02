/*
 * DerbyChangelogTest.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog;

import java.io.OutputStream;
import javax.sql.DataSource;
import org.apache.derby.jdbc.EmbeddedDataSource;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;


/**
 * Integration tests against an embedded Derby database.
 *
 * @author patrick
 */
@Tag("integration")
public class DerbyChangelogTest extends AbstractChangelogIntegrationTest {
    /** Discard stream for Derby log output. */
    public static final OutputStream DEV_NULL = OutputStream.nullOutputStream();

    private EmbeddedDataSource dataSource;


    /**
     * Suppress derby.log file creation.
     */
    @BeforeAll
    static void suppressDerbyLog() {
        System.setProperty("derby.stream.error.field", DerbyChangelogTest.class.getName() + ".DEV_NULL");
    }


    /**
     * Set up a fresh Derby in-memory database for each test.
     */
    @BeforeEach
    void setUp() {
        dataSource = new EmbeddedDataSource();
        dataSource.setDatabaseName("memory:it_derby_" + System.nanoTime());
        dataSource.setCreateDatabase("create");
    }


    /**
     * @see com.github.toolarium.db.changelog.AbstractChangelogIntegrationTest#getDataSource()
     */
    @Override
    protected DataSource getDataSource() {
        return dataSource;
    }
}
