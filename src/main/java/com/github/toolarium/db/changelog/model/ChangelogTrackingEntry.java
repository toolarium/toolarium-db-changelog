/*
 * ChangelogTrackingEntry.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog.model;

import java.time.Instant;


/**
 * Implementation of a tracking table row.
 *
 * @author patrick
 */
public class ChangelogTrackingEntry implements IChangelogTrackingEntry {
    private String id;
    private String author;
    private String filepath;
    private String checksum;
    private Instant dateExecuted;
    private int orderExecuted;
    private ChangelogExecutionType executionType;
    private String description;
    private String comments;
    private String tag;
    private String deploymentId;

    /**
     * Constructor for ChangelogTrackingEntry
     */
    public ChangelogTrackingEntry() {
    }


    /**
     * @see com.github.toolarium.db.changelog.model.IChangelogChangeSetIdentity#getId()
     */
    @Override
    public String getId() {
        return id;
    }


    /**
     * Set the id.
     *
     * @param id the id
     */
    public void setId(String id) {
        this.id = id;
    }


    /**
     * @see com.github.toolarium.db.changelog.model.IChangelogChangeSetIdentity#getAuthor()
     */
    @Override
    public String getAuthor() {
        return author;
    }


    /**
     * Set the author.
     *
     * @param author the author
     */
    public void setAuthor(String author) {
        this.author = author;
    }


    /**
     * @see com.github.toolarium.db.changelog.model.IChangelogChangeSetIdentity#getFilepath()
     */
    @Override
    public String getFilepath() {
        return filepath;
    }


    /**
     * Set the filepath.
     *
     * @param filepath the filepath
     */
    public void setFilepath(String filepath) {
        this.filepath = filepath;
    }


    /**
     * @see com.github.toolarium.db.changelog.model.IChangelogTrackingEntry#getChecksum()
     */
    @Override
    public String getChecksum() {
        return checksum;
    }


    /**
     * Set the checksum.
     *
     * @param checksum the checksum
     */
    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }


    /**
     * @see com.github.toolarium.db.changelog.model.IChangelogTrackingEntry#getDateExecuted()
     */
    @Override
    public Instant getDateExecuted() {
        return dateExecuted;
    }


    /**
     * Set the date executed.
     *
     * @param dateExecuted the date executed
     */
    public void setDateExecuted(Instant dateExecuted) {
        this.dateExecuted = dateExecuted;
    }


    /**
     * @see com.github.toolarium.db.changelog.model.IChangelogTrackingEntry#getOrderExecuted()
     */
    @Override
    public int getOrderExecuted() {
        return orderExecuted;
    }


    /**
     * Set the order executed.
     *
     * @param orderExecuted the order executed
     */
    public void setOrderExecuted(int orderExecuted) {
        this.orderExecuted = orderExecuted;
    }


    /**
     * @see com.github.toolarium.db.changelog.model.IChangelogTrackingEntry#getExecutionType()
     */
    @Override
    public ChangelogExecutionType getExecutionType() {
        return executionType;
    }


    /**
     * Set the execution type.
     *
     * @param executionType the execution type
     */
    public void setExecutionType(ChangelogExecutionType executionType) {
        this.executionType = executionType;
    }


    /**
     * @see com.github.toolarium.db.changelog.model.IChangelogTrackingEntry#getDescription()
     */
    @Override
    public String getDescription() {
        return description;
    }


    /**
     * Set the description.
     *
     * @param description the description
     */
    public void setDescription(String description) {
        this.description = description;
    }


    /**
     * @see com.github.toolarium.db.changelog.model.IChangelogTrackingEntry#getComments()
     */
    @Override
    public String getComments() {
        return comments;
    }


    /**
     * Set the comments.
     *
     * @param comments the comments
     */
    public void setComments(String comments) {
        this.comments = comments;
    }


    /**
     * @see com.github.toolarium.db.changelog.model.IChangelogTrackingEntry#getTag()
     */
    @Override
    public String getTag() {
        return tag;
    }


    /**
     * Set the tag.
     *
     * @param tag the tag
     */
    public void setTag(String tag) {
        this.tag = tag;
    }


    /**
     * @see com.github.toolarium.db.changelog.model.IChangelogTrackingEntry#getDeploymentId()
     */
    @Override
    public String getDeploymentId() {
        return deploymentId;
    }


    /**
     * Set the deployment id.
     *
     * @param deploymentId the deployment id
     */
    public void setDeploymentId(String deploymentId) {
        this.deploymentId = deploymentId;
    }


    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return java.util.Objects.hash(id, author, filepath);
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
        ChangelogTrackingEntry other = (ChangelogTrackingEntry) obj;
        return java.util.Objects.equals(id, other.id)
                && java.util.Objects.equals(author, other.author)
                && java.util.Objects.equals(filepath, other.filepath);
    }


    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return id + "::" + author + "::" + filepath;
    }
}
