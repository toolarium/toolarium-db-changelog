/*
 * SQLiteChangelogTest.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog;

import java.io.File;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.io.TempDir;
import org.sqlite.SQLiteDataSource;


/**
 * Integration tests against an embedded SQLite database.
 * Uses a file-based database because SQLite in-memory databases are not shared across connections.
 *
 * @author patrick
 */
@Tag("integration")
public class SQLiteChangelogTest extends AbstractChangelogIntegrationTest {
    private SQLiteDataSource dataSource;

    @TempDir
    File tempDir;


    /**
     * Set up a fresh SQLite file database for each test.
     */
    @BeforeEach
    void setUp() {
        dataSource = new SQLiteDataSource();
        dataSource.setUrl("jdbc:sqlite:" + new File(tempDir, "test_" + System.nanoTime() + ".db").getAbsolutePath());
    }


    /**
     * @see com.github.toolarium.db.changelog.AbstractChangelogIntegrationTest#getDataSource()
     */
    @Override
    protected DataSource getDataSource() {
        return dataSource;
    }
}
