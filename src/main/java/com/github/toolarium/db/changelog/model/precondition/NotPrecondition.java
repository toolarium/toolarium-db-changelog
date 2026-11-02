/*
 * NotPrecondition.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog.model.precondition;


/**
 * Logical NOT combinator.
 *
 * @author patrick
 */
public class NotPrecondition implements IChangelogPrecondition {
    private final IChangelogPrecondition precondition;


    /**
     * Constructor for NotPrecondition
     *
     * @param precondition the precondition to negate
     */
    public NotPrecondition(IChangelogPrecondition precondition) {
        this.precondition = precondition;
    }


    /**
     * Get the negated precondition.
     *
     * @return the precondition
     */
    public IChangelogPrecondition getPrecondition() {
        return precondition;
    }


    /**
     * @see com.github.toolarium.db.changelog.model.precondition.IChangelogPrecondition#getDescription()
     */
    @Override
    public String getDescription() {
        return "not: " + precondition.getDescription();
    }


    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return java.util.Objects.hash(precondition);
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
        NotPrecondition other = (NotPrecondition) obj;
        return java.util.Objects.equals(precondition, other.precondition);
    }


    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return getDescription();
    }
}
