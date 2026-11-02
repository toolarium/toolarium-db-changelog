/*
 * SQLiteChangelogTest.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog;

import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.sqlite.SQLiteDataSource;


/**
 * Integration tests against an embedded SQLite database.
 *
 * @author patrick
 */
@Tag("integration")
public class SQLiteChangelogTest extends AbstractChangelogIntegrationTest {
    private SQLiteDataSource dataSource;


    /**
     * Set up a fresh SQLite in-memory database for each test.
     */
    @BeforeEach
    void setUp() {
        dataSource = new SQLiteDataSource();
        dataSource.setUrl("jdbc:sqlite::memory:");
    }


    /**
     * @see com.github.toolarium.db.changelog.AbstractChangelogIntegrationTest#getDataSource()
     */
    @Override
    protected DataSource getDataSource() {
        return dataSource;
    }
}
