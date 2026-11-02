/*
 * SetTableRemarksChangelogEntry.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog.model.dml;

import com.github.toolarium.db.changelog.model.IChangelogEntry;


/**
 * Sets a comment on a table.
 *
 * @author patrick
 */
public class SetTableRemarksChangelogEntry implements IChangelogEntry {
    private final String tableName;
    private final String remarks;


    /**
     * Constructor for SetTableRemarksChangelogEntry
     *
     * @param tableName the table name
     * @param remarks the comment to set
     */
    public SetTableRemarksChangelogEntry(String tableName, String remarks) {
        this.tableName = tableName;
        this.remarks = remarks;
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
     * Get the remarks.
     *
     * @return the remarks
     */
    public String getRemarks() {
        return remarks;
    }


    /**
     * @see com.github.toolarium.db.changelog.model.IChangelogEntry#getDescription()
     */
    @Override
    public String getDescription() {
        return "setTableRemarks: " + tableName;
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
        return java.util.Objects.hash(tableName, remarks);
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
        SetTableRemarksChangelogEntry other = (SetTableRemarksChangelogEntry) obj;
        return java.util.Objects.equals(tableName, other.tableName)
                && java.util.Objects.equals(remarks, other.remarks);
    }


    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return getDescription();
    }
}
