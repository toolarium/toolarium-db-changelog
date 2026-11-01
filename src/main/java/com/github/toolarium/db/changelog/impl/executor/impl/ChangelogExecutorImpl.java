/*
 * ChangelogExecutorImpl.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog.impl.executor.impl;


import com.github.toolarium.db.changelog.dto.ChangelogExecutionType;
import com.github.toolarium.db.changelog.dto.IChangelog;
import com.github.toolarium.db.changelog.dto.IChangelogChangeSet;
import com.github.toolarium.db.changelog.dto.IChangelogChangeSetIdentity;
import com.github.toolarium.db.changelog.dto.IChangelogEntry;
import com.github.toolarium.db.changelog.dto.IChangelogTrackingEntry;
import com.github.toolarium.db.changelog.dto.SqlChangelogEntry;
import com.github.toolarium.db.changelog.exception.ChangelogChangeSetExecutionException;
import com.github.toolarium.db.changelog.exception.ChangelogException;
import com.github.toolarium.db.changelog.impl.checksum.IChangelogChecksumGenerator;
import com.github.toolarium.db.changelog.impl.executor.IChangelogExecutor;
import com.github.toolarium.db.changelog.impl.tracking.IChangelogTracker;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Executes a changelog against a database connection.
 *
 * @author patrick
 */
public class ChangelogExecutorImpl implements IChangelogExecutor {
    private static final Logger LOG = LoggerFactory.getLogger(ChangelogExecutorImpl.class);
    private final IChangelogTracker tracker;
    private final IChangelogChecksumGenerator checksumGenerator;

    /**
     * Constructor for ChangelogExecutorImpl
     *
     * @param tracker the tracking table manager
     * @param checksumGenerator the checksum generator
     */
    public ChangelogExecutorImpl(IChangelogTracker tracker, IChangelogChecksumGenerator checksumGenerator) {
        this.tracker = tracker;
        this.checksumGenerator = checksumGenerator;
    }

    /**
     * Execute all pending changesets from the changelog.
     *
     * @param connection the database connection
     * @param changelog the changelog to execute
     * @throws ChangelogException if execution fails
     */
    public void update(Connection connection, IChangelog changelog) throws ChangelogException {
        update(connection, changelog, Integer.MAX_VALUE);
    }

    /**
     * Execute up to count pending changesets from the changelog.
     *
     * @param connection the database connection
     * @param changelog the changelog to execute
     * @param count the maximum number of changesets to execute
     * @throws ChangelogException if execution fails
     */
    public void update(Connection connection, IChangelog changelog, int count) throws ChangelogException {
        tracker.ensureTrackingTable(connection);

        List<IChangelogTrackingEntry> applied = tracker.getAppliedChangeSets(connection);
        Map<String, IChangelogTrackingEntry> appliedMap = buildAppliedMap(applied);

        String deploymentId = UUID.randomUUID().toString().substring(0, 36);
        int executed = 0;

        for (IChangelogChangeSet changeSet : changelog.getChangelogSets()) {
            if (executed >= count) {
                break;
            }

            String key = buildKey(changeSet.getIdentity());
            IChangelogTrackingEntry existing = appliedMap.get(key);
            String checksum = checksumGenerator.computeChecksum(changeSet);

            if (existing != null) {
                if (changeSet.isRunAlways()) {
                    executeChangeSet(connection, changeSet);
                    tracker.removeChangeSetEntry(connection, changeSet.getIdentity());
                    tracker.markChangeSetExecuted(connection, changeSet, checksum,
                            ChangelogExecutionType.RERAN, deploymentId);
                    executed++;
                } else if (changeSet.isRunOnChange() && !checksum.equals(existing.getChecksum())) {
                    executeChangeSet(connection, changeSet);
                    tracker.removeChangeSetEntry(connection, changeSet.getIdentity());
                    tracker.markChangeSetExecuted(connection, changeSet, checksum,
                            ChangelogExecutionType.RERAN, deploymentId);
                    executed++;
                }
                // else: already applied, skip
            } else {
                executeChangeSet(connection, changeSet);
                tracker.markChangeSetExecuted(connection, changeSet, checksum,
                        ChangelogExecutionType.EXECUTED, deploymentId);
                executed++;
            }
        }

        LOG.info("Executed " + executed + " changeset(s).");
    }

