/*
 * ChangelogExecutorImpl.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog.impl.executor.impl;

import com.github.toolarium.db.changelog.IChangelogChecksumGenerator;
import com.github.toolarium.db.changelog.IChangelogContext;
import com.github.toolarium.db.changelog.IChangelogExecutor;
import com.github.toolarium.db.changelog.IChangelogTracker;
import com.github.toolarium.db.changelog.config.ChangelogRollbackPolicy;
import com.github.toolarium.db.changelog.exception.ChangelogChangeSetExecutionException;
import com.github.toolarium.db.changelog.exception.ChangelogException;
import com.github.toolarium.db.changelog.impl.precondition.IChangelogPreconditionEvaluator;
import com.github.toolarium.db.changelog.impl.rollback.ChangelogSqlRollbackGeneratorFactory;
import com.github.toolarium.db.changelog.impl.rollback.IChangelogSqlRollbackGenerator;
import com.github.toolarium.db.changelog.impl.sql.IChangelogSqlGenerator;
import com.github.toolarium.db.changelog.model.ChangelogExecutionType;
import com.github.toolarium.db.changelog.model.IChangelog;
import com.github.toolarium.db.changelog.model.IChangelogChangeSet;
import com.github.toolarium.db.changelog.model.IChangelogChangeSetIdentity;
import com.github.toolarium.db.changelog.model.IChangelogEntry;
import com.github.toolarium.db.changelog.model.IChangelogTrackingEntry;
import com.github.toolarium.db.changelog.model.sql.SqlChangelogEntry;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Executes a changelog against a database connection.
 *
 * @author patrick
 */
public class ChangelogExecutorImpl implements IChangelogExecutor {
    private static final String KEY_SEPARATOR = "|";
    private static final String LOG_SUFFIX = "].";
    private static final int DEFAULT_STATEMENT_TIMEOUT_SECONDS = 300;
    private static final Logger LOG = LoggerFactory.getLogger(ChangelogExecutorImpl.class);
    private final IChangelogTracker tracker;
    private final IChangelogChecksumGenerator checksumGenerator;
    private final IChangelogSqlGenerator sqlGenerator;
    private final IChangelogSqlRollbackGenerator sqlRollbackGenerator;
    private final IChangelogPreconditionEvaluator preconditionEvaluator;
    private final ChangelogRollbackPolicy rollbackPolicy;


    /**
     * Constructor for ChangelogExecutorImpl
     *
     * @param tracker the tracking table manager
     * @param checksumGenerator the checksum generator
     * @param sqlGenerator the SQL generator for structured change types
     * @param rollbackPolicy the rollback policy for missing rollback definitions
     */
    public ChangelogExecutorImpl(IChangelogTracker tracker, IChangelogChecksumGenerator checksumGenerator,
                                 IChangelogSqlGenerator sqlGenerator, ChangelogRollbackPolicy rollbackPolicy) {
        this.tracker = tracker;
        this.checksumGenerator = checksumGenerator;
        this.sqlGenerator = sqlGenerator;
        this.sqlRollbackGenerator = ChangelogSqlRollbackGeneratorFactory.getInstance().getSqlRollbackGenerator();
        this.preconditionEvaluator = com.github.toolarium.db.changelog.impl.precondition.ChangelogPreconditionEvaluatorFactory
                .getInstance().getPreconditionEvaluator();
        this.rollbackPolicy = rollbackPolicy;
    }


    /**
     * @see com.github.toolarium.db.changelog.IChangelogExecutor#update(Connection, IChangelog, IChangelogContext)
     */
    @Override
    public void update(Connection connection, IChangelog changelog, IChangelogContext context) throws ChangelogException {
        update(connection, changelog, Integer.MAX_VALUE, context);
    }


