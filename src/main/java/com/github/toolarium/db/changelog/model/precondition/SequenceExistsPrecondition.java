/*
 * SequenceExistsPrecondition.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog.model.precondition;


/**
 * Checks if a sequence exists.
 *
 * @author patrick
 */
public class SequenceExistsPrecondition implements IChangelogPrecondition {
    private final String sequenceName;


    /**
     * Constructor for SequenceExistsPrecondition
     *
     * @param sequenceName the sequenceName
     */
    public SequenceExistsPrecondition(String sequenceName) {
        this.sequenceName = sequenceName;
    }


    /**
     * Get the sequenceName.
     *
     * @return the sequenceName
     */
    public String getSequenceName() {
        return sequenceName;
    }


    /**
     * @see com.github.toolarium.db.changelog.model.precondition.IChangelogPrecondition#getDescription()
     */
    @Override
    public String getDescription() {
        return "sequenceExists: " + sequenceName;
    }


    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return java.util.Objects.hash(sequenceName);
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
        SequenceExistsPrecondition other = (SequenceExistsPrecondition) obj;
        return java.util.Objects.equals(sequenceName, other.sequenceName);
    }


    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return getDescription();
    }
}
