/*
 * ChangeSetExecutedPrecondition.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog.model.precondition;


/**
 * Checks if changeset applied.
 *
 * @author patrick
 */
public class ChangeSetExecutedPrecondition implements IChangelogPrecondition {
    private final String changeSetId;
    private final String author;


    /**
     * Constructor for ChangeSetExecutedPrecondition
     *
     * @param changeSetId the changeSetId
     * @param author the author
     */
    public ChangeSetExecutedPrecondition(String changeSetId, String author) {
        this.changeSetId = changeSetId;
        this.author = author;
    }


    /**
     * Get the changeSetId.
     *
     * @return the changeSetId
     */
    public String getChangeSetId() {
        return changeSetId;
    }


    /**
     * Get the author.
     *
     * @return the author
     */
    public String getAuthor() {
        return author;
    }


    /**
     * @see com.github.toolarium.db.changelog.model.precondition.IChangelogPrecondition#getDescription()
     */
    @Override
    public String getDescription() {
        return "changeSetExecuted: " + changeSetId + "::" + author;
    }


    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return java.util.Objects.hash(changeSetId, author);
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
        ChangeSetExecutedPrecondition other = (ChangeSetExecutedPrecondition) obj;
        return java.util.Objects.equals(changeSetId, other.changeSetId) && java.util.Objects.equals(author, other.author);
    }


    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return getDescription();
    }
}