    /**
     * Execute a single changeset.
     *
     * @param connection the database connection
     * @param changeSet the changeset to execute
     * @throws ChangelogException if execution fails
     * @throws ChangelogChangeSetExecutionException if a changeset fails and failOnError is true
     */
    private void executeChangeSet(Connection connection, IChangelogChangeSet changeSet) throws ChangelogException {
        IChangelogChangeSetIdentity identity = changeSet.getIdentity();

        if (LOG.isDebugEnabled()) {
            LOG.debug("Executing changeset [" + identity + "].");
        }
        LOG.info("Executing changeset [" + identity.getId() + "::" + identity.getAuthor() + "].");

        boolean autoCommit = true;
        try {
            autoCommit = connection.getAutoCommit();
            if (changeSet.isRunInTransaction()) {
                connection.setAutoCommit(false);
            }

            for (IChangelogEntry entry : changeSet.getEntries()) {
                executeSqlEntry(connection, entry);
            }

            if (changeSet.isRunInTransaction()) {
                connection.commit();
            }
        } catch (SQLException e) {
            if (changeSet.isRunInTransaction()) {
                try {
                    connection.rollback();
                } catch (SQLException re) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Failed to rollback transaction for changeset [" + identity + "]: " + re.getMessage(), re);
                    }
                }
            }

            if (changeSet.isFailOnError()) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Changeset [" + identity + "] failed: " + e.getMessage(), e);
                }
                throw new ChangelogChangeSetExecutionException("Changeset [" + identity.getId() + "::" + identity.getAuthor() + "] failed.", e);
            }

            if (LOG.isDebugEnabled()) {
                LOG.debug("Changeset [" + identity + "] failed but failOnError is false: " + e.getMessage(), e);
            }
            LOG.warn("Changeset [" + identity + "] failed but failOnError is false: " + e.getMessage());
        } finally {
            try {
                connection.setAutoCommit(autoCommit);
            } catch (SQLException e) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Failed to restore autoCommit: " + e.getMessage(), e);
                }
            }
        }
    }

    /**
     * Execute a single SQL entry.
     *
     * @param connection the database connection
     * @param entry the entry to execute
     * @throws SQLException if the SQL execution fails
     * @throws ChangelogChangeSetExecutionException if the entry type is not supported
     */
    private void executeSqlEntry(Connection connection, IChangelogEntry entry) throws SQLException, ChangelogChangeSetExecutionException {
        if (entry instanceof SqlChangelogEntry) {
            SqlChangelogEntry sqlChange = (SqlChangelogEntry) entry;
            String sql = sqlChange.getSql();
            if (sql == null || sql.trim().isEmpty()) {
                return;
            }

            if (sqlChange.isSplitStatements()) {
                String[] statements = sql.split(sqlChange.getEndDelimiter());
                for (String stmt : statements) {
                    String trimmed = stmt.trim();
                    if (!trimmed.isEmpty()) {
                        executeStatement(connection, trimmed);
                    }
                }
            } else {
                executeStatement(connection, sql.trim());
            }
        } else {
            throw new ChangelogChangeSetExecutionException("Unsupported entry type: " + entry.getClass().getName());
        }
    }

    /**
     * Execute a single SQL statement.
     *
     * @param connection the database connection
     * @param sql the SQL to execute
     * @throws SQLException if the execution fails
     */
    private void executeStatement(Connection connection, String sql) throws SQLException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Executing SQL: " + sql);
        }
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
    }

    /**
     * Build a lookup map from applied changesets.
     *
     * @param applied the list of applied entries
     * @return the lookup map keyed by id::author::filepath
     */
    private Map<String, IChangelogTrackingEntry> buildAppliedMap(List<IChangelogTrackingEntry> applied) {
        Map<String, IChangelogTrackingEntry> map = new HashMap<>();
        for (IChangelogTrackingEntry entry : applied) {
            map.put(buildKey(entry), entry);
        }
        return map;
    }

    /**
     * Build the composite key for a changeset identity.
     *
     * @param identity the identity
     * @return the key string
     */
    private String buildKey(IChangelogChangeSetIdentity identity) {
        return identity.getId() + "::" + identity.getAuthor() + "::" + identity.getFilepath();
    }
}
