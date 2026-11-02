/*
 * DropViewChangelogEntry.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog.model.view;

import com.github.toolarium.db.changelog.model.IChangelogEntry;


/**
 * Drops a view. No auto-rollback.
 *
 * @author patrick
 */
public class DropViewChangelogEntry implements IChangelogEntry {
    private final String viewName;


    /**
     * Constructor for DropViewChangelogEntry
     *
     * @param viewName the viewName
     */
    public DropViewChangelogEntry(String viewName) {
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
     * @see com.github.toolarium.db.changelog.model.IChangelogEntry#getDescription()
     */
    @Override
    public String getDescription() {
        return "dropView: " + viewName;
    }


    /**
     * @see com.github.toolarium.db.changelog.model.IChangelogEntry#supportsAutoRollback()
     */
    @Override
    public boolean supportsAutoRollback() {
        return false;
    }


    /**
     * @see com.github.toolarium.db.changelog.model.IChangelogEntry#createAutoRollback()
     */
    @Override
    public IChangelogEntry createAutoRollback() {
        return null;
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
        DropViewChangelogEntry other = (DropViewChangelogEntry) obj;
        return java.util.Objects.equals(viewName, other.viewName);
    }


    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "DropViewChangelogEntry[" + viewName + "]";
    }
}
