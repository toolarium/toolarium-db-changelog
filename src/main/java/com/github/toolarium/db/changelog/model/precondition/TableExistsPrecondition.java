/*
 * TableExistsPrecondition.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog.model.precondition;


/**
 * Checks if a table exists.
 *
 * @author patrick
 */
public class TableExistsPrecondition implements IChangelogPrecondition {
    private final String tableName;


    /**
     * Constructor for TableExistsPrecondition
     *
     * @param tableName the tableName
     */
    public TableExistsPrecondition(String tableName) {
        this.tableName = tableName;
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
     * @see com.github.toolarium.db.changelog.model.precondition.IChangelogPrecondition#getDescription()
     */
    @Override
    public String getDescription() {
        return "tableExists: " + tableName;
    }


    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return java.util.Objects.hash(tableName);
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
        TableExistsPrecondition other = (TableExistsPrecondition) obj;
        return java.util.Objects.equals(tableName, other.tableName);
    }


    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return getDescription();
    }
}
