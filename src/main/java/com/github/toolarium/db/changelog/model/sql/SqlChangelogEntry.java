/*
 * SqlChangelogEntry.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog.model.sql;

import com.github.toolarium.db.changelog.model.IChangelogEntry;


/**
 * Executes inline raw SQL.
 *
 * @author patrick
 */
public class SqlChangelogEntry implements IChangelogEntry {
    private final String sql;
    private final boolean splitStatements;
    private final String endDelimiter;
    private final boolean stripComments;
    private final String dbms;

    /**
     * Constructor for SqlChangelogEntry
     *
     * @param sql the SQL to execute
     */
    public SqlChangelogEntry(String sql) {
        this(sql, true, ";", false, null);
    }


    /**
     * Constructor for SqlChangelogEntry
     *
     * @param sql the SQL to execute
     * @param splitStatements whether to split on delimiter
     * @param endDelimiter the statement delimiter
     * @param stripComments whether to strip SQL comments
     * @param dbms the database type restriction, or null for all
     */
    public SqlChangelogEntry(String sql, boolean splitStatements, String endDelimiter, boolean stripComments, String dbms) {
        this.sql = sql;
        this.splitStatements = splitStatements;
        this.endDelimiter = endDelimiter;
        this.stripComments = stripComments;
        this.dbms = dbms;
    }


    /**
     * Get the SQL to execute.
     *
     * @return the SQL
     */
    public String getSql() {
        return sql;
    }


    /**
     * Check if statements should be split on the delimiter.
     *
     * @return true if splitting is enabled
     */
    public boolean isSplitStatements() {
        return splitStatements;
    }


    /**
     * Get the statement delimiter.
     *
     * @return the delimiter
     */
    public String getEndDelimiter() {
        return endDelimiter;
    }


    /**
     * Check if SQL comments should be stripped before execution.
     *
     * @return true if comment stripping is enabled
     */
    public boolean isStripComments() {
        return stripComments;
    }


    /**
     * Get the database type restriction.
     *
     * @return the dbms restriction, or null for all databases
     */
    public String getDbms() {
        return dbms;
    }


    /**
     * @see com.github.toolarium.db.changelog.model.IChangelogEntry#getDescription()
     */
    @Override
    public String getDescription() {
        if (sql == null) {
            return "sql";
        }
        String trimmed = sql.trim();
        if (trimmed.length() <= 80) {
            return "sql: " + trimmed;
        }
        return "sql: " + trimmed.substring(0, 77) + "...";
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
        return java.util.Objects.hash(sql);
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
        return java.util.Objects.equals(sql, ((SqlChangelogEntry) obj).sql);
    }


    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return getDescription();
    }
}
