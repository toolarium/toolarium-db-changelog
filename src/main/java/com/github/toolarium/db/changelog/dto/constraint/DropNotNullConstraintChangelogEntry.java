/*
 * DropNotNullConstraintChangelogEntry.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog.dto.constraint;


import com.github.toolarium.db.changelog.dto.IChangelogEntry;


/**
 * Drops NOT NULL from a column. Auto-rollback: add NOT NULL.
 *
 * @author patrick
 */
public class DropNotNullConstraintChangelogEntry implements IChangelogEntry {
    private final String tableName;
    private final String columnName;


    /**
     * Constructor for DropNotNullConstraintChangelogEntry
     *
     * @param tableName the tableName
     * @param columnName the columnName
     */
    public DropNotNullConstraintChangelogEntry(String tableName, String columnName) {
        this.tableName = tableName;
        this.columnName = columnName;
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
     * Get the columnName.
     *
     * @return the columnName
     */
    public String getColumnName() {
        return columnName;
    }


    /**
     * @see com.github.toolarium.db.changelog.dto.IChangelogEntry#getDescription()
     */
    @Override
    public String getDescription() {
        return "dropNotNull: " + tableName + "." + columnName;
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
        return new AddNotNullConstraintChangelogEntry(tableName, columnName);
    }


    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return java.util.Objects.hash(tableName, columnName);
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
        DropNotNullConstraintChangelogEntry other = (DropNotNullConstraintChangelogEntry) obj;
        return java.util.Objects.equals(tableName, other.tableName) && java.util.Objects.equals(columnName, other.columnName);
    }


    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "DropNotNullConstraintChangelogEntry[" + tableName + columnName + "]";
    }
}
