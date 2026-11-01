/*
 * IChangelogSqlRollbackGenerator.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog.impl.rollback;


/**
 * Parses raw SQL statements and generates rollback SQL for common DDL patterns.
 *
 * @author patrick
 */
public interface IChangelogSqlRollbackGenerator {

    /**
     * Attempt to generate rollback SQL for the given SQL statement.
     *
     * @param sql the forward SQL statement
     * @return the rollback SQL, or null if the pattern is not recognized
     */
    String generateRollbackSql(String sql);


    /**
     * Check if this generator can produce rollback SQL for the given statement.
     *
     * @param sql the forward SQL statement
     * @return true if rollback can be generated
     */
    boolean canGenerateRollback(String sql);
}
