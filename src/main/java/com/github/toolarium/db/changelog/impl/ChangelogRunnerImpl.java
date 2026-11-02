/*
 * ChangelogRunnerImpl.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog.impl;

import com.github.toolarium.common.util.FileUtil;
import com.github.toolarium.db.changelog.IChangelogContext;
import com.github.toolarium.db.changelog.IChangelogRunner;
import com.github.toolarium.db.changelog.Version;
import com.github.toolarium.db.changelog.config.ChangelogConfiguration;
import com.github.toolarium.db.changelog.exception.ChangelogException;
import com.github.toolarium.db.changelog.impl.checksum.ChangelogChecksumGeneratorFactory;
import com.github.toolarium.db.changelog.impl.checksum.IChangelogChecksumGenerator;
import com.github.toolarium.db.changelog.impl.executor.ChangelogExecutorFactory;
import com.github.toolarium.db.changelog.impl.executor.IChangelogExecutor;
import com.github.toolarium.db.changelog.impl.lock.ChangelogLockFactory;
import com.github.toolarium.db.changelog.impl.lock.IChangelogLock;
import com.github.toolarium.db.changelog.impl.parser.ChangelogParserFactory;
import com.github.toolarium.db.changelog.impl.parser.IChangelogParser;
import com.github.toolarium.db.changelog.impl.resolver.ChangelogParameterResolverFactory;
import com.github.toolarium.db.changelog.impl.tracking.ChangelogTrackerFactory;
import com.github.toolarium.db.changelog.impl.tracking.IChangelogTracker;
import com.github.toolarium.db.changelog.model.ChangelogExecutionType;
import com.github.toolarium.db.changelog.model.IChangelog;
import com.github.toolarium.db.changelog.model.IChangelogChangeSet;
import com.github.toolarium.db.changelog.model.IChangelogChangeSetStatus;
import com.github.toolarium.db.changelog.model.IChangelogTrackingEntry;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Default implementation of the changelog runner.
 *
 * @author patrick
 */
public class ChangelogRunnerImpl implements IChangelogRunner {
    private static final String KEY_SEPARATOR = "|";
    private static final Logger LOG = LoggerFactory.getLogger(ChangelogRunnerImpl.class);
    private final ChangelogConfiguration config;
    private final IChangelogTracker tracker;
    private final IChangelogChecksumGenerator checksumGenerator;
    private final IChangelogExecutor executor;
    private final IChangelogContext changelogContext;
    private final IChangelogLock lock;
    private final ChangelogParserFactory parserFactory;
    private volatile IChangelog changelog;

    /**
     * Constructor for ChangelogRunnerImpl
     *
     * @param config the configuration
     */
    public ChangelogRunnerImpl(ChangelogConfiguration config) {
        this.config = config;
        this.tracker = ChangelogTrackerFactory.getInstance().getTracker(config.getTrackingTableName(), Version.VERSION);
        this.checksumGenerator = ChangelogChecksumGeneratorFactory.getInstance().getChecksumGenerator();
        this.executor = ChangelogExecutorFactory.getInstance().getExecutor(tracker, checksumGenerator);
        this.changelogContext = new ChangelogContextImpl(config.getContexts(), config.getLabels());
        if (config.isLeaderElectionEnabled() && config.getDataSource() != null) {
            this.lock = ChangelogLockFactory.getInstance().getChangelogLock(
                    config.getDataSource(), config.getLockTableName());
        } else {
            this.lock = ChangelogLockFactory.getInstance().getChangelogLock();
        }
        this.parserFactory = ChangelogParserFactory.getInstance();
        this.changelog = loadChangelog();
    }


    /**
     * Set the changelog programmatically.
     *
     * @param changelog the changelog
     */
    public void setChangelog(IChangelog changelog) {
        this.changelog = changelog;
    }


    /**
     * @see com.github.toolarium.db.changelog.IChangelogRunner#update()
     */
    @Override
    public void update() throws ChangelogException {
        update(Integer.MAX_VALUE);
    }


