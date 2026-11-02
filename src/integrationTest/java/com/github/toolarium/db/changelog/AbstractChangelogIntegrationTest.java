/*
 * AbstractChangelogIntegrationTest.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.toolarium.db.changelog.impl.ChangelogRunnerImpl;
import com.github.toolarium.db.changelog.model.Changelog;
import com.github.toolarium.db.changelog.model.ChangelogChangeSet;
import com.github.toolarium.db.changelog.model.ChangelogChangeSetIdentity;
import com.github.toolarium.db.changelog.model.IChangelogEntry;
import com.github.toolarium.db.changelog.model.IChangelogTrackingEntry;
import com.github.toolarium.db.changelog.model.column.ChangelogColumnDefinition;
import com.github.toolarium.db.changelog.model.sql.SqlChangelogEntry;
import com.github.toolarium.db.changelog.model.table.CreateTableChangelogEntry;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;


/**
 * Abstract base test for database-specific integration tests.
 * Subclasses provide the DataSource via {@link #getDataSource()}.
 * All test logic is inherited: update, rollback, sync, validate, tag, history, DDL execution.
 *
 * @author patrick
 */
public abstract class AbstractChangelogIntegrationTest {
    private static final String FILEPATH = "integration-test.db.yaml";
    private static final String AUTHOR = "integration";
    private static final String RUN_ID = String.valueOf(System.nanoTime() % 100000);
    private static final java.util.concurrent.atomic.AtomicInteger TABLE_COUNTER = new java.util.concurrent.atomic.AtomicInteger();


    /**
     * Get the DataSource for this database.
     *
     * @return the data source
     */
    protected abstract DataSource getDataSource();


    /**
     * Test full update and history cycle.
     *
     * @throws Exception on error
     */
    @Test
    void testUpdateAndHistory() throws Exception {
        IChangelogRunner runner = createRunner(
                sqlChangeSet("it-1", "CREATE TABLE it_update_test (id INT NOT NULL PRIMARY KEY, name VARCHAR(100))"),
                sqlChangeSet("it-2", "INSERT INTO it_update_test (id, name) VALUES (1, 'Integration')")
        );

        runner.update();

        List<IChangelogTrackingEntry> history = runner.history();
        assertEquals(2, history.size());
        assertEquals("it-1", history.get(0).getId());
        assertEquals("it-2", history.get(1).getId());

        // Verify data
        try (Connection conn = getDataSource().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT name FROM it_update_test WHERE id = 1")) {
            assertTrue(rs.next());
            assertEquals("Integration", rs.getString("name"));
        }
    }


    /**
     * Test update skips already applied changesets.
     *
     * @throws Exception on error
     */
    @Test
    void testUpdateIdempotent() throws Exception {
        IChangelogRunner runner = createRunner(
                sqlChangeSet("it-idem-1", "CREATE TABLE it_idem_test (id INT NOT NULL PRIMARY KEY)")
        );

        runner.update();
        runner.update();

        assertEquals(1, runner.history().size());
    }


    /**
     * Test rollback by count.
     *
     * @throws Exception on error
     */
    @Test
    void testRollbackByCount() throws Exception {
        IChangelogRunner runner = createRunner(
                sqlChangeSetWithRollback("it-rb-1",
                        "CREATE TABLE it_rb_test (id INT NOT NULL PRIMARY KEY)",
                        "DROP TABLE it_rb_test")
        );

        runner.update();
        assertEquals(1, runner.history().size());

        runner.rollback(1);
        assertEquals(0, runner.history().size());
    }


    /**
     * Test rollback to tag.
     *
     * @throws Exception on error
     */
    @Test
    void testRollbackToTag() throws Exception {
        IChangelogRunner runner = createRunner(
                sqlChangeSetWithRollback("it-tag-1",
                        "CREATE TABLE it_tag_test (id INT NOT NULL PRIMARY KEY)",
                        "DROP TABLE it_tag_test"),
                sqlChangeSetWithRollback("it-tag-2",
                        "CREATE TABLE it_tag_extra (id INT NOT NULL PRIMARY KEY)",
                        "DROP TABLE it_tag_extra")
        );

        runner.update(1);
        runner.tag("v1.0");
        runner.update();

        assertEquals(2, runner.history().size());

        runner.rollback("v1.0");
        assertEquals(1, runner.history().size());
    }


    /**
     * Test sync marks all as executed without running.
     *
     * @throws Exception on error
     */
    @Test
    void testSync() throws Exception {
        IChangelogRunner runner = createRunner(
                sqlChangeSet("it-sync-1", "CREATE TABLE it_sync_test (id INT NOT NULL PRIMARY KEY)")
        );

        runner.sync();

        List<IChangelogTrackingEntry> history = runner.history();
        assertEquals(1, history.size());
        assertEquals("MARK_RAN", history.get(0).getExecutionType().name());
    }


    /**
     * Test validate passes with matching checksums.
     *
     * @throws Exception on error
     */
    @Test
    void testValidate() throws Exception {
        IChangelogRunner runner = createRunner(
                sqlChangeSet("it-val-1", "CREATE TABLE it_val_test (id INT NOT NULL PRIMARY KEY)")
        );

        runner.update();
        runner.validate();
    }


