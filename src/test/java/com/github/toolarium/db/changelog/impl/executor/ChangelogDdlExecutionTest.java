/*
 * ChangelogDdlExecutionTest.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog.impl.executor;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.toolarium.db.changelog.ChangelogRunnerBuilder;
import com.github.toolarium.db.changelog.IChangelogRunner;
import com.github.toolarium.db.changelog.dto.Changelog;
import com.github.toolarium.db.changelog.dto.ChangelogChangeSet;
import com.github.toolarium.db.changelog.dto.ChangelogChangeSetIdentity;
import com.github.toolarium.db.changelog.dto.IChangelogEntry;
import com.github.toolarium.db.changelog.dto.IChangelogTrackingEntry;
import com.github.toolarium.db.changelog.dto.column.AddColumnChangelogEntry;
import com.github.toolarium.db.changelog.dto.column.ChangelogColumnDefinition;
import com.github.toolarium.db.changelog.dto.index.CreateIndexChangelogEntry;
import com.github.toolarium.db.changelog.dto.table.CreateTableChangelogEntry;
import com.github.toolarium.db.changelog.impl.ChangelogRunnerImpl;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


/**
 * Tests for executing structured DDL change types end-to-end.
 *
 * @author patrick
 */
public class ChangelogDdlExecutionTest {
    private static final String FILEPATH = "test.db.yaml";
    private static final String AUTHOR = "dev";
    private JdbcDataSource dataSource;


    /**
     * Set up.
     */
    @BeforeEach
    void setUp() {
        dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:ddl_" + System.nanoTime() + ";DB_CLOSE_DELAY=-1");
        dataSource.setUser("sa");
        dataSource.setPassword("");
    }


    /**
     * Test CreateTableChangelogEntry execution.
     *
     * @throws Exception on error
     */
    @Test
    void testCreateTableExecution() throws Exception {
        ChangelogColumnDefinition idCol = new ChangelogColumnDefinition("id", "INT");
        idCol.setPrimaryKey(true).setNullable(false);
        ChangelogColumnDefinition nameCol = new ChangelogColumnDefinition("name", "VARCHAR(100)");

        CreateTableChangelogEntry createTable = new CreateTableChangelogEntry("products",
                Arrays.asList(idCol, nameCol));

        IChangelogRunner runner = createRunner(
                changeSet("create-products", createTable));
        runner.update();

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM products")) {
            assertTrue(rs.next());
            assertEquals(0, rs.getInt(1));
        }

        List<IChangelogTrackingEntry> history = runner.history();
        assertEquals(1, history.size());
        assertTrue(history.get(0).getDescription().contains("createTable"));
    }


    /**
     * Test AddColumnChangelogEntry execution.
     *
     * @throws Exception on error
     */
    @Test
    void testAddColumnExecution() throws Exception {
        ChangelogColumnDefinition idCol = new ChangelogColumnDefinition("id", "INT");
        idCol.setPrimaryKey(true).setNullable(false);

        CreateTableChangelogEntry createTable = new CreateTableChangelogEntry("items",
                Collections.singletonList(idCol));

        ChangelogColumnDefinition priceCol = new ChangelogColumnDefinition("price", "DECIMAL(10,2)");
        AddColumnChangelogEntry addColumn = new AddColumnChangelogEntry("items",
                Collections.singletonList(priceCol));

        IChangelogRunner runner = createRunner(
                changeSet("create-items", createTable),
                changeSet("add-price", addColumn));
        runner.update();

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("INSERT INTO items (id, price) VALUES (1, 9.99)");
            ResultSet rs = stmt.executeQuery("SELECT price FROM items WHERE id = 1");
            assertTrue(rs.next());
            assertEquals(9.99, rs.getDouble(1), 0.01);
        }
    }


    /**
     * Test CreateIndexChangelogEntry execution.
     *
     * @throws Exception on error
     */
    @Test
    void testCreateIndexExecution() throws Exception {
        ChangelogColumnDefinition idCol = new ChangelogColumnDefinition("id", "INT");
        idCol.setPrimaryKey(true).setNullable(false);
        ChangelogColumnDefinition emailCol = new ChangelogColumnDefinition("email", "VARCHAR(255)");

        CreateTableChangelogEntry createTable = new CreateTableChangelogEntry("contacts",
                Arrays.asList(idCol, emailCol));

        CreateIndexChangelogEntry createIndex = new CreateIndexChangelogEntry("contacts",
                "idx_contacts_email", Collections.singletonList("email"), false);

        IChangelogRunner runner = createRunner(
                changeSet("create-contacts", createTable),
                changeSet("create-idx", createIndex));
        runner.update();

        List<IChangelogTrackingEntry> history = runner.history();
        assertEquals(2, history.size());
        assertTrue(history.get(1).getDescription().contains("createIndex"));
    }


    /**
     * Create a runner with programmatic changesets.
     *
     * @param changeSets the changesets
     * @return the runner
     */
    private IChangelogRunner createRunner(ChangelogChangeSet... changeSets) {
        ChangelogRunnerImpl runner = (ChangelogRunnerImpl) ChangelogRunnerBuilder.builder()
                .dataSource(dataSource).build();
        runner.setChangelog(new Changelog(FILEPATH, Arrays.asList(changeSets)));
        return runner;
    }


    /**
     * Create a changeset with a single entry.
     *
     * @param id the changeset id
     * @param entry the entry
     * @return the changeset
     */
    private ChangelogChangeSet changeSet(String id, IChangelogEntry entry) {
        ChangelogChangeSetIdentity identity = new ChangelogChangeSetIdentity(id, AUTHOR, FILEPATH);
        return new ChangelogChangeSet(identity, Collections.singletonList(entry));
    }
}
