/*
 * ChangelogRollbackExecutorImpl.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog.impl.executor.impl;


import com.github.toolarium.db.changelog.dto.IChangelog;
import com.github.toolarium.db.changelog.dto.IChangelogChangeSet;
import com.github.toolarium.db.changelog.dto.IChangelogEntry;
import com.github.toolarium.db.changelog.dto.IChangelogTrackingEntry;
import com.github.toolarium.db.changelog.dto.sql.SqlChangelogEntry;
import com.github.toolarium.db.changelog.exception.ChangelogException;
import com.github.toolarium.db.changelog.impl.executor.IChangelogRollbackExecutor;
import com.github.toolarium.db.changelog.impl.rollback.ChangelogSqlRollbackGeneratorFactory;
import com.github.toolarium.db.changelog.impl.rollback.IChangelogSqlRollbackGenerator;
import com.github.toolarium.db.changelog.impl.sql.IChangelogSqlGenerator;
import com.github.toolarium.db.changelog.impl.tracking.IChangelogTracker;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Executes rollback operations by reversing applied changesets.
 *
 * @author patrick
 */
public class ChangelogRollbackExecutorImpl implements IChangelogRollbackExecutor {
    private static final Logger LOG = LoggerFactory.getLogger(ChangelogRollbackExecutorImpl.class);
    private final IChangelogTracker tracker;
    private final IChangelogSqlGenerator sqlGenerator;
    private final IChangelogSqlRollbackGenerator sqlRollbackGenerator;


    /**
     * Constructor for ChangelogRollbackExecutorImpl
     *
     * @param tracker the tracking table manager
     * @param sqlGenerator the SQL generator for structured entries
     */
    public ChangelogRollbackExecutorImpl(IChangelogTracker tracker, IChangelogSqlGenerator sqlGenerator) {
        this.tracker = tracker;
        this.sqlGenerator = sqlGenerator;
        this.sqlRollbackGenerator = ChangelogSqlRollbackGeneratorFactory.getInstance().getSqlRollbackGenerator();
    }


    /**
     * @see com.github.toolarium.db.changelog.impl.executor.IChangelogRollbackExecutor#rollback(Connection, IChangelog, int)
     */
    @Override
    public void rollback(Connection connection, IChangelog changelog, int count) throws ChangelogException {
        List<IChangelogTrackingEntry> applied = tracker.getAppliedChangeSets(connection);
        List<IChangelogTrackingEntry> toRollback = getLastApplied(applied, count);
        Map<String, IChangelogChangeSet> changeSetMap = buildChangeSetMap(changelog);

        executeRollback(connection, toRollback, changeSetMap);
    }


    /**
     * @see com.github.toolarium.db.changelog.impl.executor.IChangelogRollbackExecutor#rollbackToTag(Connection, IChangelog, String)
     */
    @Override
    public void rollbackToTag(Connection connection, IChangelog changelog, String tag) throws ChangelogException {
        List<IChangelogTrackingEntry> applied = tracker.getAppliedChangeSets(connection);
        List<IChangelogTrackingEntry> toRollback = getAppliedAfterTag(applied, tag);
        Map<String, IChangelogChangeSet> changeSetMap = buildChangeSetMap(changelog);

        executeRollback(connection, toRollback, changeSetMap);
    }


    /**
     * @see com.github.toolarium.db.changelog.impl.executor.IChangelogRollbackExecutor#rollbackToDate(Connection, IChangelog, Instant)
     */
    @Override
    public void rollbackToDate(Connection connection, IChangelog changelog, Instant dateTime) throws ChangelogException {
        List<IChangelogTrackingEntry> applied = tracker.getAppliedChangeSets(connection);
        List<IChangelogTrackingEntry> toRollback = getAppliedAfterDate(applied, dateTime);
        Map<String, IChangelogChangeSet> changeSetMap = buildChangeSetMap(changelog);

        executeRollback(connection, toRollback, changeSetMap);
    }


