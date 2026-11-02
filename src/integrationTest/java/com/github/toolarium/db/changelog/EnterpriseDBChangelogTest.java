/*
 * EnterpriseDBChangelogTest.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog;

import javax.sql.DataSource;
import org.junit.jupiter.api.Tag;
import org.postgresql.ds.PGSimpleDataSource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;


/**
 * Integration tests against an EnterpriseDB (EDB Postgres Advanced) container.
 * EnterpriseDB is PostgreSQL-compatible and uses the PostgreSQL JDBC driver.
 *
 * @author patrick
 */
@Tag("integration")
@Testcontainers(disabledWithoutDocker = true)
public class EnterpriseDBChangelogTest extends AbstractChangelogIntegrationTest {
    private static final int PG_PORT = 5444;

    @Container
    private static final GenericContainer<?> EDB = new GenericContainer<>("quay.io/enterprisedb/edb-postgres-advanced:16")
            .withExposedPorts(PG_PORT)
            .withEnv("POSTGRES_USER", "enterprisedb")
            .withEnv("POSTGRES_PASSWORD", "testpass")
            .withEnv("POSTGRES_DB", "testdb")
            .waitingFor(Wait.forLogMessage(".*database system is ready to accept connections.*\\n", 2));


    /**
     * @see com.github.toolarium.db.changelog.AbstractChangelogIntegrationTest#getDataSource()
     */
    @Override
    protected DataSource getDataSource() {
        PGSimpleDataSource ds = new PGSimpleDataSource();
        ds.setServerNames(new String[]{EDB.getHost()});
        ds.setPortNumbers(new int[]{EDB.getMappedPort(PG_PORT)});
        ds.setDatabaseName("testdb");
        ds.setUser("enterprisedb");
        ds.setPassword("testpass");
        return ds;
    }
}
