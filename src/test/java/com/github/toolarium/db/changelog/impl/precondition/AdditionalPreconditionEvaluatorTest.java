/*
 * AdditionalPreconditionEvaluatorTest.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog.impl.precondition;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.toolarium.db.changelog.impl.precondition.impl.ChangelogPreconditionEvaluatorImpl;
import com.github.toolarium.db.changelog.impl.resolver.impl.ChangelogParameterResolverImpl;
import com.github.toolarium.db.changelog.model.precondition.ChangeLogPropertyDefinedPrecondition;
import com.github.toolarium.db.changelog.model.precondition.RowCountPrecondition;
import com.github.toolarium.db.changelog.model.precondition.TableIsEmptyPrecondition;
import java.sql.Connection;
import java.sql.Statement;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


/**
 * Tests for additional precondition evaluation (tableIsEmpty, rowCount, changeLogPropertyDefined).
 *
 * @author patrick
 */
public class AdditionalPreconditionEvaluatorTest {
    private JdbcDataSource dataSource;
    private Connection connection;
    private ChangelogPreconditionEvaluatorImpl evaluator;


    /**
     * Set up H2 database with test data.
     *
     * @throws Exception on error
     */
    @BeforeEach
    void setUp() throws Exception {
        dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:precond_test_" + System.nanoTime() + ";DB_CLOSE_DELAY=-1");
        dataSource.setUser("sa");
        dataSource.setPassword("");

        connection = dataSource.getConnection();
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("CREATE TABLE empty_table (id INT PRIMARY KEY)");
            stmt.execute("CREATE TABLE data_table (id INT PRIMARY KEY, name VARCHAR(100))");
            stmt.execute("INSERT INTO data_table (id, name) VALUES (1, 'Alice')");
            stmt.execute("INSERT INTO data_table (id, name) VALUES (2, 'Bob')");
            stmt.execute("INSERT INTO data_table (id, name) VALUES (3, 'Charlie')");
        }

        evaluator = new ChangelogPreconditionEvaluatorImpl();
    }


    /**
     * Clean up.
     *
     * @throws Exception on error
     */
    @AfterEach
    void tearDown() throws Exception {
        if (connection != null) {
            connection.close();
        }
    }


    /**
     * Test tableIsEmpty on an empty table.
     */
    @Test
    void testTableIsEmptyOnEmptyTable() {
        assertTrue(evaluator.evaluate(new TableIsEmptyPrecondition("EMPTY_TABLE"), connection));
    }


    /**
     * Test tableIsEmpty on a table with data.
     */
    @Test
    void testTableIsEmptyOnNonEmptyTable() {
        assertFalse(evaluator.evaluate(new TableIsEmptyPrecondition("DATA_TABLE"), connection));
    }


    /**
     * Test tableIsEmpty on a non-existing table returns false.
     */
    @Test
    void testTableIsEmptyNonExistingTable() {
        assertFalse(evaluator.evaluate(new TableIsEmptyPrecondition("NON_EXISTING_TABLE"), connection));
    }


    /**
     * Test rowCount matches expected count.
     */
    @Test
    void testRowCountMatches() {
        assertTrue(evaluator.evaluate(new RowCountPrecondition("DATA_TABLE", 3), connection));
    }


    /**
     * Test rowCount does not match.
     */
    @Test
    void testRowCountDoesNotMatch() {
        assertFalse(evaluator.evaluate(new RowCountPrecondition("DATA_TABLE", 5), connection));
    }


    /**
     * Test rowCount on empty table.
     */
    @Test
    void testRowCountOnEmptyTable() {
        assertTrue(evaluator.evaluate(new RowCountPrecondition("EMPTY_TABLE", 0), connection));
        assertFalse(evaluator.evaluate(new RowCountPrecondition("EMPTY_TABLE", 1), connection));
    }


    /**
     * Test changeLogPropertyDefined when property exists.
     */
    @Test
    void testChangeLogPropertyDefinedExists() {
        ChangelogParameterResolverImpl resolver = new ChangelogParameterResolverImpl();
        resolver.setProperty("db.host", "localhost");
        evaluator.setParameterResolver(resolver);

        assertTrue(evaluator.evaluate(new ChangeLogPropertyDefinedPrecondition("db.host"), connection));
    }


    /**
     * Test changeLogPropertyDefined when property does not exist.
     */
    @Test
    void testChangeLogPropertyDefinedMissing() {
        ChangelogParameterResolverImpl resolver = new ChangelogParameterResolverImpl();
        evaluator.setParameterResolver(resolver);

        assertFalse(evaluator.evaluate(new ChangeLogPropertyDefinedPrecondition("nonexistent.prop"), connection));
    }


    /**
     * Test changeLogPropertyDefined when no resolver is set.
     */
    @Test
    void testChangeLogPropertyDefinedNoResolver() {
        assertFalse(evaluator.evaluate(new ChangeLogPropertyDefinedPrecondition("any.prop"), connection));
    }
}
