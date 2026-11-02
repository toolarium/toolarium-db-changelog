/*
 * AddColumnChangelogEntry.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog.model.column;

import com.github.toolarium.db.changelog.model.IChangelogEntry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * Adds column(s) to an existing table.
 * Auto-rollback: generates DropColumnChangelogEntry for each added column.
 *
 * @author patrick
 */
public class AddColumnChangelogEntry implements IChangelogEntry {
    private final String tableName;
    private final List<IChangelogColumnDefinition> columns;


    /**
     * Constructor for AddColumnChangelogEntry
     *
     * @param tableName the table name
     * @param columns the columns to add
     */
    public AddColumnChangelogEntry(String tableName, List<IChangelogColumnDefinition> columns) {
        this.tableName = tableName;
        this.columns = Collections.unmodifiableList(new ArrayList<>(columns));
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
     * Get the columns to add.
     *
     * @return the columns
     */
    public List<IChangelogColumnDefinition> getColumns() {
        return columns;
    }


    /**
     * @see com.github.toolarium.db.changelog.model.IChangelogEntry#getDescription()
     */
    @Override
    public String getDescription() {
        return "addColumn: " + tableName;
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
        List<String> columnNames = new ArrayList<>();
        for (IChangelogColumnDefinition col : columns) {
            columnNames.add(col.getName());
        }
        return new DropColumnChangelogEntry(tableName, columnNames);
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
        return java.util.Objects.equals(tableName, ((AddColumnChangelogEntry) obj).tableName);
    }


    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "AddColumnChangelogEntry[" + tableName + "]";
    }
}
