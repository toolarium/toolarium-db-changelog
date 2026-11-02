/*
 * ChangelogRunnerImpl.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog.impl;

import com.github.toolarium.db.changelog.IChangelogChecksumGenerator;
import com.github.toolarium.db.changelog.IChangelogContext;
import com.github.toolarium.db.changelog.IChangelogExecutor;
import com.github.toolarium.db.changelog.IChangelogLock;
import com.github.toolarium.db.changelog.IChangelogResourceResolver;
import com.github.toolarium.db.changelog.IChangelogRunner;
import com.github.toolarium.db.changelog.IChangelogTracker;
import com.github.toolarium.db.changelog.Version;
import com.github.toolarium.db.changelog.config.ChangelogConfiguration;
import com.github.toolarium.db.changelog.exception.ChangelogException;
import com.github.toolarium.db.changelog.impl.checksum.ChangelogChecksumGeneratorFactory;
import com.github.toolarium.db.changelog.impl.executor.ChangelogExecutorFactory;
import com.github.toolarium.db.changelog.impl.lock.ChangelogLockFactory;
import com.github.toolarium.db.changelog.impl.parser.ChangelogParserFactory;
import com.github.toolarium.db.changelog.impl.parser.IChangelogParser;
import com.github.toolarium.db.changelog.impl.resolver.ChangelogParameterResolverFactory;
import com.github.toolarium.db.changelog.impl.resolver.ChangelogResourceResolverFactory;
import com.github.toolarium.db.changelog.impl.tracking.ChangelogTrackerFactory;
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
    private final IChangelogResourceResolver resourceResolver;
    private volatile IChangelog changelog;

    /**
     * Constructor for ChangelogRunnerImpl
     *
     * @param config the configuration
     */
    public ChangelogRunnerImpl(ChangelogConfiguration config) {
        this(config, null, null, null, null, null);
    }


    /**
     * Constructor with custom resource resolver.
     *
     * @param config the configuration
     * @param resourceResolver the resource resolver
     */
    public ChangelogRunnerImpl(ChangelogConfiguration config, IChangelogResourceResolver resourceResolver) {
        this(config, resourceResolver, null, null, null, null);
    }


    /**
     * Constructor with all optional interface overrides.
     * Null values fall back to factory defaults.
     *
     * @param config the configuration
     * @param resourceResolver the resource resolver, or null for default
     * @param tracker the tracker, or null for default
     * @param checksumGenerator the checksum generator, or null for default
     * @param executor the executor, or null for default
     * @param lock the lock, or null for default
     */
    public ChangelogRunnerImpl(ChangelogConfiguration config, IChangelogResourceResolver resourceResolver, // CHECKSTYLE IGNORE THIS LINE
                               IChangelogTracker tracker, IChangelogChecksumGenerator checksumGenerator,
                               IChangelogExecutor executor, IChangelogLock lock) {
        this.config = config;

        if (tracker != null) {
            this.tracker = tracker;
        } else {
            this.tracker = ChangelogTrackerFactory.getInstance().getTracker(config.getTrackingTableName(), Version.VERSION);
        }

        if (checksumGenerator != null) {
            this.checksumGenerator = checksumGenerator;
        } else {
            this.checksumGenerator = ChangelogChecksumGeneratorFactory.getInstance().getChecksumGenerator();
        }

        if (executor != null) {
            this.executor = executor;
        } else {
            this.executor = ChangelogExecutorFactory.getInstance().getExecutor(this.tracker, this.checksumGenerator, config.getRollbackPolicy());
        }

        this.changelogContext = new ChangelogContextImpl(config.getContexts(), config.getLabels());

        if (lock != null) {
            this.lock = lock;
        } else if (config.isLeaderElectionEnabled() && config.getDataSource() != null) {
            this.lock = ChangelogLockFactory.getInstance().getChangelogLock(
                    config.getDataSource(), config.getLockTableName());
        } else {
            this.lock = ChangelogLockFactory.getInstance().getChangelogLock();
        }

        this.parserFactory = ChangelogParserFactory.getInstance();

        if (resourceResolver != null) {
            this.resourceResolver = resourceResolver;
        } else {
            this.resourceResolver = ChangelogResourceResolverFactory.getInstance().getResourceResolver();
        }
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
     * @see com.github.toolarium.db.changelog.IChangelogRunner#validate()
     */
    @Override
    public void validate() throws ChangelogException {
        ensureChangelog();
        ensureDataSource();
        try (Connection connection = config.getDataSource().getConnection()) {
            tracker.ensureTrackingTable(connection);
            List<IChangelogTrackingEntry> applied = tracker.getAppliedChangeSets(connection);
            java.util.Map<String, String> appliedChecksums = new java.util.HashMap<>();
            for (IChangelogTrackingEntry entry : applied) {
                appliedChecksums.put(entry.getId() + KEY_SEPARATOR + entry.getAuthor()
                        + KEY_SEPARATOR + entry.getFilepath(), entry.getChecksum());
            }

            List<String> mismatches = new java.util.ArrayList<>();
            for (IChangelogChangeSet changeSet : changelog.getChangelogSets()) {
                String key = changeSet.getIdentity().getId() + KEY_SEPARATOR
                        + changeSet.getIdentity().getAuthor() + KEY_SEPARATOR
                        + changeSet.getIdentity().getFilepath();
                String storedChecksum = appliedChecksums.get(key);
                if (storedChecksum != null) {
                    String currentChecksum = checksumGenerator.computeChecksum(changeSet);
                    if (!currentChecksum.equals(storedChecksum)) {
                        mismatches.add(changeSet.getIdentity().getId());
                    }
                }
            }

            if (!mismatches.isEmpty()) {
                throw new ChangelogException("Validation failed: checksum mismatch for changeset(s) ["
                        + String.join(", ", mismatches) + "].");
            }

            LOG.info("Validation passed for " + applied.size() + " applied changeset(s).");
        } catch (SQLException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Validation failed: " + e.getMessage(), e);
            }
            throw new ChangelogException("Validation failed.", e);
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

        try (InputStream input = resourceResolver.resolve(path)) {
            if (input == null) {
                return null;
            }

            IChangelogParser parser = parserFactory.getParser(path);
            var paramResolver = ChangelogParameterResolverFactory.getInstance().getParameterResolver();
            return parser.parse(input, path, paramResolver);
        } catch (Exception e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Failed to load changelog [" + e.getMessage(), e);
            }
            throw new RuntimeException("Failed to load changelog [" + path + "].", e);
        }
    }


}