    /**
     * @see com.github.toolarium.db.changelog.impl.executor.IChangelogRollbackExecutor#generateRollbackSql(Connection, IChangelog, int)
     */
    @Override
    public List<String> generateRollbackSql(Connection connection, IChangelog changelog, int count) throws ChangelogException {
        List<IChangelogTrackingEntry> applied = tracker.getAppliedChangeSets(connection);
        List<IChangelogTrackingEntry> toRollback = getLastApplied(applied, count);
        Map<String, IChangelogChangeSet> changeSetMap = buildChangeSetMap(changelog);

        List<String> statements = new ArrayList<>();
        for (IChangelogTrackingEntry entry : toRollback) {
            statements.addAll(getRollbackStatements(entry, changeSetMap, connection));
        }
        return statements;
    }


    /**
     * Execute rollback for the given tracking entries.
     *
     * @param connection the database connection
     * @param toRollback the entries to rollback (in reverse order)
     * @param changeSetMap the changelog changesets by key
     * @throws ChangelogException if rollback fails
     */
    private void executeRollback(Connection connection, List<IChangelogTrackingEntry> toRollback,
                                 Map<String, IChangelogChangeSet> changeSetMap) throws ChangelogException {
        for (IChangelogTrackingEntry entry : toRollback) {
            List<String> statements = getRollbackStatements(entry, changeSetMap, connection);

            if (statements.isEmpty()) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("No rollback available for changeset [" + entry + "].");
                }
                LOG.warn("No rollback available for changeset [" + entry.getId() + "::" + entry.getAuthor() + "]. Skipping.");
                continue;
            }

            LOG.info("Rolling back changeset [" + entry.getId() + "::" + entry.getAuthor() + "].");

