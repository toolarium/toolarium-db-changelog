/*
 * ViewExistsPrecondition.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog.model.precondition;


/**
 * Checks if a view exists.
 *
 * @author patrick
 */
public class ViewExistsPrecondition implements IChangelogPrecondition {
    private final String viewName;


    /**
     * Constructor for ViewExistsPrecondition
     *
     * @param viewName the viewName
     */
    public ViewExistsPrecondition(String viewName) {
        this.viewName = viewName;
    }


    /**
     * Get the viewName.
     *
     * @return the viewName
     */
    public String getViewName() {
        return viewName;
    }


    /**
     * @see com.github.toolarium.db.changelog.model.precondition.IChangelogPrecondition#getDescription()
     */
    @Override
    public String getDescription() {
        return "viewExists: " + viewName;
    }


    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return java.util.Objects.hash(viewName);
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
        ViewExistsPrecondition other = (ViewExistsPrecondition) obj;
        return java.util.Objects.equals(viewName, other.viewName);
    }


    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return getDescription();
    }
}
