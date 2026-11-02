/*
 * ChangelogConfiguration.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog.config;

import java.util.List;
import java.util.regex.Pattern;
import javax.sql.DataSource;


/**
 * Runtime configuration for the changelog runner.
 *
 * @author patrick
 */
public class ChangelogConfiguration {
    /** Pattern for valid SQL identifiers (table names, column names). */
    public static final Pattern IDENTIFIER_PATTERN = Pattern.compile("[A-Za-z_][A-Za-z0-9_]*");

    /** Default tracking table name. */
    public static final String DEFAULT_TRACKING_TABLE = "DB_CHANGELOG";

    /** Default lock table name. */
    public static final String DEFAULT_LOCK_TABLE = "DB_CHANGELOG_LOCK";

    private DataSource dataSource;
    private String changelogPath;
    private String trackingTableName;
    private String lockTableName;
    private boolean leaderElectionEnabled;
    private List<String> contexts;
    private List<String> labels;
    private String defaultAuthor;
    private ChangelogSnapshotPolicy snapshotPolicy;
    private ChangelogRollbackPolicy rollbackPolicy;
    private ChangelogObjectQuotingStrategy objectQuotingStrategy;

    /**
     * Constructor for ChangelogConfiguration
     */
    public ChangelogConfiguration() {
        this.trackingTableName = DEFAULT_TRACKING_TABLE;
        this.lockTableName = DEFAULT_LOCK_TABLE;
        this.leaderElectionEnabled = false;
        this.defaultAuthor = System.getProperty("user.name", "default");
        this.snapshotPolicy = ChangelogSnapshotPolicy.NONE;
        this.rollbackPolicy = ChangelogRollbackPolicy.WARN;
        this.objectQuotingStrategy = ChangelogObjectQuotingStrategy.NONE;
    }

    /**
     * Get the data source.
     *
     * @return the data source
     */
    public DataSource getDataSource() {
        return dataSource;
    }

