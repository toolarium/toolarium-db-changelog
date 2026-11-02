/*
 * TableIsEmptyPrecondition.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog.model.precondition;

import com.github.toolarium.db.changelog.config.ChangelogConfiguration;


/**
 * Checks if a table is empty (has zero rows).
 *
 * @author patrick
 */
public class TableIsEmptyPrecondition implements IChangelogPrecondition {
    private final String tableName;


    /**
     * Constructor for TableIsEmptyPrecondition
     *
     * @param tableName the table name
     */
    public TableIsEmptyPrecondition(String tableName) {
        ChangelogConfiguration.validateIdentifier(tableName, "tableName");
        this.tableName = tableName;
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
     * @see com.github.toolarium.db.changelog.model.precondition.IChangelogPrecondition#getDescription()
     */
    @Override
    public String getDescription() {
        return "tableIsEmpty: " + tableName;
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
        return java.util.Objects.equals(tableName, ((TableIsEmptyPrecondition) obj).tableName);
    }


    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return getDescription();
    }
}
