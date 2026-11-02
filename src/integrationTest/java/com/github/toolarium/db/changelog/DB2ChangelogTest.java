/*
 * DB2ChangelogTest.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog;

import com.ibm.db2.jcc.DB2SimpleDataSource;
import javax.sql.DataSource;
import org.junit.jupiter.api.Tag;
import org.testcontainers.containers.Db2Container;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;


/**
 * Integration tests against a real DB2 container.
 * Note: DB2 container is large and slow to start (2-5 minutes).
 *
 * @author patrick
 */
@Tag("integration")
@Testcontainers(disabledWithoutDocker = true)
public class DB2ChangelogTest extends AbstractChangelogIntegrationTest {

    @Container
    private static final Db2Container DB2 = new Db2Container("icr.io/db2_community/db2:11.5.9.0")
            .acceptLicense();


    /**
     * @see com.github.toolarium.db.changelog.AbstractChangelogIntegrationTest#getDataSource()
     */
    @Override
    protected DataSource getDataSource() {
        DB2SimpleDataSource ds = new DB2SimpleDataSource();
        ds.setServerName(DB2.getHost());
        ds.setPortNumber(DB2.getMappedPort(50000));
        ds.setDatabaseName(DB2.getDatabaseName());
        ds.setUser(DB2.getUsername());
        ds.setPassword(DB2.getPassword());
        return ds;
    }
}
