/*
 * RenameColumnChangelogEntry.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog.model.column;

import com.github.toolarium.db.changelog.model.IChangelogEntry;


/**
 * Renames a column. Auto-rollback: reverse rename.
 *
 * @author patrick
 */
public class RenameColumnChangelogEntry implements IChangelogEntry {
    private final String tableName;
    private final String oldColumnName;
    private final String newColumnName;


    /**
     * Constructor for RenameColumnChangelogEntry
     *
     * @param tableName the table name
     * @param oldColumnName the current column name
     * @param newColumnName the new column name
     */
    public RenameColumnChangelogEntry(String tableName, String oldColumnName, String newColumnName) {
        this.tableName = tableName;
        this.oldColumnName = oldColumnName;
        this.newColumnName = newColumnName;
    }


    /**
     * Get the table name.
     *
     * @return the table name
     */
    public String getTableName() {
        return tableName;
    }


    /**
     * Get the current column name.
     *
     * @return the old column name
     */
    public String getOldColumnName() {
        return oldColumnName;
    }


    /**
     * Get the new column name.
     *
     * @return the new column name
     */
    public String getNewColumnName() {
        return newColumnName;
    }


    /**
     * @see com.github.toolarium.db.changelog.model.IChangelogEntry#getDescription()
     */
    @Override
    public String getDescription() {
        return "renameColumn: " + tableName + "." + oldColumnName + " to " + newColumnName;
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
        return new RenameColumnChangelogEntry(tableName, newColumnName, oldColumnName);
    }


    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return java.util.Objects.hash(tableName, oldColumnName, newColumnName);
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
        RenameColumnChangelogEntry other = (RenameColumnChangelogEntry) obj;
        return java.util.Objects.equals(tableName, other.tableName)
                && java.util.Objects.equals(oldColumnName, other.oldColumnName)
                && java.util.Objects.equals(newColumnName, other.newColumnName);
    }


    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "RenameColumnChangelogEntry[" + tableName + "." + oldColumnName + " -> " + newColumnName + "]";
    }
}
