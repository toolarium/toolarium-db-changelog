/*
 * ChangelogRunnerTest.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.toolarium.db.changelog.dto.Changelog;
import com.github.toolarium.db.changelog.dto.ChangelogChangeSet;
import com.github.toolarium.db.changelog.dto.ChangelogChangeSetIdentity;
import com.github.toolarium.db.changelog.dto.ChangelogExecutionType;
import com.github.toolarium.db.changelog.dto.IChangelogEntry;
import com.github.toolarium.db.changelog.dto.IChangelogTrackingEntry;
import com.github.toolarium.db.changelog.dto.SqlChangelogEntry;
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