    /**
     * Set the data source.
     *
     * @param dataSource the data source
     * @return this configuration for chaining
     */
    public ChangelogConfiguration setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
        return this;
    }

    /**
     * Get the changelog path.
     *
     * @return the changelog path
     */
    public String getChangelogPath() {
        return changelogPath;
    }

    /**
     * Set the changelog path.
     *
     * @param changelogPath the changelog path
     * @return this configuration for chaining
     */
    public ChangelogConfiguration setChangelogPath(String changelogPath) {
        this.changelogPath = changelogPath;
        return this;
    }

    /**
     * Get the tracking table name.
     *
     * @return the tracking table name
     */
    public String getTrackingTableName() {
        return trackingTableName;
    }

    /**
     * Set the tracking table name.
     *
     * @param trackingTableName the tracking table name
     * @return this configuration for chaining
     */
    public ChangelogConfiguration setTrackingTableName(String trackingTableName) {
        this.trackingTableName = trackingTableName;
        return this;
    }

    /**
     * Get the lock table name.
     *
     * @return the lock table name
     */
    public String getLockTableName() {
        return lockTableName;
    }

    /**
     * Set the lock table name.
     *
     * @param lockTableName the lock table name
     * @return this configuration for chaining
     */
    public ChangelogConfiguration setLockTableName(String lockTableName) {
        this.lockTableName = lockTableName;
        return this;
    }

    /**
     * Check if leader election is enabled.
     *
     * @return true if leader election is enabled
     */
    public boolean isLeaderElectionEnabled() {
        return leaderElectionEnabled;
    }

    /**
     * Set whether leader election is enabled.
     *
     * @param leaderElectionEnabled true to enable leader election
     * @return this configuration for chaining
     */
    public ChangelogConfiguration setLeaderElectionEnabled(boolean leaderElectionEnabled) {
        this.leaderElectionEnabled = leaderElectionEnabled;
        return this;
    }

    /**
     * Get the active contexts.
     *
     * @return the contexts, or null if none
     */
    public List<String> getContexts() {
        return contexts;
    }

    /**
     * Set the active contexts.
     *
     * @param contexts the contexts
     * @return this configuration for chaining
     */
    public ChangelogConfiguration setContexts(List<String> contexts) {
        this.contexts = contexts;
        return this;
    }

    /**
     * Get the active labels.
     *
     * @return the labels, or null if none
     */
    public List<String> getLabels() {
        return labels;
    }

    /**
     * Set the active labels.
     *
     * @param labels the labels
     * @return this configuration for chaining
     */
    public ChangelogConfiguration setLabels(List<String> labels) {
        this.labels = labels;
        return this;
    }

    /**
     * Get the default author.
     *
     * @return the default author
     */
    public String getDefaultAuthor() {
        return defaultAuthor;
    }

    /**
     * Set the default author.
     *
     * @param defaultAuthor the default author
     * @return this configuration for chaining
     */
    public ChangelogConfiguration setDefaultAuthor(String defaultAuthor) {
        this.defaultAuthor = defaultAuthor;
        return this;
    }


    /**
     * Get the snapshot policy.
     *
     * @return the snapshot policy
     */
    public ChangelogSnapshotPolicy getSnapshotPolicy() {
        return snapshotPolicy;
    }


    /**
     * Set the snapshot policy.
     *
     * @param snapshotPolicy the snapshot policy
     * @return this configuration for chaining
     */
    public ChangelogConfiguration setSnapshotPolicy(ChangelogSnapshotPolicy snapshotPolicy) {
        this.snapshotPolicy = snapshotPolicy;
        return this;
    }


    /**
     * Get the rollback policy.
     *
     * @return the rollback policy
     */
    public ChangelogRollbackPolicy getRollbackPolicy() {
        return rollbackPolicy;
    }


    /**
     * Set the rollback policy.
     *
     * @param rollbackPolicy the rollback policy
     * @return this configuration for chaining
     */
    public ChangelogConfiguration setRollbackPolicy(ChangelogRollbackPolicy rollbackPolicy) {
        this.rollbackPolicy = rollbackPolicy;
        return this;
    }


    /**
     * Get the object quoting strategy.
     *
     * @return the object quoting strategy
     */
    public ChangelogObjectQuotingStrategy getObjectQuotingStrategy() {
        return objectQuotingStrategy;
    }


    /**
     * Set the object quoting strategy.
     *
     * @param objectQuotingStrategy the object quoting strategy
     * @return this configuration for chaining
     */
    public ChangelogConfiguration setObjectQuotingStrategy(ChangelogObjectQuotingStrategy objectQuotingStrategy) {
        this.objectQuotingStrategy = objectQuotingStrategy;
        return this;
    }


    /**
     * Validate the configuration.
     *
     * @throws IllegalArgumentException if the configuration is invalid
     */
    public void validate() throws IllegalArgumentException {
        if (dataSource == null) {
            throw new IllegalArgumentException("DataSource must not be null.");
        }
        validateIdentifier(trackingTableName, "trackingTableName");
        validateIdentifier(lockTableName, "lockTableName");
    }


    /**
     * Validate that a SQL identifier contains only safe characters.
     *
     * @param identifier the identifier to validate
     * @param name the name for error messages
     * @throws IllegalArgumentException if the identifier is invalid
     */
    public static void validateIdentifier(String identifier, String name) {
        if (identifier == null || identifier.isEmpty()) {
            throw new IllegalArgumentException(name + " must not be null or empty.");
        }
        if (!IDENTIFIER_PATTERN.matcher(identifier).matches()) {
            throw new IllegalArgumentException(name + " contains invalid characters: " + identifier);
        }
    }


    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return java.util.Objects.hash(trackingTableName, lockTableName, changelogPath, defaultAuthor, leaderElectionEnabled);
    }


    /**
     * @see java.lang.Object#equals(Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        ChangelogConfiguration other = (ChangelogConfiguration) obj;
        return java.util.Objects.equals(trackingTableName, other.trackingTableName)
                && java.util.Objects.equals(lockTableName, other.lockTableName)
                && java.util.Objects.equals(changelogPath, other.changelogPath)
                && java.util.Objects.equals(defaultAuthor, other.defaultAuthor)
                && leaderElectionEnabled == other.leaderElectionEnabled;
    }


    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "ChangelogConfiguration [trackingTable=" + trackingTableName
                + ", lockTable=" + lockTableName
                + ", changelog=" + changelogPath
                + ", leaderElection=" + leaderElectionEnabled
                + ", author=" + defaultAuthor + "]";
    }
}
