/*
 * IChangelogChangeSet.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog.dto;


import java.util.List;

/**
 * A single migration unit containing one or more changelog entries.
 *
 * @author patrick
 */
public interface IChangelogChangeSet {

    /**
     * Get the changeset identity (id + author + filepath).
     *
     * @return the identity
     */
    IChangelogChangeSetIdentity getIdentity();

    /**
     * Get the changelog entries (changes) in this changeset.
     *
     * @return the list of entries
     */
    List<IChangelogEntry> getEntries();

    /**
     * Get the rollback entries. If empty and entries support auto-rollback,
     * auto-rollback will be used.
     *
     * @return the list of rollback entries, or empty list
     */
    List<IChangelogEntry> getRollbackEntries();

    /**
     * Get the comment for this changeset.
     *
     * @return the comment, or null
     */
    String getComment();

    /**
     * Check if this changeset should run every time.
     *
     * @return true if runAlways
     */
    boolean isRunAlways();

    /**
     * Check if this changeset should re-run when its checksum changes.
     *
     * @return true if runOnChange
     */
    boolean isRunOnChange();

    /**
     * Check if execution should halt on error.
     *
     * @return true if failOnError (default true)
     */
    boolean isFailOnError();

    /**
     * Check if this changeset should be wrapped in a transaction.
     *
     * @return true if runInTransaction (default true)
     */
    boolean isRunInTransaction();

    /**
     * Get the context filter expressions.
     *
     * @return the contexts, or empty list if none
     */
    List<String> getContexts();

    /**
     * Get the label filter expressions.
     *
     * @return the labels, or empty list if none
     */
    List<String> getLabels();
}