            for (String sql : statements) {
                try (Statement stmt = connection.createStatement()) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Executing rollback SQL: " + sql);
                    }
                    stmt.execute(sql);
                } catch (SQLException e) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Rollback SQL failed for [" + entry + "]: " + e.getMessage(), e);
                    }
                    throw new ChangelogException("Rollback failed for changeset [" + entry.getId() + "]: " + e.getMessage(), e);
                }
            }

            tracker.removeChangeSetEntry(connection, entry);
        }

        LOG.info("Rolled back " + toRollback.size() + " changeset(s).");
    }


    /**
     * Get the rollback SQL statements for a tracking entry.
     * Tries in order: manual rollback entries, auto-rollback, Level 1 SQL rollback.
     *
     * @param trackingEntry the tracking entry
     * @param changeSetMap the changeset map
     * @param connection the database connection
     * @return the rollback SQL statements
     */
    private List<String> getRollbackStatements(IChangelogTrackingEntry trackingEntry,
                                               Map<String, IChangelogChangeSet> changeSetMap,
                                               Connection connection) {
        String key = buildKey(trackingEntry);
        IChangelogChangeSet changeSet = changeSetMap.get(key);

        if (changeSet != null) {
            // Try manual rollback entries first
            if (!changeSet.getRollbackEntries().isEmpty()) {
                return entriesToSql(changeSet.getRollbackEntries(), connection);
            }

            // Try auto-rollback
            List<String> autoRollback = new ArrayList<>();
            for (IChangelogEntry entry : changeSet.getEntries()) {
                if (entry.supportsAutoRollback()) {
                    IChangelogEntry rollbackEntry = entry.createAutoRollback();
                    if (rollbackEntry != null) {
                        autoRollback.addAll(entriesToSql(Collections.singletonList(rollbackEntry), connection));
                    }
                }
            }
            if (!autoRollback.isEmpty()) {
                // Reverse order for auto-rollback (undo in reverse)
                Collections.reverse(autoRollback);
                return autoRollback;
            }

            // Try Level 1 SQL rollback for raw SQL entries
            List<String> sqlRollback = new ArrayList<>();
            for (IChangelogEntry entry : changeSet.getEntries()) {
                if (entry instanceof SqlChangelogEntry) {
                    String sql = ((SqlChangelogEntry) entry).getSql();
                    String rollback = sqlRollbackGenerator.generateRollbackSql(sql);
                    if (rollback != null) {
                        sqlRollback.add(rollback);
                    }
                }
            }
            if (!sqlRollback.isEmpty()) {
                Collections.reverse(sqlRollback);
                return sqlRollback;
            }
        }

        return Collections.emptyList();
    }


    /**
     * Convert changelog entries to SQL statements.
     *
     * @param entries the entries
     * @param connection the database connection
     * @return the SQL statements
     */
    private List<String> entriesToSql(List<IChangelogEntry> entries, Connection connection) {
        List<String> statements = new ArrayList<>();
        for (IChangelogEntry entry : entries) {
            if (entry instanceof SqlChangelogEntry) {
                String sql = ((SqlChangelogEntry) entry).getSql();
                if (sql != null && !sql.trim().isEmpty()) {
                    statements.add(sql.trim());
                }
            } else if (sqlGenerator.supports(entry, connection)) {
                statements.addAll(sqlGenerator.generateSql(entry));
            }
        }
        return statements;
    }


    /**
     * Get the last N applied changesets in reverse order.
     *
     * @param applied all applied entries
     * @param count the number to return
     * @return the entries in reverse order
     */
    private List<IChangelogTrackingEntry> getLastApplied(List<IChangelogTrackingEntry> applied, int count) {
        List<IChangelogTrackingEntry> result = new ArrayList<>();
        int start = Math.max(0, applied.size() - count);
        for (int i = applied.size() - 1; i >= start; i--) {
            result.add(applied.get(i));
        }
        return result;
    }


    /**
     * Get all applied changesets after the given tag in reverse order.
     *
     * @param applied all applied entries
     * @param tag the tag name
     * @return the entries after the tag in reverse order
     * @throws ChangelogException if the tag is not found
     */
    private List<IChangelogTrackingEntry> getAppliedAfterTag(List<IChangelogTrackingEntry> applied,
                                                             String tag) throws ChangelogException {
        int tagIndex = -1;
        for (int i = 0; i < applied.size(); i++) {
            if (tag.equals(applied.get(i).getTag())) {
                tagIndex = i;
            }
        }

        if (tagIndex < 0) {
            throw new ChangelogException("Tag [" + tag + "] not found in tracking table.");
        }

        List<IChangelogTrackingEntry> result = new ArrayList<>();
        for (int i = applied.size() - 1; i > tagIndex; i--) {
            result.add(applied.get(i));
        }
        return result;
    }


    /**
     * Get all applied changesets after the given date in reverse order.
     *
     * @param applied all applied entries
     * @param dateTime the cutoff date
     * @return the entries after the date in reverse order
     */
    private List<IChangelogTrackingEntry> getAppliedAfterDate(List<IChangelogTrackingEntry> applied, Instant dateTime) {
        List<IChangelogTrackingEntry> result = new ArrayList<>();
        for (int i = applied.size() - 1; i >= 0; i--) {
            if (applied.get(i).getDateExecuted() != null && applied.get(i).getDateExecuted().isAfter(dateTime)) {
                result.add(applied.get(i));
            }
        }
        return result;
    }


    /**
     * Build a lookup map from changelog changesets.
     *
     * @param changelog the changelog
     * @return the map keyed by id::author::filepath
     */
    private Map<String, IChangelogChangeSet> buildChangeSetMap(IChangelog changelog) {
        Map<String, IChangelogChangeSet> map = new HashMap<>();
        for (IChangelogChangeSet cs : changelog.getChangelogSets()) {
            map.put(buildKey(cs.getIdentity()), cs);
        }
        return map;
    }


    /**
     * Build the composite key.
     *
     * @param identity the identity
     * @return the key
     */
    private String buildKey(com.github.toolarium.db.changelog.dto.IChangelogChangeSetIdentity identity) {
        return identity.getId() + "::" + identity.getAuthor() + "::" + identity.getFilepath();
    }
}
