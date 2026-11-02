/*
 * HSQLDBChangelogTest.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog;

import javax.sql.DataSource;
import org.hsqldb.jdbc.JDBCDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;


/**
 * Integration tests against an embedded HSQLDB database.
 *
 * @author patrick
 */
@Tag("integration")
public class HSQLDBChangelogTest extends AbstractChangelogIntegrationTest {
    private JDBCDataSource dataSource;


    /**
     * Set up a fresh HSQLDB in-memory database for each test.
     */
    @BeforeEach
    void setUp() {
        dataSource = new JDBCDataSource();
        dataSource.setUrl("jdbc:hsqldb:mem:it_hsqldb_" + System.nanoTime());
        dataSource.setUser("SA");
        dataSource.setPassword("");
    }


    /**
     * @see com.github.toolarium.db.changelog.AbstractChangelogIntegrationTest#getDataSource()
     */
    @Override
    protected DataSource getDataSource() {
        return dataSource;
    }
}