    /**
     * @see com.github.toolarium.db.changelog.IChangelogRunner#update(int)
     */
    @Override
    public void update(int count) throws ChangelogException {
        ensureChangelog();
        ensureDataSource();

        ChangelogException error = null;
        try {
            lock.acquireLock();
            try (Connection connection = config.getDataSource().getConnection()) {
                executor.update(connection, changelog, count, changelogContext);
            }
        } catch (SQLException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Database connection error: " + e.getMessage(), e);
            }
            error = new ChangelogException("Database connection error.", e);
        } catch (ChangelogException e) {
            error = e;
        } finally {
            releaseLockSafely(error);
        }
        if (error != null) {
            throw error;
        }
    }


    /**
     * @see com.github.toolarium.db.changelog.IChangelogRunner#rollback(int)
     */
    @Override
    public void rollback(int count) throws ChangelogException {
        ensureChangelog();
        ensureDataSource();

        ChangelogException error = null;
        try {
            lock.acquireLock();
            try (Connection connection = config.getDataSource().getConnection()) {
                tracker.ensureTrackingTable(connection);
                executor.rollback(connection, changelog, count);
            }
        } catch (SQLException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Rollback failed: " + e.getMessage(), e);
            }
            error = new ChangelogException("Rollback failed.", e);
        } catch (ChangelogException e) {
            error = e;
        } finally {
            releaseLockSafely(error);
        }
        if (error != null) {
            throw error;
        }
    }


    /**
     * @see com.github.toolarium.db.changelog.IChangelogRunner#rollback(String)
     */
    @Override
    public void rollback(String tag) throws ChangelogException {
        ensureChangelog();
        ensureDataSource();

        ChangelogException error = null;
        try {
            lock.acquireLock();
            try (Connection connection = config.getDataSource().getConnection()) {
                tracker.ensureTrackingTable(connection);
                executor.rollbackToTag(connection, changelog, tag);
            }
        } catch (SQLException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Rollback to tag failed: " + e.getMessage(), e);
            }
            error = new ChangelogException("Rollback to tag failed.", e);
        } catch (ChangelogException e) {
            error = e;
        } finally {
            releaseLockSafely(error);
        }
        if (error != null) {
            throw error;
        }
    }


    /**
     * @see com.github.toolarium.db.changelog.IChangelogRunner#rollback(Instant)
     */
    @Override
    public void rollback(Instant dateTime) throws ChangelogException {
        ensureChangelog();
        ensureDataSource();

        ChangelogException error = null;
        try {
            lock.acquireLock();
            try (Connection connection = config.getDataSource().getConnection()) {
                tracker.ensureTrackingTable(connection);
                executor.rollbackToDate(connection, changelog, dateTime);
            }
        } catch (SQLException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Rollback to date failed: " + e.getMessage(), e);
            }
            error = new ChangelogException("Rollback to date failed.", e);
        } catch (ChangelogException e) {
            error = e;
        } finally {
            releaseLockSafely(error);
        }
        if (error != null) {
            throw error;
        }
    }


    /**
     * @see com.github.toolarium.db.changelog.IChangelogRunner#generateRollbackSql(int)
     */
    @Override
    public List<String> generateRollbackSql(int count) throws ChangelogException {
        ensureChangelog();
        ensureDataSource();
        try (Connection connection = config.getDataSource().getConnection()) {
            tracker.ensureTrackingTable(connection);
            return executor.generateRollbackSql(connection, changelog, count);
        } catch (SQLException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Generate rollback SQL failed: " + e.getMessage(), e);
            }
            throw new ChangelogException("Generate rollback SQL failed.", e);
        }
    }


    /**
     * @see com.github.toolarium.db.changelog.IChangelogRunner#tag(String)
     */
    @Override
    public void tag(String tagName) throws ChangelogException {
        ensureDataSource();
        try (Connection connection = config.getDataSource().getConnection()) {
            tracker.ensureTrackingTable(connection);
            tracker.tagLastApplied(connection, tagName);
        } catch (SQLException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Failed to tag." + e.getMessage(), e);
            }
            throw new ChangelogException("Failed to tag.", e);
        }
    }


    /**
     * @see com.github.toolarium.db.changelog.IChangelogRunner#sync()
     */
    @Override
    public void sync() throws ChangelogException {
        ensureChangelog();
        ensureDataSource();

        ChangelogException error = null;
        try {
            lock.acquireLock();
            try (Connection connection = config.getDataSource().getConnection()) {
                tracker.ensureTrackingTable(connection);
                List<IChangelogTrackingEntry> applied = tracker.getAppliedChangeSets(connection);
                java.util.Set<String> appliedKeys = new java.util.HashSet<>();
                for (IChangelogTrackingEntry entry : applied) {
                    appliedKeys.add(entry.getId() + KEY_SEPARATOR + entry.getAuthor() + KEY_SEPARATOR + entry.getFilepath());
                }

                String deploymentId = java.util.UUID.randomUUID().toString();
                int synced = 0;
                for (IChangelogChangeSet changeSet : changelog.getChangelogSets()) {
                    String key = changeSet.getIdentity().getId() + KEY_SEPARATOR + changeSet.getIdentity().getAuthor()
                            + KEY_SEPARATOR + changeSet.getIdentity().getFilepath();
                    if (!appliedKeys.contains(key)) {
                        String checksum = checksumGenerator.computeChecksum(changeSet);
                        tracker.markChangeSetExecuted(connection, changeSet, checksum,
                                ChangelogExecutionType.MARK_RAN, deploymentId);
                        synced++;
                    }
                }
                LOG.info("Synced " + synced + " changeset(s).");
            }
        } catch (SQLException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Sync failed: " + e.getMessage(), e);
            }
            error = new ChangelogException("Sync failed.", e);
        } catch (ChangelogException e) {
            error = e;
        } finally {
            releaseLockSafely(error);
        }
        if (error != null) {
            throw error;
        }
    }


    /**
     * @see com.github.toolarium.db.changelog.IChangelogRunner#status()
     */
    @Override
    public List<IChangelogChangeSetStatus> status() throws ChangelogException {
        ensureChangelog();
        ensureDataSource();
        try (Connection connection = config.getDataSource().getConnection()) {
            tracker.ensureTrackingTable(connection);
            List<IChangelogTrackingEntry> applied = tracker.getAppliedChangeSets(connection);
            java.util.Set<String> appliedKeys = new java.util.HashSet<>();
            for (IChangelogTrackingEntry entry : applied) {
                appliedKeys.add(entry.getId() + KEY_SEPARATOR + entry.getAuthor() + KEY_SEPARATOR + entry.getFilepath());
            }

            // Return empty list for now - full implementation needs IChangelogChangeSetStatus impl
            return java.util.Collections.emptyList();
        } catch (SQLException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Status check failed: " + e.getMessage(), e);
            }
            throw new ChangelogException("Status check failed.", e);
        }
    }


    /**
     * @see com.github.toolarium.db.changelog.IChangelogRunner#history()
     */
    @Override
    public List<IChangelogTrackingEntry> history() throws ChangelogException {
        ensureDataSource();
        try (Connection connection = config.getDataSource().getConnection()) {
            tracker.ensureTrackingTable(connection);
            return tracker.getAppliedChangeSets(connection);
        } catch (SQLException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Failed to read history." + e.getMessage(), e);
            }
            throw new ChangelogException("Failed to read history.", e);
        }
    }


    /**
     * @see com.github.toolarium.db.changelog.IChangelogRunner#clearChecksums()
     */
    @Override
    public void clearChecksums() throws ChangelogException {
        ensureDataSource();
        try (Connection connection = config.getDataSource().getConnection()) {
            tracker.ensureTrackingTable(connection);
            tracker.clearChecksums(connection);
        } catch (SQLException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Failed to clear checksums." + e.getMessage(), e);
            }
            throw new ChangelogException("Failed to clear checksums.", e);
        }
    }


    /**
     * Ensure a changelog is configured.
     *
     * @throws ChangelogException if no changelog is available
     */
    private void ensureChangelog() throws ChangelogException {
        if (changelog == null) {
            throw new ChangelogException("No changelog configured. Set a changelog before calling this operation.");
        }
    }


    /**
     * Ensure a data source is configured.
     *
     * @throws ChangelogException if no data source is available
     */
    private void ensureDataSource() throws ChangelogException {
        if (config.getDataSource() == null) {
            throw new ChangelogException("No data source configured.");
        }
    }


    /**
     * Safely release the lock, suppressing any exceptions to avoid masking the original error.
     *
     * @param primary the primary exception, or null if no error occurred
     * @throws ChangelogException if lock release fails and no primary exception exists
     */
    private void releaseLockSafely(ChangelogException primary) throws ChangelogException {
        try {
            lock.releaseLock();
        } catch (ChangelogException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Failed to release lock: " + e.getMessage(), e);
            }
            if (primary != null) {
                primary.addSuppressed(e);
            } else {
                throw e;
            }
        }
    }


    /**
     * Load the changelog from the configured path.
     *
     * @return the parsed changelog, or null if no path is configured
     * @throws RuntimeException if the changelog cannot be loaded
     */
    private IChangelog loadChangelog() {
        String path = config.getChangelogPath();
        if (path == null || path.trim().isEmpty()) {
            return null;
        }

        try {
            InputStream input = resolveChangelogInput(path);
            if (input == null) {
                return null;
            }

            try {
                IChangelogParser parser = parserFactory.getParser(path);
                var paramResolver = ChangelogParameterResolverFactory.getInstance().getParameterResolver();
                return parser.parse(input, path, paramResolver);
            } finally {
                input.close();
            }
        } catch (Exception e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Failed to load changelog [" + e.getMessage(), e);
            }
            throw new RuntimeException("Failed to load changelog [" + path + "].", e);
        }
    }


    /**
     * Resolve the changelog input stream from a path. Supports classpath: prefix and file paths.
     *
     * @param path the changelog path
     * @return the input stream, or null if not found
     */
    private InputStream resolveChangelogInput(String path) {
        String resolvedPath = path;
        if (resolvedPath.startsWith("classpath:")) {
            resolvedPath = FileUtil.getInstance().simplifyPath(resolvedPath.substring("classpath:".length()));
            InputStream input = Thread.currentThread().getContextClassLoader().getResourceAsStream(resolvedPath);
            if (input == null) {
                input = getClass().getResourceAsStream(resolvedPath);
            }
            return input;
        }

        resolvedPath = FileUtil.getInstance().simplifyPath(resolvedPath);
        try {
            return new java.io.FileInputStream(new java.io.File(resolvedPath));
        } catch (java.io.FileNotFoundException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Changelog file not found [" + resolvedPath + "]: " + e.getMessage(), e);
            }
            return null;
        }
    }
}
