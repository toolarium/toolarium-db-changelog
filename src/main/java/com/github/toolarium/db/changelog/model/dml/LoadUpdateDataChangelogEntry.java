/*
 * LoadUpdateDataChangelogEntry.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog.model.dml;

import com.github.toolarium.db.changelog.model.IChangelogEntry;


/**
 * Bulk upserts rows from a CSV file into a table (inserts new rows, updates existing).
 *
 * @author patrick
 */
public class LoadUpdateDataChangelogEntry implements IChangelogEntry {
    private final String tableName;
    private final String filePath;
    private final String primaryKeyColumn;
    private final String separator;
    private final String encoding;


    /**
     * Constructor for LoadUpdateDataChangelogEntry
     *
     * @param tableName the table name
     * @param filePath the CSV file path
     * @param primaryKeyColumn the primary key column for matching existing rows
     * @param separator the column separator (default comma)
     * @param encoding the file encoding (default UTF-8)
     */
    public LoadUpdateDataChangelogEntry(String tableName, String filePath, String primaryKeyColumn,
                                        String separator, String encoding) { // CHECKSTYLE IGNORE THIS LINE
        this.tableName = tableName;
        this.filePath = filePath;
        this.primaryKeyColumn = primaryKeyColumn;
        if (separator != null) {
            this.separator = separator;
        } else {
            this.separator = ",";
        }
        if (encoding != null) {
            this.encoding = encoding;
        } else {
            this.encoding = "UTF-8";
        }
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
     * Get the CSV file path.
     *
     * @return the file path
     */
    public String getFilePath() {
        return filePath;
    }


    /**
     * Get the primary key column for matching.
     *
     * @return the primary key column
     */
    public String getPrimaryKeyColumn() {
        return primaryKeyColumn;
    }


    /**
     * Get the column separator.
     *
     * @return the separator
     */
    public String getSeparator() {
        return separator;
    }


    /**
     * Get the file encoding.
     *
     * @return the encoding
     */
    public String getEncoding() {
        return encoding;
    }


    /**
     * @see com.github.toolarium.db.changelog.model.IChangelogEntry#getDescription()
     */
    @Override
    public String getDescription() {
        return "loadUpdateData: " + tableName + " from " + filePath;
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
        return java.util.Objects.hash(tableName, filePath, primaryKeyColumn);
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
        LoadUpdateDataChangelogEntry other = (LoadUpdateDataChangelogEntry) obj;
        return java.util.Objects.equals(tableName, other.tableName)
                && java.util.Objects.equals(filePath, other.filePath)
                && java.util.Objects.equals(primaryKeyColumn, other.primaryKeyColumn);
    }


    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return getDescription();
    }
}
