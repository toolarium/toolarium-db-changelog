/*
 * CockroachDBChangelogTest.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog;

import javax.sql.DataSource;
import org.junit.jupiter.api.Tag;
import org.postgresql.ds.PGSimpleDataSource;
import org.testcontainers.containers.CockroachContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;


/**
 * Integration tests against a real CockroachDB container.
 * CockroachDB is PostgreSQL wire-compatible and uses the PostgreSQL JDBC driver.
 *
 * @author patrick
 */
@Tag("integration")
@Testcontainers(disabledWithoutDocker = true)
public class CockroachDBChangelogTest extends AbstractChangelogIntegrationTest {

    @Container
    private static final CockroachContainer COCKROACH = new CockroachContainer("cockroachdb/cockroach:v24.1.0");


    /**
     * @see com.github.toolarium.db.changelog.AbstractChangelogIntegrationTest#getDataSource()
     */
    @Override
    protected DataSource getDataSource() {
        PGSimpleDataSource ds = new PGSimpleDataSource();
        ds.setUrl(COCKROACH.getJdbcUrl());
        ds.setUser(COCKROACH.getUsername());
        ds.setPassword(COCKROACH.getPassword());
        return ds;
    }
}
