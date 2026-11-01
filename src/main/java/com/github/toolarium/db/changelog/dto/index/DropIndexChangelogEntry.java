/*
 * DropIndexChangelogEntry.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog.dto.index;

import com.github.toolarium.db.changelog.dto.IChangelogEntry;

/**
 * Drops an index. No auto-rollback.
 *
 * @author patrick
 */
public class DropIndexChangelogEntry implements IChangelogEntry {
    private final String tableName;
    private final String indexName;


    /**
     * Constructor for DropIndexChangelogEntry
     *
     * @param tableName the table name
     * @param indexName the index name
     */
    public DropIndexChangelogEntry(String tableName, String indexName) {
        this.tableName = tableName;
        this.indexName = indexName;
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
     * @see com.github.toolarium.db.changelog.dto.IChangelogEntry#getDescription()
     */
    @Override
    public String getDescription() {
        return "dropIndex: " + indexName + " on " + tableName;
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
        DropIndexChangelogEntry other = (DropIndexChangelogEntry) obj;
        return java.util.Objects.equals(tableName, other.tableName)
                && java.util.Objects.equals(indexName, other.indexName);
    }


    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "DropIndexChangelogEntry[" + indexName + " on " + tableName + "]";
    }
}
