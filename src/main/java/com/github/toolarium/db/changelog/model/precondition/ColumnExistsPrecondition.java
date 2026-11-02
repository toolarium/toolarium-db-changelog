/*
 * ColumnExistsPrecondition.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog.model.precondition;


/**
 * Checks if a column exists.
 *
 * @author patrick
 */
public class ColumnExistsPrecondition implements IChangelogPrecondition {
    private final String tableName;
    private final String columnName;


    /**
     * Constructor for ColumnExistsPrecondition
     *
     * @param tableName the tableName
     * @param columnName the columnName
     */
    public ColumnExistsPrecondition(String tableName, String columnName) {
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
     * @see com.github.toolarium.db.changelog.model.precondition.IChangelogPrecondition#getDescription()
     */
    @Override
    public String getDescription() {
        return "columnExists: " + tableName + "." + columnName;
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
        ColumnExistsPrecondition other = (ColumnExistsPrecondition) obj;
        return java.util.Objects.equals(tableName, other.tableName) && java.util.Objects.equals(columnName, other.columnName);
    }


    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return getDescription();
    }
}
