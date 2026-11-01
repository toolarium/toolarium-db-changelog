/*
 * ChangelogExecutionBehaviorTest.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog.impl;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.github.toolarium.db.changelog.ChangelogRunnerBuilder;
import com.github.toolarium.db.changelog.IChangelogRunner;
import com.github.toolarium.db.changelog.dto.Changelog;
import com.github.toolarium.db.changelog.dto.ChangelogChangeSet;
import com.github.toolarium.db.changelog.dto.ChangelogChangeSetIdentity;
import com.github.toolarium.db.changelog.dto.ChangelogExecutionType;
import com.github.toolarium.db.changelog.dto.IChangelogEntry;
import com.github.toolarium.db.changelog.dto.IChangelogTrackingEntry;
import com.github.toolarium.db.changelog.dto.SqlChangelogEntry;
import com.github.toolarium.db.changelog.exception.ChangelogException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


/**
 * Tests for execution behavior: runAlways, runOnChange, failOnError, checksum, error handling.
 *
 * @author patrick
 */
public class ChangelogExecutionBehaviorTest {
    private static final String FILEPATH = "test.db.yaml";
    private static final String AUTHOR = "dev";
    private JdbcDataSource dataSource;


    /**
     * Set up a fresh H2 in-memory database for each test.
     */
    @BeforeEach
    void setUp() {
        dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:behavior_" + System.nanoTime() + ";DB_CLOSE_DELAY=-1");
        dataSource.setUser("sa");
        dataSource.setPassword("");
    }


    /**
     * Test runAlways re-executes every time.
     *
     * @throws Exception on error
     */
    @Test
    void testRunAlways() throws Exception {
        ChangelogChangeSetIdentity id = new ChangelogChangeSetIdentity("run-always", AUTHOR, FILEPATH);
        List<IChangelogEntry> entries = Collections.singletonList(
                new SqlChangelogEntry("CREATE TABLE IF NOT EXISTS ra_test (id INT)"));
        ChangelogChangeSet cs = new ChangelogChangeSet(id, entries, Collections.emptyList(), null,
                true, false, true, true, Collections.emptyList(), Collections.emptyList());

        IChangelogRunner runner = createRunner(cs);
        runner.update();
        runner.update();

        List<IChangelogTrackingEntry> history = runner.history();
        assertEquals(1, history.size());
        assertEquals(ChangelogExecutionType.RERAN, history.get(0).getExecutionType());
    }


    /**
     * Test runOnChange re-executes when checksum changes.
     *
     * @throws Exception on error
     */
    @Test
    void testRunOnChange() throws Exception {
        ChangelogChangeSetIdentity id = new ChangelogChangeSetIdentity("run-on-change", AUTHOR, FILEPATH);

        // First run
        List<IChangelogEntry> entries1 = Collections.singletonList(
                new SqlChangelogEntry("CREATE TABLE IF NOT EXISTS roc_test (id INT)"));
        ChangelogChangeSet cs1 = new ChangelogChangeSet(id, entries1, Collections.emptyList(), null,
                false, true, true, true, Collections.emptyList(), Collections.emptyList());
        IChangelogRunner runner1 = createRunner(cs1);
        runner1.update();

        final String checksum1 = runner1.history().get(0).getChecksum();

        // Second run with different SQL (checksum changes)
        List<IChangelogEntry> entries2 = Collections.singletonList(
                new SqlChangelogEntry("CREATE TABLE IF NOT EXISTS roc_test (id INT, name VARCHAR(50))"));
        ChangelogChangeSet cs2 = new ChangelogChangeSet(id, entries2, Collections.emptyList(), null,
                false, true, true, true, Collections.emptyList(), Collections.emptyList());
        ChangelogRunnerImpl runner2 = (ChangelogRunnerImpl) ChangelogRunnerBuilder.builder()
                .dataSource(dataSource).build();
        runner2.setChangelog(new Changelog(FILEPATH, Collections.singletonList(cs2)));
        runner2.update();

        List<IChangelogTrackingEntry> history2 = runner2.history();
        assertEquals(1, history2.size());
        assertNotEquals(checksum1, history2.get(0).getChecksum());
        assertEquals(ChangelogExecutionType.RERAN, history2.get(0).getExecutionType());
    }


