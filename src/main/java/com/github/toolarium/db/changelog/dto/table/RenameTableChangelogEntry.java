/*
 * RenameTableChangelogEntry.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog.dto.table;


import com.github.toolarium.db.changelog.dto.IChangelogEntry;


/**
 * Renames a table. Auto-rollback: reverse rename.
 *
 * @author patrick
 */
public class RenameTableChangelogEntry implements IChangelogEntry {
    private final String oldTableName;
    private final String newTableName;


    /**
     * Constructor for RenameTableChangelogEntry
     *
     * @param oldTableName the current table name
     * @param newTableName the new table name
     */
    public RenameTableChangelogEntry(String oldTableName, String newTableName) {
        this.oldTableName = oldTableName;
        this.newTableName = newTableName;
    }


    /**
     * Get the current table name.
     *
     * @return the old table name
     */
    public String getOldTableName() {
        return oldTableName;
    }


    /**
     * Get the new table name.
     *
     * @return the new table name
     */
    public String getNewTableName() {
        return newTableName;
    }


    /**
     * @see com.github.toolarium.db.changelog.dto.IChangelogEntry#getDescription()
     */
    @Override
    public String getDescription() {
        return "renameTable: " + oldTableName + " to " + newTableName;
    }


    /**
     * @see com.github.toolarium.db.changelog.dto.IChangelogEntry#supportsAutoRollback()
     */
    @Override
    public boolean supportsAutoRollback() {
        return true;
    }


    /**
     * @see com.github.toolarium.db.changelog.dto.IChangelogEntry#createAutoRollback()
     */
    @Override
    public IChangelogEntry createAutoRollback() {
        return new RenameTableChangelogEntry(newTableName, oldTableName);
    }


    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return java.util.Objects.hash(oldTableName, newTableName);
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
        RenameTableChangelogEntry other = (RenameTableChangelogEntry) obj;
        return java.util.Objects.equals(oldTableName, other.oldTableName)
                && java.util.Objects.equals(newTableName, other.newTableName);
    }


    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "RenameTableChangelogEntry[" + oldTableName + " -> " + newTableName + "]";
    }
}
