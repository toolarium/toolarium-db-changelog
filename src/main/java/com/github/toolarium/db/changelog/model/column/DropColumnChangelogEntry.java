/*
 * DropColumnChangelogEntry.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog.model.column;

import com.github.toolarium.db.changelog.model.IChangelogEntry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * Removes column(s) from a table. No auto-rollback (data is lost).
 *
 * @author patrick
 */
public class DropColumnChangelogEntry implements IChangelogEntry {
    private final String tableName;
    private final List<String> columnNames;


    /**
     * Constructor for DropColumnChangelogEntry
     *
     * @param tableName the table name
     * @param columnNames the column names to drop
     */
    public DropColumnChangelogEntry(String tableName, List<String> columnNames) {
        this.tableName = tableName;
        this.columnNames = Collections.unmodifiableList(new ArrayList<>(columnNames));
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
     * Get the column names to drop.
     *
     * @return the column names
     */
    public List<String> getColumnNames() {
        return columnNames;
    }


    /**
     * @see com.github.toolarium.db.changelog.model.IChangelogEntry#getDescription()
     */
    @Override
    public String getDescription() {
        return "dropColumn: " + tableName + " " + columnNames;
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
        return java.util.Objects.hash(tableName, columnNames);
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
        DropColumnChangelogEntry other = (DropColumnChangelogEntry) obj;
        return java.util.Objects.equals(tableName, other.tableName)
                && java.util.Objects.equals(columnNames, other.columnNames);
    }


    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "DropColumnChangelogEntry[" + tableName + " " + columnNames + "]";
    }
}
