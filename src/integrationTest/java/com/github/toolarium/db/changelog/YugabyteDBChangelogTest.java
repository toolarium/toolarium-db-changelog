/*
 * YugabyteDBChangelogTest.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog;

import javax.sql.DataSource;
import org.junit.jupiter.api.Tag;
import org.postgresql.ds.PGSimpleDataSource;
import org.testcontainers.containers.YugabyteDBYSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;


/**
 * Integration tests against a real YugabyteDB container.
 * YugabyteDB is PostgreSQL wire-compatible and uses the PostgreSQL JDBC driver.
 *
 * @author patrick
 */
@Tag("integration")
@Testcontainers(disabledWithoutDocker = true)
public class YugabyteDBChangelogTest extends AbstractChangelogIntegrationTest {

    @Container
    private static final YugabyteDBYSQLContainer YUGABYTE = new YugabyteDBYSQLContainer("yugabytedb/yugabyte:2024.1.3.0-b105");


    /**
     * @see com.github.toolarium.db.changelog.AbstractChangelogIntegrationTest#getDataSource()
     */
    @Override
    protected DataSource getDataSource() {
        PGSimpleDataSource ds = new PGSimpleDataSource();
        ds.setUrl(YUGABYTE.getJdbcUrl());
        ds.setUser(YUGABYTE.getUsername());
        ds.setPassword(YUGABYTE.getPassword());
        return ds;
    }
}
