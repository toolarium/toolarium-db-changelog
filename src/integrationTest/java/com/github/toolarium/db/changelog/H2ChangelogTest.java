/*
 * H2ChangelogTest.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog;

import javax.sql.DataSource;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;


/**
 * Integration tests against an embedded H2 database.
 *
 * @author patrick
 */
@Tag("integration")
public class H2ChangelogTest extends AbstractChangelogIntegrationTest {
    private JdbcDataSource dataSource;


    /**
     * Set up a fresh H2 in-memory database for each test.
     */
    @BeforeEach
    void setUp() {
        dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:it_h2_" + System.nanoTime() + ";DB_CLOSE_DELAY=-1");
        dataSource.setUser("sa");
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
