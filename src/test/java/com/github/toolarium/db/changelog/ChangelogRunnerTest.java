/*
 * ChangelogRunnerTest.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.toolarium.db.changelog.impl.ChangelogRunnerImpl;
import com.github.toolarium.db.changelog.model.Changelog;
import com.github.toolarium.db.changelog.model.ChangelogChangeSet;
import com.github.toolarium.db.changelog.model.ChangelogChangeSetIdentity;
import com.github.toolarium.db.changelog.model.ChangelogExecutionType;
import com.github.toolarium.db.changelog.model.IChangelogEntry;
import com.github.toolarium.db.changelog.model.IChangelogTrackingEntry;
import com.github.toolarium.db.changelog.model.sql.SqlChangelogEntry;
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
 * Tests for the core changelog runner with H2.
 *
 * @author patrick
 */
public class ChangelogRunnerTest {
    private static final String ID_1 = "1";
    private static final String ID_2 = "2";
    private static final String ID_3 = "3";
    private JdbcDataSource dataSource;

    /**
     * Set up a fresh H2 in-memory database for each test.
     */
    @BeforeEach
    void setUp() {
        dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:test_" + System.nanoTime() + ";DB_CLOSE_DELAY=-1");
        dataSource.setUser("sa");
        dataSource.setPassword("");
    }

    /**
     * Test that update creates the tracking table and executes changesets.
     *
     * @throws Exception on error
     */
    @Test
    void testUpdateCreatesTableAndExecutes() throws Exception {
        IChangelogRunner runner = createRunner(
            changeSet(ID_1, "CREATE TABLE person (id INT PRIMARY KEY, name VARCHAR(100))"),
            changeSet(ID_2, "INSERT INTO person (id, name) VALUES (1, 'Alice')")
        );

        runner.update();

        // Verify the person table was created and data inserted
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT name FROM person WHERE id = 1")) {
            assertTrue(rs.next(), "Should have one row");
            assertEquals("Alice", rs.getString("name"));
        }

