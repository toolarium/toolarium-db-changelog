/*
 * CreateIndexChangelogEntry.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog.model.index;

import com.github.toolarium.db.changelog.model.IChangelogEntry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * Creates an index on a table.
 * Auto-rollback: generates a DropIndexChangelogEntry.
 *
 * @author patrick
 */
public class CreateIndexChangelogEntry implements IChangelogEntry {
    private final String tableName;
    private final String indexName;
    private final List<String> columnNames;
    private final boolean unique;


    /**
     * Constructor for CreateIndexChangelogEntry
     *
     * @param tableName the table name
     * @param indexName the index name
     * @param columnNames the column names
     * @param unique true for unique index
     */
    public CreateIndexChangelogEntry(String tableName, String indexName, List<String> columnNames, boolean unique) {
        this.tableName = tableName;
        this.indexName = indexName;
        this.columnNames = Collections.unmodifiableList(new ArrayList<>(columnNames));
        this.unique = unique;
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
     * Get the index name.
     *
     * @return the index name
     */
    public String getIndexName() {
        return indexName;
    }


    /**
     * Get the column names.
     *
     * @return the column names
     */
    public List<String> getColumnNames() {
        return columnNames;
    }


    /**
     * Check if this is a unique index.
     *
     * @return true if unique
     */
    public boolean isUnique() {
        return unique;
    }


    /**
     * @see com.github.toolarium.db.changelog.model.IChangelogEntry#getDescription()
     */
    @Override
    public String getDescription() {
        return "createIndex: " + indexName + " on " + tableName;
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
        return new DropIndexChangelogEntry(tableName, indexName);
    }


    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return java.util.Objects.hash(tableName, indexName);
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
        CreateIndexChangelogEntry other = (CreateIndexChangelogEntry) obj;
        return java.util.Objects.equals(tableName, other.tableName)
                && java.util.Objects.equals(indexName, other.indexName);
    }


    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "CreateIndexChangelogEntry[" + indexName + " on " + tableName + "]";
    }
}
