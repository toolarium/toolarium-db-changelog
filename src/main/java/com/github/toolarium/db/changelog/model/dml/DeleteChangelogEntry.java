/*
 * DeleteChangelogEntry.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog.model.dml;

import com.github.toolarium.db.changelog.model.IChangelogEntry;


/**
 * Deletes rows from a table.
 *
 * @author patrick
 */
public class DeleteChangelogEntry implements IChangelogEntry {
    private final String tableName;
    private final String whereClause;


    /**
     * Constructor for DeleteChangelogEntry
     *
     * @param tableName the tableName
     * @param whereClause the whereClause
     */
    public DeleteChangelogEntry(String tableName, String whereClause) {
        this.tableName = tableName;
        this.whereClause = whereClause;
    }


    /**
     * Get the tableName.
     *
     * @return the tableName
     */
    public String getTableName() {
        return tableName;
    }


    /**
     * Get the whereClause.
     *
     * @return the whereClause
     */
    public String getWhereClause() {
        return whereClause;
    }


    /**
     * @see com.github.toolarium.db.changelog.model.IChangelogEntry#getDescription()
     */
    @Override
    public String getDescription() {
        return "delete: " + tableName;
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
        return java.util.Objects.hash(tableName, whereClause);
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
        DeleteChangelogEntry other = (DeleteChangelogEntry) obj;
        return java.util.Objects.equals(tableName, other.tableName) && java.util.Objects.equals(whereClause, other.whereClause);
    }


    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return getDescription();
    }
}
