/*
 * DropTableChangelogEntry.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog.dto.table;
import com.github.toolarium.db.changelog.dto.IChangelogEntry;

/**
 * Drops an existing table. No auto-rollback (data is lost).
 *
 * @author patrick
 */
public class DropTableChangelogEntry implements IChangelogEntry {
    private final String tableName;


    /**
     * Constructor for DropTableChangelogEntry
     *
     * @param tableName the table name
     */
    public DropTableChangelogEntry(String tableName) {
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
     * @see com.github.toolarium.db.changelog.dto.IChangelogEntry#getDescription()
     */
    @Override
    public String getDescription() {
        return "dropTable: " + tableName;
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
        return java.util.Objects.equals(tableName, ((DropTableChangelogEntry) obj).tableName);
    }


    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "DropTableChangelogEntry[" + tableName + "]";
    }
}
