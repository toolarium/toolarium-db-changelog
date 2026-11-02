/*
 * ChangelogTrackerTest.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog.impl.tracking;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.toolarium.db.changelog.IChangelogTracker;
import com.github.toolarium.db.changelog.exception.ChangelogException;
import com.github.toolarium.db.changelog.model.ChangelogChangeSet;
import com.github.toolarium.db.changelog.model.ChangelogChangeSetIdentity;
import com.github.toolarium.db.changelog.model.ChangelogExecutionType;
import com.github.toolarium.db.changelog.model.IChangelogTrackingEntry;
import com.github.toolarium.db.changelog.model.sql.SqlChangelogEntry;
import java.sql.Connection;
import java.util.Collections;
import java.util.List;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


/**
 * Isolated tests for ChangelogTrackerImpl.
 *
 * @author patrick
 */
public class ChangelogTrackerTest {
    private static final String TABLE_NAME = "TEST_CHANGELOG";
    private static final String VERSION = "0.0.1";
    private JdbcDataSource dataSource;
    private IChangelogTracker tracker;


    /**
     * Set up.
     */
    @BeforeEach
    void setUp() {
        dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:tracker_" + System.nanoTime() + ";DB_CLOSE_DELAY=-1");
        dataSource.setUser("sa");
        dataSource.setPassword("");
        tracker = ChangelogTrackerFactory.getInstance().getTracker(TABLE_NAME, VERSION);
    }


    /**
     * Test table creation.
     *
     * @throws Exception on error
     */
    @Test
    void testEnsureTrackingTable() throws Exception {
        try (Connection conn = dataSource.getConnection()) {
            tracker.ensureTrackingTable(conn);
            tracker.ensureTrackingTable(conn); // idempotent
            List<IChangelogTrackingEntry> entries = tracker.getAppliedChangeSets(conn);
            assertNotNull(entries);
            assertTrue(entries.isEmpty());
        }
    }


    /**
     * Test mark and read.
     *
     * @throws Exception on error
     */
    @Test
    void testMarkAndRead() throws Exception {
        try (Connection conn = dataSource.getConnection()) {
            tracker.ensureTrackingTable(conn);

            ChangelogChangeSetIdentity id = new ChangelogChangeSetIdentity("cs-1", "dev", "file.db.yaml");
            ChangelogChangeSet cs = new ChangelogChangeSet(id,
                    Collections.singletonList(new SqlChangelogEntry("SELECT 1")));

            tracker.markChangeSetExecuted(conn, cs, "abc123", ChangelogExecutionType.EXECUTED, "deploy-1");

            List<IChangelogTrackingEntry> entries = tracker.getAppliedChangeSets(conn);
            assertEquals(1, entries.size());
            assertEquals("cs-1", entries.get(0).getId());
            assertEquals("abc123", entries.get(0).getChecksum());
            assertEquals(ChangelogExecutionType.EXECUTED, entries.get(0).getExecutionType());
            assertEquals("deploy-1", entries.get(0).getDeploymentId());
        }
    }


    /**
     * Test remove entry.
     *
     * @throws Exception on error
     */
    @Test
    void testRemoveEntry() throws Exception {
        try (Connection conn = dataSource.getConnection()) {
            tracker.ensureTrackingTable(conn);

            ChangelogChangeSetIdentity id = new ChangelogChangeSetIdentity("cs-1", "dev", "file.db.yaml");
            ChangelogChangeSet cs = new ChangelogChangeSet(id,
                    Collections.singletonList(new SqlChangelogEntry("SELECT 1")));

            tracker.markChangeSetExecuted(conn, cs, "abc", ChangelogExecutionType.EXECUTED, "d1");
            assertEquals(1, tracker.getAppliedChangeSets(conn).size());

            tracker.removeChangeSetEntry(conn, id);
            assertEquals(0, tracker.getAppliedChangeSets(conn).size());
        }
    }


    /**
     * Test tag.
     *
     * @throws Exception on error
     */
    @Test
    void testTag() throws Exception {
        try (Connection conn = dataSource.getConnection()) {
            tracker.ensureTrackingTable(conn);

            ChangelogChangeSetIdentity id = new ChangelogChangeSetIdentity("cs-1", "dev", "file.db.yaml");
            ChangelogChangeSet cs = new ChangelogChangeSet(id,
                    Collections.singletonList(new SqlChangelogEntry("SELECT 1")));

            tracker.markChangeSetExecuted(conn, cs, "abc", ChangelogExecutionType.EXECUTED, "d1");
            tracker.tagLastApplied(conn, "v1.0");

            List<IChangelogTrackingEntry> entries = tracker.getAppliedChangeSets(conn);
            assertEquals("v1.0", entries.get(0).getTag());
        }
    }


    /**
     * Test tag on empty table throws.
     *
     * @throws Exception on error
     */
    @Test
    void testTagEmptyThrows() throws Exception {
        try (Connection conn = dataSource.getConnection()) {
            tracker.ensureTrackingTable(conn);
            assertThrows(ChangelogException.class, () -> tracker.tagLastApplied(conn, "v1.0"));
        }
    }


    /**
     * Test clear checksums.
     *
     * @throws Exception on error
     */
    @Test
    void testClearChecksums() throws Exception {
        try (Connection conn = dataSource.getConnection()) {
            tracker.ensureTrackingTable(conn);

            ChangelogChangeSetIdentity id = new ChangelogChangeSetIdentity("cs-1", "dev", "file.db.yaml");
            ChangelogChangeSet cs = new ChangelogChangeSet(id,
                    Collections.singletonList(new SqlChangelogEntry("SELECT 1")));

            tracker.markChangeSetExecuted(conn, cs, "abc123", ChangelogExecutionType.EXECUTED, "d1");
            tracker.clearChecksums(conn);

            List<IChangelogTrackingEntry> entries = tracker.getAppliedChangeSets(conn);
            assertNull(entries.get(0).getChecksum());
        }
    }
}
