/*
 * SQLServerChangelogTest.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog;

import com.microsoft.sqlserver.jdbc.SQLServerDataSource;
import javax.sql.DataSource;
import org.junit.jupiter.api.Tag;
import org.testcontainers.containers.MSSQLServerContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;


/**
 * Integration tests against a real SQL Server container.
 *
 * @author patrick
 */
@Tag("integration")
@Testcontainers(disabledWithoutDocker = true)
public class SQLServerChangelogTest extends AbstractChangelogIntegrationTest {

    @Container
    private static final MSSQLServerContainer<?> MSSQL = new MSSQLServerContainer<>(
            "mcr.microsoft.com/mssql/server:2022-latest").acceptLicense();


    /**
     * @see com.github.toolarium.db.changelog.AbstractChangelogIntegrationTest#getDataSource()
     */
    @Override
    protected DataSource getDataSource() {
        SQLServerDataSource ds = new SQLServerDataSource();
        ds.setURL(MSSQL.getJdbcUrl());
        ds.setUser(MSSQL.getUsername());
        ds.setPassword(MSSQL.getPassword());
        return ds;
    }
}
