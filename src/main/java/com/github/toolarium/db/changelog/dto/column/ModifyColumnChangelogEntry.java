/*
 * ModifyColumnChangelogEntry.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog.dto.column;


import com.github.toolarium.db.changelog.dto.IChangelogEntry;


/**
 * Modifies a column type, nullable, or default. No auto-rollback (original state unknown).
 *
 * @author patrick
 */
public class ModifyColumnChangelogEntry implements IChangelogEntry {
    private final String tableName;
    private final String columnName;
    private final String newType;


    /**
     * Constructor for ModifyColumnChangelogEntry
     *
     * @param tableName the table name
     * @param columnName the column name
     * @param newType the new column type
     */
    public ModifyColumnChangelogEntry(String tableName, String columnName, String newType) {
        this.tableName = tableName;
        this.columnName = columnName;
        this.newType = newType;
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
     * Get the column name.
     *
     * @return the column name
     */
    public String getColumnName() {
        return columnName;
    }


    /**
     * Get the new column type.
     *
     * @return the new type
     */
    public String getNewType() {
        return newType;
    }


    /**
     * @see com.github.toolarium.db.changelog.dto.IChangelogEntry#getDescription()
     */
    @Override
    public String getDescription() {
        return "modifyColumn: " + tableName + "." + columnName;
    }


    /**
     * @see com.github.toolarium.db.changelog.dto.IChangelogEntry#supportsAutoRollback()
     */
    @Override
    public boolean supportsAutoRollback() {
        return false;
    }


    /**
     * @see com.github.toolarium.db.changelog.dto.IChangelogEntry#createAutoRollback()
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
        return java.util.Objects.hash(tableName, columnName, newType);
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
        ModifyColumnChangelogEntry other = (ModifyColumnChangelogEntry) obj;
        return java.util.Objects.equals(tableName, other.tableName)
                && java.util.Objects.equals(columnName, other.columnName)
                && java.util.Objects.equals(newType, other.newType);
    }


    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "ModifyColumnChangelogEntry[" + tableName + "." + columnName + " -> " + newType + "]";
    }
}
