/*
 * SqlCheckPrecondition.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog.model.precondition;


/**
 * Runs SQL and checks result.
 *
 * @author patrick
 */
public class SqlCheckPrecondition implements IChangelogPrecondition {
    private final String sql;
    private final String expectedResult;


    /**
     * Constructor for SqlCheckPrecondition
     *
     * @param sql the sql
     * @param expectedResult the expectedResult
     */
    public SqlCheckPrecondition(String sql, String expectedResult) {
        this.sql = sql;
        this.expectedResult = expectedResult;
    }


    /**
     * Get the sql.
     *
     * @return the sql
     */
    public String getSql() {
        return sql;
    }


    /**
     * Get the expectedResult.
     *
     * @return the expectedResult
     */
    public String getExpectedResult() {
        return expectedResult;
    }


    /**
     * @see com.github.toolarium.db.changelog.model.precondition.IChangelogPrecondition#getDescription()
     */
    @Override
    public String getDescription() {
        return "sqlCheck: " + sql;
    }


    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return java.util.Objects.hash(sql, expectedResult);
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
        SqlCheckPrecondition other = (SqlCheckPrecondition) obj;
        return java.util.Objects.equals(sql, other.sql) && java.util.Objects.equals(expectedResult, other.expectedResult);
    }


    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return getDescription();
    }
}
