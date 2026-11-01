/*
 * IChangelogExecutor.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog.impl.executor;


import com.github.toolarium.db.changelog.dto.IChangelog;
import com.github.toolarium.db.changelog.exception.ChangelogException;
import java.sql.Connection;

/**
 * Executes a changelog against a database connection.
 *
 * @author patrick
 */
public interface IChangelogExecutor {

    /**
     * Execute all pending changesets from the changelog.
     *
     * @param connection the database connection
     * @param changelog the changelog to execute
     * @throws ChangelogException if execution fails
     */
    void update(Connection connection, IChangelog changelog) throws ChangelogException;


    /**
     * Execute up to count pending changesets from the changelog.
     *
     * @param connection the database connection
     * @param changelog the changelog to execute
     * @param count the maximum number of changesets to execute
     * @throws ChangelogException if execution fails
     */
    void update(Connection connection, IChangelog changelog, int count) throws ChangelogException;
}
