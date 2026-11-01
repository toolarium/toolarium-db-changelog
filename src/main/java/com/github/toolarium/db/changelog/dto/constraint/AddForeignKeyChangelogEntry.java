/*
 * AddForeignKeyChangelogEntry.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog.dto.constraint;


import com.github.toolarium.db.changelog.dto.IChangelogEntry;


/**
 * Adds a foreign key constraint. Auto-rollback: drop foreign key.
 *
 * @author patrick
 */
public class AddForeignKeyChangelogEntry implements IChangelogEntry {
    private final String tableName;
    private final String constraintName;
    private final String columnNames;
    private final String referencedTableName;
    private final String referencedColumnNames;


    /**
     * Constructor for AddForeignKeyChangelogEntry
     *
     * @param tableName the tableName
     * @param constraintName the constraintName
     * @param columnNames the columnNames
     * @param referencedTableName the referencedTableName
     * @param referencedColumnNames the referencedColumnNames
     */
    public AddForeignKeyChangelogEntry(String tableName, String constraintName, String columnNames, String referencedTableName, String referencedColumnNames) {
        this.tableName = tableName;
        this.constraintName = constraintName;
        this.columnNames = columnNames;
        this.referencedTableName = referencedTableName;
        this.referencedColumnNames = referencedColumnNames;
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
     * Get the referencedTableName.
     *
     * @return the referencedTableName
     */
    public String getReferencedTableName() {
        return referencedTableName;
    }


    /**
     * Get the referencedColumnNames.
     *
     * @return the referencedColumnNames
     */
    public String getReferencedColumnNames() {
        return referencedColumnNames;
    }


    /**
     * @see com.github.toolarium.db.changelog.dto.IChangelogEntry#getDescription()
     */
    @Override
    public String getDescription() {
        return "addForeignKey: " + constraintName + " on " + tableName;
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
        return new DropForeignKeyChangelogEntry(tableName, constraintName);
    }


    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return java.util.Objects.hash(tableName, constraintName, columnNames, referencedTableName, referencedColumnNames);
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
        AddForeignKeyChangelogEntry other = (AddForeignKeyChangelogEntry) obj;
        return java.util.Objects.equals(tableName, other.tableName)
                && java.util.Objects.equals(constraintName, other.constraintName)
                && java.util.Objects.equals(columnNames, other.columnNames)
                && java.util.Objects.equals(referencedTableName, other.referencedTableName)
                && java.util.Objects.equals(referencedColumnNames, other.referencedColumnNames);
    }


    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "AddForeignKeyChangelogEntry[" + tableName + constraintName + columnNames + referencedTableName + referencedColumnNames + "]";
    }
}
