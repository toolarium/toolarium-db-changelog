/*
 * DropForeignKeyChangelogEntry.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog.model.constraint;

import com.github.toolarium.db.changelog.model.IChangelogEntry;


/**
 * Drops a foreign key constraint. No auto-rollback.
 *
 * @author patrick
 */
public class DropForeignKeyChangelogEntry implements IChangelogEntry {
    private final String tableName;
    private final String constraintName;


    /**
     * Constructor for DropForeignKeyChangelogEntry
     *
     * @param tableName the tableName
     * @param constraintName the constraintName
     */
    public DropForeignKeyChangelogEntry(String tableName, String constraintName) {
        this.tableName = tableName;
        this.constraintName = constraintName;
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
     * @see com.github.toolarium.db.changelog.model.IChangelogEntry#getDescription()
     */
    @Override
    public String getDescription() {
        return "dropForeignKey: " + constraintName + " on " + tableName;
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
        return java.util.Objects.hash(tableName, constraintName);
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
        DropForeignKeyChangelogEntry other = (DropForeignKeyChangelogEntry) obj;
        return java.util.Objects.equals(tableName, other.tableName) && java.util.Objects.equals(constraintName, other.constraintName);
    }


    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "DropForeignKeyChangelogEntry[" + tableName + constraintName + "]";
    }
}