    /**
     * Test failOnError=false continues execution on failure.
     *
     * @throws Exception on error
     */
    @Test
    void testFailOnErrorFalse() throws Exception {
        ChangelogChangeSetIdentity id1 = new ChangelogChangeSetIdentity("will-fail", AUTHOR, FILEPATH);
        List<IChangelogEntry> entries1 = Collections.singletonList(
                new SqlChangelogEntry("INVALID SQL STATEMENT"));
        ChangelogChangeSet cs1 = new ChangelogChangeSet(id1, entries1, Collections.emptyList(), null,
                false, false, false, true, Collections.emptyList(), Collections.emptyList());

        ChangelogChangeSetIdentity id2 = new ChangelogChangeSetIdentity("will-succeed", AUTHOR, FILEPATH);
        List<IChangelogEntry> entries2 = Collections.singletonList(
                new SqlChangelogEntry("CREATE TABLE fe_test (id INT PRIMARY KEY)"));
        ChangelogChangeSet cs2 = new ChangelogChangeSet(id2, entries2);

        IChangelogRunner runner = createRunner(cs1, cs2);
        runner.update();

        List<IChangelogTrackingEntry> history = runner.history();
        assertEquals(2, history.size());
    }


    /**
     * Test failOnError=true (default) throws exception on failure.
     *
     * @throws Exception on error
     */
    @Test
    void testFailOnErrorTrue() throws Exception {
        ChangelogChangeSetIdentity id = new ChangelogChangeSetIdentity("will-fail", AUTHOR, FILEPATH);
        List<IChangelogEntry> entries = Collections.singletonList(
                new SqlChangelogEntry("INVALID SQL STATEMENT"));
        ChangelogChangeSet cs = new ChangelogChangeSet(id, entries);

        IChangelogRunner runner = createRunner(cs);
        assertThrows(ChangelogException.class, () -> runner.update());
    }


    /**
     * Test that update with no changelog throws.
     *
     * @throws Exception on error
     */
    @Test
    void testUpdateWithNoChangelog() throws Exception {
        IChangelogRunner runner = ChangelogRunnerBuilder.builder()
                .dataSource(dataSource).build();
        assertThrows(ChangelogException.class, () -> runner.update());
    }


    /**
     * Test checksum is deterministic.
     *
     * @throws Exception on error
     */
    @Test
    void testChecksumDeterministic() throws Exception {
        var factory = com.github.toolarium.db.changelog.impl.checksum.ChangelogChecksumGeneratorFactory.getInstance();
        var generator = factory.getChecksumGenerator();

        ChangelogChangeSetIdentity id = new ChangelogChangeSetIdentity("cs", AUTHOR, FILEPATH);
        List<IChangelogEntry> entries = Collections.singletonList(new SqlChangelogEntry("SELECT 1"));
        ChangelogChangeSet cs = new ChangelogChangeSet(id, entries);

        String checksum1 = generator.computeChecksum(cs);
        String checksum2 = generator.computeChecksum(cs);

        assertEquals(checksum1, checksum2);
        assertEquals(64, checksum1.length());
        assertNotNull(checksum1);
    }


    /**
     * Test checksum differs for different SQL.
     *
     * @throws Exception on error
     */
    @Test
    void testChecksumDiffersForDifferentSql() throws Exception {
        var generator = com.github.toolarium.db.changelog.impl.checksum.ChangelogChecksumGeneratorFactory
                .getInstance().getChecksumGenerator();

        ChangelogChangeSetIdentity id = new ChangelogChangeSetIdentity("cs", AUTHOR, FILEPATH);
        ChangelogChangeSet cs1 = new ChangelogChangeSet(id,
                Collections.singletonList(new SqlChangelogEntry("SELECT 1")));
        ChangelogChangeSet cs2 = new ChangelogChangeSet(id,
                Collections.singletonList(new SqlChangelogEntry("SELECT 2")));

        assertNotEquals(generator.computeChecksum(cs1), generator.computeChecksum(cs2));
    }


    /**
     * Test history on empty database returns empty list.
     *
     * @throws Exception on error
     */
    @Test
    void testHistoryEmpty() throws Exception {
        IChangelogRunner runner = createRunner();
        runner.update();

        List<IChangelogTrackingEntry> history = runner.history();
        assertNotNull(history);
        assertEquals(0, history.size());
    }


    /**
     * Test ChangelogConfiguration validation.
     */
    @Test
    void testConfigurationValidation() {
        assertThrows(IllegalArgumentException.class, () ->
                ChangelogRunnerBuilder.builder().build());
    }


    /**
     * Test ChangelogConfiguration toString.
     */
    @Test
    void testConfigurationToString() {
        var config = new com.github.toolarium.db.changelog.config.ChangelogConfiguration();
        assertNotNull(config.toString());
        assertEquals(config, config);
        assertEquals(config.hashCode(), config.hashCode());
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
}
