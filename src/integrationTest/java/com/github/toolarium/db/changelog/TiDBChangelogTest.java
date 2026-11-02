/*
 * TiDBChangelogTest.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog;

import com.mysql.cj.jdbc.MysqlDataSource;
import javax.sql.DataSource;
import org.junit.jupiter.api.Tag;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.tidb.TiDBContainer;


/**
 * Integration tests against a real TiDB container.
 * TiDB is MySQL wire-compatible and uses the MySQL JDBC driver.
 *
 * @author patrick
 */
@Tag("integration")
@Testcontainers(disabledWithoutDocker = true)
public class TiDBChangelogTest extends AbstractChangelogIntegrationTest {

    @Container
    private static final TiDBContainer TIDB = new TiDBContainer("pingcap/tidb:v7.5.0");


    /**
     * @see com.github.toolarium.db.changelog.AbstractChangelogIntegrationTest#getDataSource()
     */
    @Override
    protected DataSource getDataSource() {
        MysqlDataSource ds = new MysqlDataSource();
        ds.setUrl(TIDB.getJdbcUrl());
        ds.setUser(TIDB.getUsername());
        ds.setPassword(TIDB.getPassword());
        return ds;
    }
}
