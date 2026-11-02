/*
 * OracleChangelogTest.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog;

import javax.sql.DataSource;
import oracle.jdbc.pool.OracleDataSource;
import org.junit.jupiter.api.Tag;
import org.testcontainers.containers.OracleContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;


/**
 * Integration tests against a real Oracle container.
 *
 * @author patrick
 */
@Tag("integration")
@Testcontainers(disabledWithoutDocker = true)
public class OracleChangelogTest extends AbstractChangelogIntegrationTest {

    @Container
    private static final OracleContainer ORACLE = new OracleContainer("gvenzl/oracle-xe:21-slim-faststart");


    /**
     * @see com.github.toolarium.db.changelog.AbstractChangelogIntegrationTest#getDataSource()
     */
    @Override
    protected DataSource getDataSource() {
        try {
            OracleDataSource ds = new OracleDataSource();
            ds.setURL(ORACLE.getJdbcUrl());
            ds.setUser(ORACLE.getUsername());
            ds.setPassword(ORACLE.getPassword());
            return ds;
        } catch (java.sql.SQLException e) {
            throw new RuntimeException("Failed to create Oracle DataSource.", e);
        }
    }
}
