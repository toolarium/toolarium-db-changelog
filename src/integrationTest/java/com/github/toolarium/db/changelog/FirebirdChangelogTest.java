/*
 * FirebirdChangelogTest.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog;

import javax.sql.DataSource;
import org.firebirdsql.ds.FBSimpleDataSource;
import org.junit.jupiter.api.Tag;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;


/**
 * Integration tests against a real Firebird container.
 *
 * @author patrick
 */
@Tag("integration")
@Testcontainers(disabledWithoutDocker = true)
public class FirebirdChangelogTest extends AbstractChangelogIntegrationTest {
    private static final int FIREBIRD_PORT = 3050;

    @Container
    private static final GenericContainer<?> FIREBIRD = new GenericContainer<>("jacobalberty/firebird:v4.0")
            .withExposedPorts(FIREBIRD_PORT)
            .withEnv("FIREBIRD_DATABASE", "test.fdb")
            .withEnv("FIREBIRD_USER", "testuser")
            .withEnv("FIREBIRD_PASSWORD", "testpass")
            .withEnv("ISC_PASSWORD", "masterkey");


    /**
     * @see com.github.toolarium.db.changelog.AbstractChangelogIntegrationTest#getDataSource()
     */
    @Override
    protected DataSource getDataSource() {
        FBSimpleDataSource ds = new FBSimpleDataSource();
        ds.setServerName(FIREBIRD.getHost());
        ds.setPortNumber(FIREBIRD.getMappedPort(FIREBIRD_PORT));
        ds.setDatabaseName("test.fdb");
        ds.setUserName("testuser");
        ds.setPassword("testpass");
        ds.setCharSet("UTF8");
        return ds;
    }
}
