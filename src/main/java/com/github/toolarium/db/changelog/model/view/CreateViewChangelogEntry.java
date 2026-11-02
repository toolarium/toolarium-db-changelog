/*
 * CreateViewChangelogEntry.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog.model.view;

import com.github.toolarium.db.changelog.model.IChangelogEntry;


/**
 * Creates or replaces a view. Auto-rollback: drop view.
 *
 * @author patrick
 */
public class CreateViewChangelogEntry implements IChangelogEntry {
    private final String viewName;
    private final String selectQuery;


    /**
     * Constructor for CreateViewChangelogEntry
     *
     * @param viewName the viewName
     * @param selectQuery the selectQuery
     */
    public CreateViewChangelogEntry(String viewName, String selectQuery) {
        this.viewName = viewName;
        this.selectQuery = selectQuery;
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
     * Get the selectQuery.
     *
     * @return the selectQuery
     */
    public String getSelectQuery() {
        return selectQuery;
    }


    /**
     * @see com.github.toolarium.db.changelog.model.IChangelogEntry#getDescription()
     */
    @Override
    public String getDescription() {
        return "createView: " + viewName;
    }


    /**
     * @see com.github.toolarium.db.changelog.model.IChangelogEntry#supportsAutoRollback()
     */
    @Override
    public boolean supportsAutoRollback() {
        return true;
    }


    /**
     * @see com.github.toolarium.db.changelog.model.IChangelogEntry#createAutoRollback()
     */
    @Override
    public IChangelogEntry createAutoRollback() {
        return new DropViewChangelogEntry(viewName);
    }


    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return java.util.Objects.hash(viewName, selectQuery);
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
        CreateViewChangelogEntry other = (CreateViewChangelogEntry) obj;
        return java.util.Objects.equals(viewName, other.viewName) && java.util.Objects.equals(selectQuery, other.selectQuery);
    }


    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "CreateViewChangelogEntry[" + viewName + selectQuery + "]";
    }
}
