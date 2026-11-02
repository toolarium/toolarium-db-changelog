/*
 * MariaDBChangelogTest.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog;

import javax.sql.DataSource;
import org.junit.jupiter.api.Tag;
import org.mariadb.jdbc.MariaDbDataSource;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;


/**
 * Integration tests against a real MariaDB container.
 *
 * @author patrick
 */
@Tag("integration")
@Testcontainers(disabledWithoutDocker = true)
public class MariaDBChangelogTest extends AbstractChangelogIntegrationTest {

    @Container
    private static final MariaDBContainer<?> MARIADB = new MariaDBContainer<>("mariadb:11");


    /**
     * @see com.github.toolarium.db.changelog.AbstractChangelogIntegrationTest#getDataSource()
     */
    @Override
    protected DataSource getDataSource() {
        try {
            MariaDbDataSource ds = new MariaDbDataSource();
            ds.setUrl(MARIADB.getJdbcUrl());
            ds.setUser(MARIADB.getUsername());
            ds.setPassword(MARIADB.getPassword());
            return ds;
        } catch (java.sql.SQLException e) {
            throw new RuntimeException("Failed to create MariaDB DataSource.", e);
        }
    }
}