    /**
     * @see com.github.toolarium.db.changelog.IChangelogExecutor#update(Connection, IChangelog, int, IChangelogContext)
     */
    @Override
    public void update(Connection connection, IChangelog changelog, int count, IChangelogContext context) throws ChangelogException {
        tracker.ensureTrackingTable(connection);

        List<IChangelogTrackingEntry> applied = tracker.getAppliedChangeSets(connection);
        Map<String, IChangelogTrackingEntry> appliedMap = buildAppliedMap(applied);

        String deploymentId = UUID.randomUUID().toString().substring(0, 36);
        int executed = 0;

        for (IChangelogChangeSet changeSet : changelog.getChangelogSets()) {
            if (executed >= count) {
                break;
            }

            // Check context/label filters
            if (!matchesContextAndLabels(changeSet, context)) {
                continue;
            }

            // Check preconditions
            ChangelogPreconditionAction preconditionResult = evaluatePreconditions(changeSet, connection);
            if (preconditionResult == ChangelogPreconditionAction.SKIP) {
                continue;
            }
            if (preconditionResult == ChangelogPreconditionAction.MARK_RAN) {
                String cs = checksumGenerator.computeChecksum(changeSet);
                tracker.markChangeSetExecuted(connection, changeSet, cs, ChangelogExecutionType.SKIPPED, deploymentId);
                executed++;
                continue;
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
            LOG.debug("Executing changeset [" + identity + LOG_SUFFIX);
        }
        LOG.info("Executing changeset [" + identity.getId() + KEY_SEPARATOR + identity.getAuthor() + LOG_SUFFIX);

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
                throw new ChangelogChangeSetExecutionException("Changeset [" + identity.getId() + KEY_SEPARATOR + identity.getAuthor() + "] failed.", e);
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
            executeSqlChangelogEntry(connection, (SqlChangelogEntry) entry);
        } else if (entry instanceof com.github.toolarium.db.changelog.model.dml.CustomChangelogEntry) {
            executeCustomEntry(connection, (com.github.toolarium.db.changelog.model.dml.CustomChangelogEntry) entry);
        } else if (sqlGenerator.supports(entry, connection)) {
            List<String> statements = sqlGenerator.generateSql(entry);
            for (String stmt : statements) {
                executeStatement(connection, stmt);
            }
        } else {
            throw new ChangelogChangeSetExecutionException("Unsupported entry type: " + entry.getClass().getName());
        }
    }


