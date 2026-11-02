/*
 * ChangelogRunnerBuilder.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog;

import com.github.toolarium.db.changelog.config.ChangelogConfiguration;
import com.github.toolarium.db.changelog.impl.ChangelogRunnerImpl;
import java.util.List;
import javax.sql.DataSource;


/**
 * Builder for creating an IChangelogRunner instance.
 * 
 * @author patrick
 */
public final class ChangelogRunnerBuilder {

    private final ChangelogConfiguration config;

    /**
     * Constructor for ChangelogRunnerBuilder
     */
    private ChangelogRunnerBuilder() {
        this.config = new ChangelogConfiguration();
    }

    /**
     * Create a new builder instance.
     *
     * @return a new builder
     */
    public static ChangelogRunnerBuilder builder() {
        return new ChangelogRunnerBuilder();
    }

    /**
     * Set the data source.
     *
     * @param dataSource the data source
     * @return this builder
     */
    public ChangelogRunnerBuilder dataSource(DataSource dataSource) {
        config.setDataSource(dataSource);
        return this;
    }

    /**
     * Set the changelog path.
     *
     * @param changelogPath the changelog path
     * @return this builder
     */
    public ChangelogRunnerBuilder changelog(String changelogPath) {
        config.setChangelogPath(changelogPath);
        return this;
    }

    /**
     * Set the tracking table name.
     *
     * @param tableName the tracking table name (default: DB_CHANGELOG)
     * @return this builder
     */
    public ChangelogRunnerBuilder trackingTable(String tableName) {
        config.setTrackingTableName(tableName);
        return this;
    }

    /**
     * Set the lock table name.
     *
     * @param tableName the lock table name (default: DB_CHANGELOG_LOCK)
     * @return this builder
     */
    public ChangelogRunnerBuilder lockTable(String tableName) {
        config.setLockTableName(tableName);
        return this;
    }

    /**
     * Enable or disable leader election for distributed locking.
     *
     * @param enabled true to enable leader election
     * @return this builder
     */
    public ChangelogRunnerBuilder leaderElection(boolean enabled) {
        config.setLeaderElectionEnabled(enabled);
        return this;
    }

    /**
     * Set the active contexts for filtering changesets.
     *
     * @param contexts the contexts
     * @return this builder
     */
    public ChangelogRunnerBuilder contexts(List<String> contexts) {
        config.setContexts(contexts);
        return this;
    }

    /**
     * Set the active labels for filtering changesets.
     *
     * @param labels the labels
     * @return this builder
     */
    public ChangelogRunnerBuilder labels(List<String> labels) {
        config.setLabels(labels);
        return this;
    }

    /**
     * Build the IChangelogRunner instance.
     *
     * @return the configured changelog runner
     * @throws IllegalArgumentException if the configuration is invalid
     */
    public IChangelogRunner build() throws IllegalArgumentException {
        config.validate();
        return new ChangelogRunnerImpl(config);
    }
}
