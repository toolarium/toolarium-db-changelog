/*
 * ChangelogSnapshotManagerTest.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog.impl.snapshot;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.toolarium.db.changelog.model.ChangelogChangeSetIdentity;
import java.sql.Connection;
import java.sql.Statement;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


/**
 * Tests for ChangelogSnapshotManagerImpl.
 *
 * @author patrick
 */
public class ChangelogSnapshotManagerTest {
    private JdbcDataSource dataSource;
    private IChangelogSnapshotManager snapshotManager;


    /**
     * Set up a fresh H2 in-memory database for each test.
     */
    @BeforeEach
    void setUp() {
        dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:snapshot_test_" + System.nanoTime() + ";DB_CLOSE_DELAY=-1");
        dataSource.setUser("sa");
        dataSource.setPassword("");
        snapshotManager = ChangelogSnapshotManagerFactory.getInstance().getSnapshotManager();
    }


    /**
     * Test factory singleton.
     */
    @Test
    void testFactorySingleton() {
        assertSame(ChangelogSnapshotManagerFactory.getInstance(), ChangelogSnapshotManagerFactory.getInstance());
        assertNotNull(snapshotManager);
    }


    /**
     * Test ensure snapshot table is idempotent.
     *
     * @throws Exception on error
     */
    @Test
    void testEnsureSnapshotTableIdempotent() throws Exception {
        try (Connection conn = dataSource.getConnection()) {
            snapshotManager.ensureSnapshotTable(conn);
            snapshotManager.ensureSnapshotTable(conn);
        }
    }


    /**
     * Test capture and retrieve snapshot.
     *
     * @throws Exception on error
     */
    @Test
    void testCaptureAndRetrieveSnapshot() throws Exception {
        try (Connection conn = dataSource.getConnection()) {
            // Create a test table to snapshot
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("CREATE TABLE test_person (id INT PRIMARY KEY, name VARCHAR(100) NOT NULL, email VARCHAR(255))");
            }

            snapshotManager.ensureSnapshotTable(conn);

            ChangelogChangeSetIdentity identity = new ChangelogChangeSetIdentity("cs-1", "author", "test.db.yaml");
            snapshotManager.captureSnapshot(conn, identity, "TEST_PERSON");

            String snapshot = snapshotManager.getSnapshot(conn, identity, "TEST_PERSON");
            assertNotNull(snapshot);
            assertTrue(snapshot.contains("ID"));
            assertTrue(snapshot.contains("NAME"));
            assertTrue(snapshot.contains("EMAIL"));
        }
    }


    /**
     * Test get snapshot returns null for non-existent snapshot.
     *
     * @throws Exception on error
     */
    @Test
    void testGetSnapshotReturnsNull() throws Exception {
        try (Connection conn = dataSource.getConnection()) {
            snapshotManager.ensureSnapshotTable(conn);

            ChangelogChangeSetIdentity identity = new ChangelogChangeSetIdentity("nonexistent", "author", "test.db.yaml");
            String snapshot = snapshotManager.getSnapshot(conn, identity, "some_table");
            assertNull(snapshot);
        }
    }


    /**
     * Test remove snapshot.
     *
     * @throws Exception on error
     */
    @Test
    void testRemoveSnapshot() throws Exception {
        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("CREATE TABLE test_remove (id INT PRIMARY KEY)");
            }

            snapshotManager.ensureSnapshotTable(conn);

            ChangelogChangeSetIdentity identity = new ChangelogChangeSetIdentity("cs-remove", "author", "test.db.yaml");
            snapshotManager.captureSnapshot(conn, identity, "TEST_REMOVE");
            assertNotNull(snapshotManager.getSnapshot(conn, identity, "TEST_REMOVE"));

            snapshotManager.removeSnapshot(conn, identity);
            assertNull(snapshotManager.getSnapshot(conn, identity, "TEST_REMOVE"));
        }
    }


    /**
     * Test cleanup snapshots does not fail.
     *
     * @throws Exception on error
     */
    @Test
    void testCleanupSnapshots() throws Exception {
        try (Connection conn = dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("CREATE TABLE test_cleanup (id INT PRIMARY KEY)");
            }

            snapshotManager.ensureSnapshotTable(conn);

            ChangelogChangeSetIdentity id1 = new ChangelogChangeSetIdentity("cs-cleanup-1", "author", "test.db.yaml");
            ChangelogChangeSetIdentity id2 = new ChangelogChangeSetIdentity("cs-cleanup-2", "author", "test.db.yaml");
            snapshotManager.captureSnapshot(conn, id1, "TEST_CLEANUP");
            snapshotManager.captureSnapshot(conn, id2, "TEST_CLEANUP");

            // Cleanup keeping only 1 most recent should not throw
            snapshotManager.cleanupSnapshots(conn, 1);
        }
    }


    /**
     * Test custom snapshot table name via factory.
     *
     * @throws Exception on error
     */
    @Test
    void testCustomSnapshotTableName() throws Exception {
        IChangelogSnapshotManager custom = ChangelogSnapshotManagerFactory.getInstance()
                .getSnapshotManager("CUSTOM_SNAPSHOT");

        try (Connection conn = dataSource.getConnection()) {
            custom.ensureSnapshotTable(conn);
        }
    }
}