        // Verify tracking table has two entries
        List<IChangelogTrackingEntry> history = runner.history();
        assertEquals(2, history.size());
        assertEquals(ID_1, history.get(0).getId());
        assertEquals(ID_2, history.get(1).getId());
        assertEquals(ChangelogExecutionType.EXECUTED, history.get(0).getExecutionType());
        assertEquals(ChangelogExecutionType.EXECUTED, history.get(1).getExecutionType());
    }

    /**
     * Test that running update twice skips already applied changesets.
     *
     * @throws Exception on error
     */
    @Test
    void testUpdateSkipsAlreadyApplied() throws Exception {
        IChangelogRunner runner = createRunner(
            changeSet(ID_1, "CREATE TABLE test_table (id INT PRIMARY KEY)")
        );

        runner.update();
        runner.update(); // second run should skip

        List<IChangelogTrackingEntry> history = runner.history();
        assertEquals(1, history.size());
    }

    /**
     * Test update with count limit.
     *
     * @throws Exception on error
     */
    @Test
    void testUpdateWithCount() throws Exception {
        IChangelogRunner runner = createRunner(
            changeSet(ID_1, "CREATE TABLE t1 (id INT PRIMARY KEY)"),
            changeSet(ID_2, "CREATE TABLE t2 (id INT PRIMARY KEY)"),
            changeSet(ID_3, "CREATE TABLE t3 (id INT PRIMARY KEY)")
        );

        runner.update(2);

        List<IChangelogTrackingEntry> history = runner.history();
        assertEquals(2, history.size());
        assertEquals(ID_1, history.get(0).getId());
        assertEquals(ID_2, history.get(1).getId());
    }

    /**
     * Test tagging.
     *
     * @throws Exception on error
     */
    @Test
    void testTag() throws Exception {
        IChangelogRunner runner = createRunner(
            changeSet(ID_1, "CREATE TABLE tagged_table (id INT PRIMARY KEY)")
        );

        runner.update();
        runner.tag("v1.0");

        List<IChangelogTrackingEntry> history = runner.history();
        assertEquals(1, history.size());
        assertEquals("v1.0", history.get(0).getTag());
    }

    /**
     * Test checksum generation and storage.
     *
     * @throws Exception on error
     */
    @Test
    void testChecksumIsStored() throws Exception {
        IChangelogRunner runner = createRunner(
            changeSet(ID_1, "CREATE TABLE checksum_test (id INT PRIMARY KEY)")
        );

        runner.update();

        List<IChangelogTrackingEntry> history = runner.history();
        assertNotNull(history.get(0).getChecksum());
        assertEquals(64, history.get(0).getChecksum().length(), "SHA-256 hex should be 64 chars");
    }

    /**
     * Test clearChecksums.
     *
     * @throws Exception on error
     */
    @Test
    void testClearChecksums() throws Exception {
        IChangelogRunner runner = createRunner(
            changeSet(ID_1, "CREATE TABLE clear_test (id INT PRIMARY KEY)")
        );

        runner.update();
        runner.clearChecksums();

        List<IChangelogTrackingEntry> history = runner.history();
        assertEquals(1, history.size());
        assertTrue(history.get(0).getChecksum() == null || history.get(0).getChecksum().isEmpty(),
                "Checksum should be cleared");
    }

    /**
     * Test deployment id groups changesets from same run.
     *
     * @throws Exception on error
     */
    @Test
    void testDeploymentIdGrouping() throws Exception {
        IChangelogRunner runner = createRunner(
            changeSet(ID_1, "CREATE TABLE deploy_test1 (id INT PRIMARY KEY)"),
            changeSet(ID_2, "CREATE TABLE deploy_test2 (id INT PRIMARY KEY)")
        );

        runner.update();

        List<IChangelogTrackingEntry> history = runner.history();
        assertEquals(2, history.size());
        assertNotNull(history.get(0).getDeploymentId());
        assertEquals(history.get(0).getDeploymentId(), history.get(1).getDeploymentId(),
                "Changesets from same run should share deployment id");
    }

    /**
     * Test sync marks all pending changesets as executed.
     *
     * @throws Exception on error
     */
    @Test
    void testSync() throws Exception {
        IChangelogRunner runner = createRunner(
            changeSet(ID_1, "CREATE TABLE sync_test (id INT PRIMARY KEY)"),
            changeSet(ID_2, "CREATE TABLE sync_test2 (id INT PRIMARY KEY)")
        );

        runner.sync();

        List<IChangelogTrackingEntry> history = runner.history();
        assertEquals(2, history.size());
        assertEquals(ChangelogExecutionType.MARK_RAN, history.get(0).getExecutionType());
        assertEquals(ChangelogExecutionType.MARK_RAN, history.get(1).getExecutionType());
    }


    /**
     * Test status returns without error.
     *
     * @throws Exception on error
     */
    @Test
    void testStatus() throws Exception {
        IChangelogRunner runner = createRunner(
            changeSet(ID_1, "CREATE TABLE status_test (id INT PRIMARY KEY)")
        );

        runner.update();
        assertNotNull(runner.status());
    }


    /**
     * Test validate passes when checksums match.
     *
     * @throws Exception on error
     */
    @Test
    void testValidateSuccess() throws Exception {
        IChangelogRunner runner = createRunner(
            changeSet(ID_1, "CREATE TABLE validate_test (id INT PRIMARY KEY)")
        );

        runner.update();
        runner.validate();
    }


    /**
     * Test update without changelog throws exception.
     */
    @Test
    void testUpdateWithoutChangelog() {
        ChangelogRunnerImpl runner = (ChangelogRunnerImpl) ChangelogRunnerBuilder.builder()
                .dataSource(dataSource)
                .build();

        assertThrows(com.github.toolarium.db.changelog.exception.ChangelogException.class,
                () -> runner.update());
    }


    /**
     * Test validate detects checksum mismatch.
     *
     * @throws Exception on error
     */
    @Test
    void testValidateDetectsMismatch() throws Exception {
        IChangelogRunner runner = createRunner(
            changeSet(ID_1, "CREATE TABLE mismatch_test (id INT PRIMARY KEY)")
        );

        runner.update();

        // Replace changelog with different SQL but same id, causing checksum mismatch
        ChangelogChangeSetIdentity identity = new ChangelogChangeSetIdentity(ID_1, "test-author", "test.db.yaml");
        List<IChangelogEntry> changedEntries = Collections.singletonList(new SqlChangelogEntry("SELECT 999"));
        ChangelogChangeSet changedCs = new ChangelogChangeSet(identity, changedEntries);
        ((ChangelogRunnerImpl) runner).setChangelog(new Changelog("test.db.yaml", Collections.singletonList(changedCs)));

        assertThrows(com.github.toolarium.db.changelog.exception.ChangelogException.class,
                () -> runner.validate());
    }


    /**
     * Test builder with rollback policy, snapshot policy, and object quoting strategy.
     */
    @Test
    void testBuilderPolicies() {
        ChangelogRunnerImpl runner = (ChangelogRunnerImpl) ChangelogRunnerBuilder.builder()
                .dataSource(dataSource)
                .rollbackPolicy(com.github.toolarium.db.changelog.config.ChangelogRollbackPolicy.FAIL)
                .snapshotPolicy(com.github.toolarium.db.changelog.config.ChangelogSnapshotPolicy.DDL)
                .objectQuotingStrategy(com.github.toolarium.db.changelog.config.ChangelogObjectQuotingStrategy.QUOTE_ALL)
                .build();

        assertNotNull(runner);
    }


    /**
     * Test builder with custom resource resolver.
     */
    @Test
    void testBuilderWithResourceResolver() {
        com.github.toolarium.db.changelog.IChangelogResourceResolver customResolver =
                com.github.toolarium.db.changelog.impl.resolver.ChangelogResourceResolverFactory.getInstance().getResourceResolver();

        ChangelogRunnerImpl runner = (ChangelogRunnerImpl) ChangelogRunnerBuilder.builder()
                .dataSource(dataSource)
                .resourceResolver(customResolver)
                .build();

        assertNotNull(runner);
    }


    /**
     * Test Version.
     */
    @Test
    void testVersion() {
        assertEquals(Version.VERSION, Version.getVersion());
        new Version();
        Version.main(new String[]{});
    }

    /**
     * Create a runner with programmatic changesets.
     *
     * @param changeSets the changesets
     * @return the runner
     */
    private IChangelogRunner createRunner(ChangelogChangeSet... changeSets) {
        ChangelogRunnerImpl runner = (ChangelogRunnerImpl) ChangelogRunnerBuilder.builder()
                .dataSource(dataSource)
                .build();

        Changelog changelog = new Changelog("test.db.yaml", Arrays.asList(changeSets));
        runner.setChangelog(changelog);
        return runner;
    }

    /**
     * Create a simple changeset with a single SQL statement.
     *
     * @param id the changeset id
     * @param sql the SQL to execute
     * @return the changeset
     */
    private ChangelogChangeSet changeSet(String id, String sql) {
        ChangelogChangeSetIdentity identity = new ChangelogChangeSetIdentity(id, "test-author", "test.db.yaml");
        List<IChangelogEntry> entries = Collections.singletonList(new SqlChangelogEntry(sql));
        return new ChangelogChangeSet(identity, entries);
    }
}
