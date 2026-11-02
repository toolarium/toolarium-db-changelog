/*
 * AddPrimaryKeyChangelogEntry.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog.model.constraint;

import com.github.toolarium.db.changelog.model.IChangelogEntry;


/**
 * Adds a primary key constraint. Auto-rollback: drop primary key.
 *
 * @author patrick
 */
public class AddPrimaryKeyChangelogEntry implements IChangelogEntry {
    private final String tableName;
    private final String constraintName;
    private final String columnNames;


    /**
     * Constructor for AddPrimaryKeyChangelogEntry
     *
     * @param tableName the tableName
     * @param constraintName the constraintName
     * @param columnNames the columnNames
     */
    public AddPrimaryKeyChangelogEntry(String tableName, String constraintName, String columnNames) {
        this.tableName = tableName;
        this.constraintName = constraintName;
        this.columnNames = columnNames;
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
     * Get the constraintName.
     *
     * @return the constraintName
     */
    public String getConstraintName() {
        return constraintName;
    }


    /**
     * Get the columnNames.
     *
     * @return the columnNames
     */
    public String getColumnNames() {
        return columnNames;
    }


    /**
     * @see com.github.toolarium.db.changelog.model.IChangelogEntry#getDescription()
     */
    @Override
    public String getDescription() {
        return "addPrimaryKey: " + constraintName + " on " + tableName;
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
        return new DropPrimaryKeyChangelogEntry(tableName, constraintName);
    }


    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return java.util.Objects.hash(tableName, constraintName, columnNames);
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
        AddPrimaryKeyChangelogEntry other = (AddPrimaryKeyChangelogEntry) obj;
        return java.util.Objects.equals(tableName, other.tableName) && java.util.Objects.equals(constraintName, other.constraintName) && java.util.Objects.equals(columnNames, other.columnNames);
    }


    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "AddPrimaryKeyChangelogEntry[" + tableName + constraintName + columnNames + "]";
    }
}
