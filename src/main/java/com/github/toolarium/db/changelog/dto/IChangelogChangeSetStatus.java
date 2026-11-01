/*
 * IChangelogChangeSetStatus.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog.dto;

/**
 * Status information for a pending (not yet applied) changeset.
 *
 * @author patrick
 */
public interface IChangelogChangeSetStatus {

    /**
     * Get the changeset identity.
     *
     * @return the identity
     */
    IChangelogChangeSetIdentity getIdentity();

    /**
     * Get the description of the changeset.
     *
     * @return the description
     */
    String getDescription();

    /**
     * Get the comments.
     *
     * @return the comments, or null
     */
    String getComments();

    /**
     * Check if this changeset will be executed.
     *
     * @return true if it will be executed (new or runOnChange/runAlways), false if already applied
     */
    boolean willExecute();
}
