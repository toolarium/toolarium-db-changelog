/*
 * ChangelogRunnerImpl.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog.impl;


import com.github.toolarium.db.changelog.IChangelogRunner;
import com.github.toolarium.db.changelog.Version;
import com.github.toolarium.db.changelog.config.ChangelogConfiguration;
import com.github.toolarium.db.changelog.dto.IChangelog;
import com.github.toolarium.db.changelog.dto.IChangelogChangeSetStatus;
import com.github.toolarium.db.changelog.dto.IChangelogTrackingEntry;
import com.github.toolarium.db.changelog.exception.ChangelogException;
import com.github.toolarium.db.changelog.impl.checksum.ChangelogChecksumGeneratorFactory;
import com.github.toolarium.db.changelog.impl.checksum.IChangelogChecksumGenerator;
import com.github.toolarium.db.changelog.impl.executor.ChangelogExecutorFactory;
import com.github.toolarium.db.changelog.impl.executor.IChangelogExecutor;
import com.github.toolarium.db.changelog.impl.executor.IChangelogRollbackExecutor;
import com.github.toolarium.db.changelog.impl.lock.ChangelogLockFactory;
import com.github.toolarium.db.changelog.impl.lock.IChangelogLock;
import com.github.toolarium.db.changelog.impl.parser.ChangelogParserFactory;
import com.github.toolarium.db.changelog.impl.parser.IChangelogParser;
import com.github.toolarium.db.changelog.impl.resolver.ChangelogParameterResolverFactory;
import com.github.toolarium.db.changelog.impl.tracking.ChangelogTrackerFactory;
import com.github.toolarium.db.changelog.impl.tracking.IChangelogTracker;
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
    private static final Logger LOG = LoggerFactory.getLogger(ChangelogRunnerImpl.class);
    private final ChangelogConfiguration config;
    private final IChangelogTracker tracker;
    private final IChangelogChecksumGenerator checksumGenerator;
    private final IChangelogExecutor executor;
    private final IChangelogRollbackExecutor rollbackExecutor;
    private final IChangelogLock lock;
    private final ChangelogParserFactory parserFactory;
    private IChangelog changelog;

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
        this.rollbackExecutor = ChangelogExecutorFactory.getInstance().getRollbackExecutor(tracker);
        this.lock = ChangelogLockFactory.getInstance().getChangelogLock();
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

        try {
            lock.acquireLock();
            try (Connection connection = config.getDataSource().getConnection()) {
                executor.update(connection, changelog, count);
            }
        } catch (SQLException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Database connection error." + e.getMessage(), e);
            }
            throw new ChangelogException("Database connection error.", e);
        } finally {
            lock.releaseLock();
        }
    }

    /**
     * @see com.github.toolarium.db.changelog.IChangelogRunner#rollback(int)
     */
    @Override
    public void rollback(int count) throws ChangelogException {
        ensureChangelog();
        try {
            lock.acquireLock();
            try (Connection connection = config.getDataSource().getConnection()) {
                tracker.ensureTrackingTable(connection);
                rollbackExecutor.rollback(connection, changelog, count);
            }
        } catch (SQLException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Rollback failed: " + e.getMessage(), e);
            }
            throw new ChangelogException("Rollback failed.", e);
        } finally {
            lock.releaseLock();
        }
    }


    /**
     * @see com.github.toolarium.db.changelog.IChangelogRunner#rollback(String)
     */
    @Override
    public void rollback(String tag) throws ChangelogException {
        ensureChangelog();
        try {
            lock.acquireLock();
            try (Connection connection = config.getDataSource().getConnection()) {
                tracker.ensureTrackingTable(connection);
                rollbackExecutor.rollbackToTag(connection, changelog, tag);
            }
        } catch (SQLException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Rollback to tag failed: " + e.getMessage(), e);
            }
            throw new ChangelogException("Rollback to tag failed.", e);
        } finally {
            lock.releaseLock();
        }
    }


    /**
     * @see com.github.toolarium.db.changelog.IChangelogRunner#rollback(Instant)
     */
    @Override
    public void rollback(Instant dateTime) throws ChangelogException {
        ensureChangelog();
        try {
            lock.acquireLock();
            try (Connection connection = config.getDataSource().getConnection()) {
                tracker.ensureTrackingTable(connection);
                rollbackExecutor.rollbackToDate(connection, changelog, dateTime);
            }
        } catch (SQLException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Rollback to date failed: " + e.getMessage(), e);
            }
            throw new ChangelogException("Rollback to date failed.", e);
        } finally {
            lock.releaseLock();
        }
    }


    /**
     * @see com.github.toolarium.db.changelog.IChangelogRunner#generateRollbackSql(int)
     */
    @Override
    public List<String> generateRollbackSql(int count) throws ChangelogException {
        ensureChangelog();
        try (Connection connection = config.getDataSource().getConnection()) {
            tracker.ensureTrackingTable(connection);
            return rollbackExecutor.generateRollbackSql(connection, changelog, count);
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
        // TODO: implement
        throw new UnsupportedOperationException("sync() will be available in a future version.");
    }

    /**
     * @see com.github.toolarium.db.changelog.IChangelogRunner#status()
     */
    @Override
    public List<IChangelogChangeSetStatus> status() throws ChangelogException {
        // TODO: implement
        throw new UnsupportedOperationException("status() will be available in a future version.");
    }

    /**
     * @see com.github.toolarium.db.changelog.IChangelogRunner#history()
     */
    @Override
    public List<IChangelogTrackingEntry> history() throws ChangelogException {
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
            resolvedPath = resolvedPath.substring("classpath:".length());
            InputStream input = Thread.currentThread().getContextClassLoader().getResourceAsStream(resolvedPath);
            if (input == null) {
                input = getClass().getResourceAsStream(resolvedPath);
            }
            return input;
        }

        try {
            return new java.io.FileInputStream(resolvedPath);
        } catch (java.io.FileNotFoundException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Changelog file not found [" + resolvedPath + "]: " + e.getMessage(), e);
            }
            return null;
        }
    }


}
