/*
 * ChangelogPreconditionEvaluatorTest.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog.impl.precondition;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.toolarium.db.changelog.model.precondition.AndPrecondition;
import com.github.toolarium.db.changelog.model.precondition.ColumnExistsPrecondition;
import com.github.toolarium.db.changelog.model.precondition.DbmsPrecondition;
import com.github.toolarium.db.changelog.model.precondition.NotPrecondition;
import com.github.toolarium.db.changelog.model.precondition.OrPrecondition;
import com.github.toolarium.db.changelog.model.precondition.SqlCheckPrecondition;
import com.github.toolarium.db.changelog.model.precondition.TableExistsPrecondition;
import java.sql.Connection;
import java.sql.Statement;
import java.util.Arrays;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


/**
 * Tests for precondition evaluator.
 *
 * @author patrick
 */
public class ChangelogPreconditionEvaluatorTest {
    private static final String TABLE_USERS = "PRECOND_USERS";
    private JdbcDataSource dataSource;
    private IChangelogPreconditionEvaluator evaluator;


    /**
     * Set up.
     *
     * @throws Exception on error
     */
    @BeforeEach
    void setUp() throws Exception {
        dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:precond_" + System.nanoTime() + ";DB_CLOSE_DELAY=-1");
        dataSource.setUser("sa");
        dataSource.setPassword("");
        evaluator = ChangelogPreconditionEvaluatorFactory.getInstance().getPreconditionEvaluator();

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE " + TABLE_USERS + " (id INT PRIMARY KEY, name VARCHAR(100))");
        }
    }


    /**
     * Test factory singleton.
     */
    @Test
    void testFactory() {
        assertSame(ChangelogPreconditionEvaluatorFactory.getInstance(),
                ChangelogPreconditionEvaluatorFactory.getInstance());
        assertNotNull(evaluator);
    }


    /**
     * Test dbms precondition with H2.
     *
     * @throws Exception on error
     */
    @Test
    void testDbmsPrecondition() throws Exception {
        try (Connection conn = dataSource.getConnection()) {
            assertTrue(evaluator.evaluate(new DbmsPrecondition("h2"), conn));
            assertTrue(evaluator.evaluate(new DbmsPrecondition("H2"), conn));
            assertFalse(evaluator.evaluate(new DbmsPrecondition("oracle"), conn));
            assertTrue(evaluator.evaluate(new DbmsPrecondition("oracle,h2"), conn));
        }
    }


    /**
     * Test tableExists precondition.
     *
     * @throws Exception on error
     */
    @Test
    void testTableExists() throws Exception {
        try (Connection conn = dataSource.getConnection()) {
            assertTrue(evaluator.evaluate(new TableExistsPrecondition(TABLE_USERS), conn));
            assertFalse(evaluator.evaluate(new TableExistsPrecondition("NONEXISTENT"), conn));
        }
    }


    /**
     * Test columnExists precondition.
     *
     * @throws Exception on error
     */
    @Test
    void testColumnExists() throws Exception {
        try (Connection conn = dataSource.getConnection()) {
            assertTrue(evaluator.evaluate(new ColumnExistsPrecondition(TABLE_USERS, "ID"), conn));
            assertFalse(evaluator.evaluate(new ColumnExistsPrecondition(TABLE_USERS, "NONEXISTENT"), conn));
        }
    }


    /**
     * Test sqlCheck precondition.
     *
     * @throws Exception on error
     */
    @Test
    void testSqlCheck() throws Exception {
        try (Connection conn = dataSource.getConnection()) {
            assertTrue(evaluator.evaluate(new SqlCheckPrecondition("SELECT COUNT(*) FROM " + TABLE_USERS, "0"), conn));
            assertFalse(evaluator.evaluate(new SqlCheckPrecondition("SELECT COUNT(*) FROM " + TABLE_USERS, "5"), conn));
        }
    }


    /**
     * Test AND combinator.
     *
     * @throws Exception on error
     */
    @Test
    void testAndPrecondition() throws Exception {
        try (Connection conn = dataSource.getConnection()) {
            assertTrue(evaluator.evaluate(new AndPrecondition(Arrays.asList(
                    new TableExistsPrecondition(TABLE_USERS),
                    new DbmsPrecondition("h2"))), conn));

            assertFalse(evaluator.evaluate(new AndPrecondition(Arrays.asList(
                    new TableExistsPrecondition(TABLE_USERS),
                    new DbmsPrecondition("oracle"))), conn));
        }
    }


    /**
     * Test OR combinator.
     *
     * @throws Exception on error
     */
    @Test
    void testOrPrecondition() throws Exception {
        try (Connection conn = dataSource.getConnection()) {
            assertTrue(evaluator.evaluate(new OrPrecondition(Arrays.asList(
                    new TableExistsPrecondition("NONEXISTENT"),
                    new DbmsPrecondition("h2"))), conn));

            assertFalse(evaluator.evaluate(new OrPrecondition(Arrays.asList(
                    new TableExistsPrecondition("NONEXISTENT"),
                    new DbmsPrecondition("oracle"))), conn));
        }
    }


    /**
     * Test NOT combinator.
     *
     * @throws Exception on error
     */
    @Test
    void testNotPrecondition() throws Exception {
        try (Connection conn = dataSource.getConnection()) {
            assertTrue(evaluator.evaluate(new NotPrecondition(
                    new TableExistsPrecondition("NONEXISTENT")), conn));

            assertFalse(evaluator.evaluate(new NotPrecondition(
                    new TableExistsPrecondition(TABLE_USERS)), conn));
        }
    }
}
