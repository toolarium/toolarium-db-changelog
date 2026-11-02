/*
 * IChangelogSqlGenerator.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog.impl.sql;

import com.github.toolarium.db.changelog.model.IChangelogEntry;
import java.sql.Connection;
import java.util.List;


/**
 * Generates SQL statements from changelog entries for a specific database.
 * Each implementation handles a specific database dialect.
 *
 * @author patrick
 */
public interface IChangelogSqlGenerator {

    /**
     * Generate the SQL statements for a changelog entry.
     *
     * @param entry the changelog entry
     * @return the list of SQL statements to execute
     */
    List<String> generateSql(IChangelogEntry entry);

    /**
     * Check if this generator supports the given entry type and database connection.
     *
     * @param entry the changelog entry
     * @param connection the database connection
     * @return true if this generator can handle the entry for the connected database
     */
    boolean supports(IChangelogEntry entry, Connection connection);
}