    /**
     * Test clearChecksums and re-execution.
     *
     * @throws Exception on error
     */
    @Test
    void testClearChecksums() throws Exception {
        IChangelogRunner runner = createRunner(
                sqlChangeSet("it-clear-1", "CREATE TABLE it_clear_test (id INT NOT NULL PRIMARY KEY)")
        );

        runner.update();
        runner.clearChecksums();

        List<IChangelogTrackingEntry> history = runner.history();
        assertEquals(1, history.size());
        assertTrue(history.get(0).getChecksum() == null || history.get(0).getChecksum().isEmpty());
    }


    /**
     * Test generateRollbackSql dry-run.
     *
     * @throws Exception on error
     */
    @Test
    void testGenerateRollbackSql() throws Exception {
        IChangelogRunner runner = createRunner(
                sqlChangeSetWithRollback("it-dry-1",
                        "CREATE TABLE it_dry_test (id INT NOT NULL PRIMARY KEY)",
                        "DROP TABLE it_dry_test")
        );

        runner.update();

        List<String> rollbackSql = runner.generateRollbackSql(1);
        assertFalse(rollbackSql.isEmpty());
        assertTrue(rollbackSql.get(0).contains("DROP TABLE"));
    }


    /**
     * Test structured DDL change type (createTable) execution.
     *
     * @throws Exception on error
     */
    @Test
    void testDdlCreateTable() throws Exception {
        ChangelogColumnDefinition idCol = new ChangelogColumnDefinition("id", "INT");
        idCol.setPrimaryKey(true).setNullable(false);
        ChangelogColumnDefinition nameCol = new ChangelogColumnDefinition("name", "VARCHAR(100)");

        CreateTableChangelogEntry createTable = new CreateTableChangelogEntry(
                "it_ddl_test", Arrays.asList(idCol, nameCol));

        ChangelogChangeSetIdentity identity = new ChangelogChangeSetIdentity("it-ddl-1", AUTHOR, FILEPATH);
        ChangelogChangeSet cs = new ChangelogChangeSet(identity, Collections.singletonList(createTable));

        IChangelogRunner runner = createRunner(cs);
        runner.update();

        // Verify table exists by inserting data
        try (Connection conn = getDataSource().getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("INSERT INTO it_ddl_test (id, name) VALUES (1, 'DDL Test')");
        }

        assertEquals(1, runner.history().size());
    }


    /**
     * Test deployment ID grouping.
     *
     * @throws Exception on error
     */
    @Test
    void testDeploymentIdGrouping() throws Exception {
        IChangelogRunner runner = createRunner(
                sqlChangeSet("it-deploy-1", "CREATE TABLE it_deploy_test1 (id INT NOT NULL PRIMARY KEY)"),
                sqlChangeSet("it-deploy-2", "CREATE TABLE it_deploy_test2 (id INT NOT NULL PRIMARY KEY)")
        );

        runner.update();

        List<IChangelogTrackingEntry> history = runner.history();
        assertEquals(2, history.size());
        assertNotNull(history.get(0).getDeploymentId());
        assertEquals(history.get(0).getDeploymentId(), history.get(1).getDeploymentId());
    }


    /**
     * Create a runner with programmatic changesets.
     *
     * @param changeSets the changesets
     * @return the runner
     */
    private IChangelogRunner createRunner(ChangelogChangeSet... changeSets) {
        String trackingTable = "DBCL_" + RUN_ID + "_" + TABLE_COUNTER.incrementAndGet();
        ChangelogRunnerImpl runner = (ChangelogRunnerImpl) ChangelogRunnerBuilder.builder()
                .dataSource(getDataSource())
                .trackingTable(trackingTable)
                .build();
        runner.setChangelog(new Changelog(FILEPATH, Arrays.asList(changeSets)));
        return runner;
    }


    /**
     * Create a SQL changeset without rollback.
     *
     * @param id the changeset id
     * @param sql the SQL
     * @return the changeset
     */
    private ChangelogChangeSet sqlChangeSet(String id, String sql) {
        ChangelogChangeSetIdentity identity = new ChangelogChangeSetIdentity(id, AUTHOR, FILEPATH);
        return new ChangelogChangeSet(identity, Collections.singletonList(new SqlChangelogEntry(sql)));
    }


    /**
     * Create a SQL changeset with manual rollback.
     *
     * @param id the changeset id
     * @param sql the forward SQL
     * @param rollbackSql the rollback SQL
     * @return the changeset
     */
    private ChangelogChangeSet sqlChangeSetWithRollback(String id, String sql, String rollbackSql) {
        ChangelogChangeSetIdentity identity = new ChangelogChangeSetIdentity(id, AUTHOR, FILEPATH);
        List<IChangelogEntry> entries = Collections.singletonList(new SqlChangelogEntry(sql));
        List<IChangelogEntry> rollback = Collections.singletonList(new SqlChangelogEntry(rollbackSql));
        return new ChangelogChangeSet(identity, entries, rollback, null,
                false, false, true, true, Collections.emptyList(), Collections.emptyList(), null);
    }
}
