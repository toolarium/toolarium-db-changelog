/*
 * IChangelogTrackingEntry.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog.model;

import java.time.Instant;


/**
 * A record from the DB_CHANGELOG tracking table representing an applied changeset.
 *
 * @author patrick
 */
public interface IChangelogTrackingEntry extends IChangelogChangeSetIdentity {

    /**
     * Get the SHA-256 checksum.
     *
     * @return the checksum, or null if not yet computed
     */
    String getChecksum();

    /**
     * Get the timestamp when this changeset was executed.
     *
     * @return the execution timestamp
     */
    Instant getDateExecuted();

    /**
     * Get the execution order.
     *
     * @return the order in which this changeset was executed
     */
    int getOrderExecuted();

    /**
     * Get the execution type.
     *
     * @return the execution type
     */
    ChangelogExecutionType getExecutionType();

    /**
     * Get the description.
     *
     * @return the description, or null
     */
    String getDescription();

    /**
     * Get the comments.
     *
     * @return the comments, or null
     */
    String getComments();

    /**
     * Get the tag.
     *
     * @return the tag, or null
     */
    String getTag();

    /**
     * Get the deployment id.
     *
     * @return the deployment id, or null
     */
    String getDeploymentId();
}
