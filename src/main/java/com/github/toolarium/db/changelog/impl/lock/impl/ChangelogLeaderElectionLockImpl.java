/*
 * ChangelogLeaderElectionLockImpl.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog.impl.lock.impl;

import com.github.toolarium.db.changelog.IChangelogLock;
import com.github.toolarium.db.changelog.exception.ChangelogException;
import com.github.toolarium.leader.election.ILeaderElector;
import com.github.toolarium.leader.election.LeaderElectionFactory;
import com.github.toolarium.leader.election.exception.LeaderElectionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Lock implementation using toolarium-leader-election with DATABASE strategy.
 * Uses the same DataSource as the changelog to acquire a distributed lock
 * via the DB_CHANGELOG_LOCK table.
 *
 * @author patrick
 */
public class ChangelogLeaderElectionLockImpl implements IChangelogLock {
    private static final Logger LOG = LoggerFactory.getLogger(ChangelogLeaderElectionLockImpl.class);
    private static final long DEFAULT_LOCK_TIMEOUT_SECONDS = 60;
    private final ILeaderElector elector;
    private volatile boolean locked;


    /**
     * Constructor for ChangelogLeaderElectionLockImpl
     *
     * @param dataSource the data source
     * @param lockTableName the lock table name (default: DB_CHANGELOG_LOCK)
     * @throws IllegalStateException if leader election initialization fails
     */
    public ChangelogLeaderElectionLockImpl(DataSource dataSource, String lockTableName) {
        try {
            this.elector = LeaderElectionFactory.builder()
                    .name("db-changelog")
                    .database()
                    .dataSource(dataSource)
                    .tableName(lockTableName)
                    .build();
        } catch (LeaderElectionException e) {
            throw new IllegalStateException("Failed to initialize leader election lock.", e);
        }
        this.locked = false;
    }


    /**
     * @see com.github.toolarium.db.changelog.IChangelogLock#acquireLock()
     */
    @Override
    public void acquireLock() throws ChangelogException {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            Future<Boolean> future = executor.submit(() -> elector.elect());
            boolean isLeader = future.get(DEFAULT_LOCK_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            if (isLeader) {
                locked = true;
                LOG.info("Acquired changelog lock via leader election.");
            } else {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Failed to acquire changelog lock: another instance is the leader.");
                }
                locked = false;
            }
        } catch (TimeoutException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Lock acquisition timed out after " + DEFAULT_LOCK_TIMEOUT_SECONDS + " seconds.", e);
            }
            throw new ChangelogException("Lock acquisition timed out after " + DEFAULT_LOCK_TIMEOUT_SECONDS + " seconds.", e);
        } catch (ExecutionException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Failed to acquire changelog lock: " + e.getMessage(), e);
            }
            throw new ChangelogException("Failed to acquire changelog lock.", e.getCause());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ChangelogException("Lock acquisition interrupted.", e);
        } finally {
            executor.shutdownNow();
        }
    }


    /**
     * @see com.github.toolarium.db.changelog.IChangelogLock#releaseLock()
     */
    @Override
    public void releaseLock() throws ChangelogException {
        try {
            elector.close();
            locked = false;
            LOG.info("Released changelog lock.");
        } catch (LeaderElectionException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Failed to release changelog lock: " + e.getMessage(), e);
            }
            throw new ChangelogException("Failed to release changelog lock.", e);
        }
    }


    /**
     * @see com.github.toolarium.db.changelog.IChangelogLock#isLocked()
     */
    @Override
    public boolean isLocked() {
        return locked;
    }


    /**
     * @see java.lang.AutoCloseable#close()
     */
    @Override
    public void close() throws ChangelogException {
        releaseLock();
    }
}
