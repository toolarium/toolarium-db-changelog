/*
 * IChangelogEntry.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog.model;

/**
 * A single operation inside a changeset (e.g., createTable, addColumn, sql).
 *
 * @author patrick
 */
public interface IChangelogEntry {

    /**
     * Get a human-readable description of this entry.
     *
     * @return the description
     */
    String getDescription();

    /**
     * Check if this entry type supports auto-generated rollback.
     *
     * @return true if auto-rollback is supported
     */
    boolean supportsAutoRollback();

    /**
     * Create the auto-generated rollback entry for this entry.
     *
     * @return the rollback entry, or null if auto-rollback is not supported
     */
    IChangelogEntry createAutoRollback();
}
