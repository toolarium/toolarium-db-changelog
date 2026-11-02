/*
 * OrPrecondition.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog.model.precondition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * Logical OR combinator.
 *
 * @author patrick
 */
public class OrPrecondition implements IChangelogPrecondition {
    private final List<IChangelogPrecondition> preconditions;


    /**
     * Constructor for OrPrecondition
     *
     * @param preconditions the preconditions
     */
    public OrPrecondition(List<IChangelogPrecondition> preconditions) {
        this.preconditions = Collections.unmodifiableList(new ArrayList<>(preconditions));
    }


    /**
     * Get the preconditions.
     *
     * @return the preconditions
     */
    public List<IChangelogPrecondition> getPreconditions() {
        return preconditions;
    }


    /**
     * @see com.github.toolarium.db.changelog.model.precondition.IChangelogPrecondition#getDescription()
     */
    @Override
    public String getDescription() {
        return "or: " + preconditions.size() + " condition(s)";
    }


    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return java.util.Objects.hash(preconditions);
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
        return java.util.Objects.equals(preconditions, ((OrPrecondition) obj).preconditions);
    }


    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return getDescription();
    }
}
