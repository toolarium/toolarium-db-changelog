/*
 * ForeignKeyExistsPrecondition.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog.model.precondition;


/**
 * Checks if a foreign key exists.
 *
 * @author patrick
 */
public class ForeignKeyExistsPrecondition implements IChangelogPrecondition {
    private final String constraintName;


    /**
     * Constructor for ForeignKeyExistsPrecondition
     *
     * @param constraintName the constraintName
     */
    public ForeignKeyExistsPrecondition(String constraintName) {
        this.constraintName = constraintName;
    }


    /**
     * Get the constraintName.
     *
     * @return the constraintName
     */
    public String getConstraintName() {
        return constraintName;
    }


    /**
     * @see com.github.toolarium.db.changelog.model.precondition.IChangelogPrecondition#getDescription()
     */
    @Override
    public String getDescription() {
        return "foreignKeyExists: " + constraintName;
    }


    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return java.util.Objects.hash(constraintName);
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
        ForeignKeyExistsPrecondition other = (ForeignKeyExistsPrecondition) obj;
        return java.util.Objects.equals(constraintName, other.constraintName);
    }


    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return getDescription();
    }
}