    /**
     * Execute a custom changelog entry.
     *
     * @param connection the database connection
     * @param entry the custom entry
     * @throws ChangelogChangeSetExecutionException if execution fails
     */
    private void executeCustomEntry(Connection connection,
                                    com.github.toolarium.db.changelog.model.dml.CustomChangelogEntry entry) throws ChangelogChangeSetExecutionException {
        try {
            entry.getCustomEntry().execute(connection);
        } catch (Exception e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Custom change failed [" + entry.getClassName() + "]: " + e.getMessage(), e);
            }
            throw new ChangelogChangeSetExecutionException("Custom change [" + entry.getClassName() + "] failed.", e);
        }
    }


    /**
     * Execute a SqlChangelogEntry by splitting and running its SQL.
     *
     * @param connection the database connection
     * @param sqlChange the SQL changelog entry
     * @throws SQLException if execution fails
     */
    private void executeSqlChangelogEntry(Connection connection, SqlChangelogEntry sqlChange) throws SQLException {
        String sql = sqlChange.getSql();
        if (sql == null || sql.trim().isEmpty()) {
            return;
        }

        if (sqlChange.isSplitStatements()) {
            String[] statements = sql.split(Pattern.quote(sqlChange.getEndDelimiter()));
            for (String stmt : statements) {
                String trimmed = stmt.trim();
                if (!trimmed.isEmpty()) {
                    executeStatement(connection, trimmed);
                }
            }
        } else {
            executeStatement(connection, sql.trim());
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
            stmt.setQueryTimeout(DEFAULT_STATEMENT_TIMEOUT_SECONDS);
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
        return identity.getId() + KEY_SEPARATOR + identity.getAuthor() + KEY_SEPARATOR + identity.getFilepath();
    }


    /**
     * @see com.github.toolarium.db.changelog.IChangelogExecutor#rollback(Connection, IChangelog, int)
     */
    @Override
    public void rollback(Connection connection, IChangelog changelog, int count) throws ChangelogException {
        List<IChangelogTrackingEntry> applied = tracker.getAppliedChangeSets(connection);
        executeRollback(connection, getLastApplied(applied, count), buildChangeSetMap(changelog));
    }


    /**
     * @see com.github.toolarium.db.changelog.IChangelogExecutor#rollbackToTag(Connection, IChangelog, String)
     */
    @Override
    public void rollbackToTag(Connection connection, IChangelog changelog, String tag) throws ChangelogException {
        List<IChangelogTrackingEntry> applied = tracker.getAppliedChangeSets(connection);
        executeRollback(connection, getAppliedAfterTag(applied, tag), buildChangeSetMap(changelog));
    }


    /**
     * @see com.github.toolarium.db.changelog.IChangelogExecutor#rollbackToDate(Connection, IChangelog, Instant)
     */
    @Override
    public void rollbackToDate(Connection connection, IChangelog changelog, Instant dateTime) throws ChangelogException {
        List<IChangelogTrackingEntry> applied = tracker.getAppliedChangeSets(connection);
        executeRollback(connection, getAppliedAfterDate(applied, dateTime), buildChangeSetMap(changelog));
    }


    /**
     * @see com.github.toolarium.db.changelog.IChangelogExecutor#generateRollbackSql(Connection, IChangelog, int)
     */
    @Override
    public List<String> generateRollbackSql(Connection connection, IChangelog changelog, int count) throws ChangelogException {
        List<IChangelogTrackingEntry> applied = tracker.getAppliedChangeSets(connection);
        Map<String, IChangelogChangeSet> changeSetMap = buildChangeSetMap(changelog);
        List<String> statements = new ArrayList<>();
        for (IChangelogTrackingEntry entry : getLastApplied(applied, count)) {
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
        List<String> failedChangesets = new ArrayList<>();
        int rolledBack = 0;

        for (IChangelogTrackingEntry entry : toRollback) {
            List<String> statements = getRollbackStatements(entry, changeSetMap, connection);

            if (statements.isEmpty()) {
                if (rollbackPolicy == ChangelogRollbackPolicy.FAIL) {
                    throw new ChangelogException("No rollback available for changeset ["
                            + entry.getId() + KEY_SEPARATOR + entry.getAuthor() + LOG_SUFFIX);
                }
                if (rollbackPolicy == ChangelogRollbackPolicy.WARN) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("No rollback available for changeset [" + entry + LOG_SUFFIX);
                    }
                    LOG.warn("No rollback available for changeset [" + entry.getId() + KEY_SEPARATOR + entry.getAuthor() + "]. Skipping.");
                }
                continue;
            }

            LOG.info("Rolling back changeset [" + entry.getId() + KEY_SEPARATOR + entry.getAuthor() + LOG_SUFFIX);

            boolean success = true;
            for (String sql : statements) {
                try {
                    executeStatement(connection, sql);
                } catch (SQLException e) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Rollback SQL failed for [" + entry + "]: " + e.getMessage(), e);
                    }
                    LOG.warn("Rollback SQL failed for changeset [" + entry.getId() + "]: " + e.getMessage());
                    failedChangesets.add(entry.getId());
                    success = false;
                    break;
                }
            }

            if (success) {
                tracker.removeChangeSetEntry(connection, entry);
                rolledBack++;
            }
        }

        LOG.info("Rolled back " + rolledBack + " changeset(s).");

        if (!failedChangesets.isEmpty()) {
            throw new ChangelogException("Rollback completed with " + failedChangesets.size()
                    + " failure(s): " + String.join(", ", failedChangesets));
        }
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
            if (!changeSet.getRollbackEntries().isEmpty()) {
                return entriesToSql(changeSet.getRollbackEntries(), connection);
            }

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
                Collections.reverse(autoRollback);
                return autoRollback;
            }

            List<String> sqlRollback = new ArrayList<>();
            for (IChangelogEntry entry : changeSet.getEntries()) {
                if (entry instanceof SqlChangelogEntry) {
                    String rollback = sqlRollbackGenerator.generateRollbackSql(((SqlChangelogEntry) entry).getSql());
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
     * Check if a changeset matches the active contexts and labels.
     * A changeset with no contexts/labels always matches.
     *
     * @param changeSet the changeset
     * @param context the execution context
     * @return true if the changeset should be executed
     */
    private boolean matchesContextAndLabels(IChangelogChangeSet changeSet, IChangelogContext context) {
        if (!context.getActiveContexts().isEmpty() && !changeSet.getContexts().isEmpty()) {
            boolean contextMatch = false;
            for (String ctx : changeSet.getContexts()) {
                if (context.getActiveContexts().contains(ctx)) {
                    contextMatch = true;
                    break;
                }
            }
            if (!contextMatch) {
                return false;
            }
        }

        if (!context.getActiveLabels().isEmpty() && !changeSet.getLabels().isEmpty()) {
            boolean labelMatch = false;
            for (String label : changeSet.getLabels()) {
                if (context.getActiveLabels().contains(label)) {
                    labelMatch = true;
                    break;
                }
            }
            if (!labelMatch) {
                return false;
            }
        }

        return true;
    }


    private enum ChangelogPreconditionAction {
        /** Proceed with execution. */
        EXECUTE,
        /** Skip this changeset. */
        SKIP,
        /** Skip but mark as ran. */
        MARK_RAN,
        /** Halt with error. */
        HALT
    }


    /**
     * Evaluate preconditions for a changeset.
     *
     * @param changeSet the changeset
     * @param connection the database connection
     * @return the action to take
     * @throws ChangelogException if HALT is the action
     */
    private ChangelogPreconditionAction evaluatePreconditions(IChangelogChangeSet changeSet,
                                                              Connection connection) throws ChangelogException {
        com.github.toolarium.db.changelog.model.precondition.ChangelogPreconditionContainer preconditions = changeSet.getPreconditions();
        if (preconditions == null || preconditions.getChecks().isEmpty()) {
            return ChangelogPreconditionAction.EXECUTE;
        }

        for (com.github.toolarium.db.changelog.model.precondition.IChangelogPrecondition check : preconditions.getChecks()) {
            boolean passed = preconditionEvaluator.evaluate(check, connection);
            if (!passed) {
                switch (preconditions.getOnFail()) {
                    case HALT:
                        throw new ChangelogException("Precondition failed [" + check.getDescription()
                                + "] for changeset [" + changeSet.getIdentity() + LOG_SUFFIX);
                    case WARN:
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("Precondition failed [" + check.getDescription()
                                    + "] for changeset [" + changeSet.getIdentity() + LOG_SUFFIX);
                        }
                        LOG.warn("Precondition failed for changeset [" + changeSet.getIdentity() + "]. Skipping.");
                        return ChangelogPreconditionAction.SKIP;
                    case CONTINUE:
                        return ChangelogPreconditionAction.SKIP;
                    case MARK_RAN:
                        return ChangelogPreconditionAction.MARK_RAN;
                    default:
                        return ChangelogPreconditionAction.SKIP;
                }
            }
        }

        return ChangelogPreconditionAction.EXECUTE;
    }


    /**
     * Build a map of changeset keys to changesets for quick lookup.
     *
     * @param changelog the changelog
     * @return the map
     */
    private Map<String, IChangelogChangeSet> buildChangeSetMap(IChangelog changelog) {
        Map<String, IChangelogChangeSet> map = new HashMap<>();
        for (IChangelogChangeSet cs : changelog.getChangelogSets()) {
            map.put(buildKey(cs.getIdentity()), cs);
        }
        return map;
    }
}
