/*
 * ChangelogLeaderElectionLockTest.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog.impl.lock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.toolarium.db.changelog.ChangelogRunnerBuilder;
import com.github.toolarium.db.changelog.IChangelogLock;
import com.github.toolarium.db.changelog.impl.ChangelogRunnerImpl;
import com.github.toolarium.db.changelog.impl.lock.impl.ChangelogLeaderElectionLockImpl;
import com.github.toolarium.db.changelog.model.Changelog;
import com.github.toolarium.db.changelog.model.ChangelogChangeSet;
import com.github.toolarium.db.changelog.model.ChangelogChangeSetIdentity;
import com.github.toolarium.db.changelog.model.IChangelogEntry;
import com.github.toolarium.db.changelog.model.sql.SqlChangelogEntry;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


/**
 * Tests for leader-election lock integration.
 *
 * @author patrick
 */
public class ChangelogLeaderElectionLockTest {
    private static final String FILEPATH = "test.db.yaml";
    private JdbcDataSource dataSource;


    /**
     * Set up.
     */
    @BeforeEach
    void setUp() {
        dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:lelock_" + System.nanoTime() + ";DB_CLOSE_DELAY=-1");
        dataSource.setUser("sa");
        dataSource.setPassword("");
    }


    /**
     * Test factory creates leader-election lock with DataSource.
     */
    @Test
    void testFactoryCreatesLeaderElectionLock() {
        IChangelogLock lock = ChangelogLockFactory.getInstance().getChangelogLock(dataSource, "DB_CHANGELOG_LOCK");
        assertNotNull(lock);
        assertTrue(lock instanceof ChangelogLeaderElectionLockImpl);
    }


    /**
     * Test leader-election lock acquire, isLocked, release cycle.
     *
     * @throws Exception on error
     */
    @Test
    void testLeaderElectionLockCycle() throws Exception {
        IChangelogLock lock = ChangelogLockFactory.getInstance().getChangelogLock(dataSource, "DB_CHANGELOG_LOCK");
        lock.acquireLock();
        assertTrue(lock.isLocked());
        lock.releaseLock();
    }


    /**
     * Test end-to-end: update with leader election enabled.
     *
     * @throws Exception on error
     */
    @Test
    void testUpdateWithLeaderElection() throws Exception {
        ChangelogRunnerImpl runner = (ChangelogRunnerImpl) ChangelogRunnerBuilder.builder()
                .dataSource(dataSource)
                .leaderElection(true)
                .build();

        ChangelogChangeSetIdentity id = new ChangelogChangeSetIdentity("le-test", "dev", FILEPATH);
        List<IChangelogEntry> entries = Collections.singletonList(
                new SqlChangelogEntry("CREATE TABLE le_table (id INT PRIMARY KEY)"));
        ChangelogChangeSet cs = new ChangelogChangeSet(id, entries);
        runner.setChangelog(new Changelog(FILEPATH, Arrays.asList(cs)));

        runner.update();
        assertEquals(1, runner.history().size());
    }
}
