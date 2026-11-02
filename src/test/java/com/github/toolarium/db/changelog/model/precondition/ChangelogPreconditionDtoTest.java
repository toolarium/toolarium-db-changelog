/*
 * ChangelogPreconditionDtoTest.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog.model.precondition;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.Test;


/**
 * Tests for precondition DTOs.
 *
 * @author patrick
 */
public class ChangelogPreconditionDtoTest {
    private static final String DBMS_H2 = "h2";
    private static final String TEST_TABLE = "users";


    /**
     * Test DbmsPrecondition.
     */
    @Test
    void testDbms() {
        DbmsPrecondition p = new DbmsPrecondition(DBMS_H2);
        assertEquals(DBMS_H2, p.getDbms());
        assertTrue(p.getDescription().contains(DBMS_H2));
        assertNotNull(p.toString());
    }


    /**
     * Test TableExistsPrecondition.
     */
    @Test
    void testTableExists() {
        TableExistsPrecondition p = new TableExistsPrecondition(TEST_TABLE);
        assertEquals(TEST_TABLE, p.getTableName());
        assertTrue(p.getDescription().contains(TEST_TABLE));
    }


    /**
     * Test ColumnExistsPrecondition.
     */
    @Test
    void testColumnExists() {
        ColumnExistsPrecondition p = new ColumnExistsPrecondition(TEST_TABLE, "email");
        assertEquals(TEST_TABLE, p.getTableName());
        assertEquals("email", p.getColumnName());
        assertTrue(p.getDescription().contains("email"));
    }


    /**
     * Test SqlCheckPrecondition.
     */
    @Test
    void testSqlCheck() {
        SqlCheckPrecondition p = new SqlCheckPrecondition("SELECT 1", "1");
        assertEquals("SELECT 1", p.getSql());
        assertEquals("1", p.getExpectedResult());
    }


    /**
     * Test ChangeSetExecutedPrecondition.
     */
    @Test
    void testChangeSetExecuted() {
        ChangeSetExecutedPrecondition p = new ChangeSetExecutedPrecondition("cs-1", "dev");
        assertEquals("cs-1", p.getChangeSetId());
        assertEquals("dev", p.getAuthor());
        assertTrue(p.getDescription().contains("cs-1"));
    }


    /**
     * Test IndexExistsPrecondition.
     */
    @Test
    void testIndexExists() {
        IndexExistsPrecondition p = new IndexExistsPrecondition("idx_email");
        assertEquals("idx_email", p.getIndexName());
        assertTrue(p.getDescription().contains("idx_email"));
        assertNotNull(p.toString());
        assertEquals(p, new IndexExistsPrecondition("idx_email"));
        assertEquals(p.hashCode(), new IndexExistsPrecondition("idx_email").hashCode());
    }


    /**
     * Test ViewExistsPrecondition.
     */
    @Test
    void testViewExists() {
        ViewExistsPrecondition p = new ViewExistsPrecondition("v_report");
        assertEquals("v_report", p.getViewName());
        assertTrue(p.getDescription().contains("v_report"));
        assertEquals(p, new ViewExistsPrecondition("v_report"));
    }


    /**
     * Test SequenceExistsPrecondition.
     */
    @Test
    void testSequenceExists() {
        SequenceExistsPrecondition p = new SequenceExistsPrecondition("user_seq");
        assertEquals("user_seq", p.getSequenceName());
        assertTrue(p.getDescription().contains("user_seq"));
        assertEquals(p, new SequenceExistsPrecondition("user_seq"));
    }


    /**
     * Test PrimaryKeyExistsPrecondition.
     */
    @Test
    void testPrimaryKeyExists() {
        PrimaryKeyExistsPrecondition p = new PrimaryKeyExistsPrecondition(TEST_TABLE);
        assertEquals(TEST_TABLE, p.getTableName());
        assertTrue(p.getDescription().contains(TEST_TABLE));
        assertEquals(p, new PrimaryKeyExistsPrecondition(TEST_TABLE));
    }


    /**
     * Test ForeignKeyExistsPrecondition.
     */
    @Test
    void testForeignKeyExists() {
        ForeignKeyExistsPrecondition p = new ForeignKeyExistsPrecondition("fk_user");
        assertEquals("fk_user", p.getConstraintName());
        assertTrue(p.getDescription().contains("fk_user"));
        assertEquals(p, new ForeignKeyExistsPrecondition("fk_user"));
    }


    /**
     * Test RunningAsPrecondition.
     */
    @Test
    void testRunningAs() {
        RunningAsPrecondition p = new RunningAsPrecondition("admin");
        assertEquals("admin", p.getUsername());
        assertTrue(p.getDescription().contains("admin"));
        assertNotNull(p.toString());
        assertEquals(p, new RunningAsPrecondition("admin"));
        assertEquals(p.hashCode(), new RunningAsPrecondition("admin").hashCode());
    }


    /**
     * Test logical combinators.
     */
    @Test
    void testLogicalCombinators() {
        AndPrecondition and = new AndPrecondition(Arrays.asList(
                new DbmsPrecondition(DBMS_H2), new TableExistsPrecondition("t")));
        assertEquals(2, and.getPreconditions().size());
        assertTrue(and.getDescription().contains("and"));

        OrPrecondition or = new OrPrecondition(Collections.singletonList(new DbmsPrecondition(DBMS_H2)));
        assertEquals(1, or.getPreconditions().size());

        NotPrecondition not = new NotPrecondition(new DbmsPrecondition("oracle"));
        assertNotNull(not.getPrecondition());
        assertTrue(not.getDescription().contains("not"));
    }


    /**
     * Test ChangelogPreconditionFailureAction values.
     */
    @Test
    void testFailureAction() {
        assertEquals(4, ChangelogPreconditionFailureAction.values().length);
        assertNotNull(ChangelogPreconditionFailureAction.valueOf("HALT"));
        assertNotNull(ChangelogPreconditionFailureAction.valueOf("WARN"));
        assertNotNull(ChangelogPreconditionFailureAction.valueOf("CONTINUE"));
        assertNotNull(ChangelogPreconditionFailureAction.valueOf("MARK_RAN"));
    }


    /**
     * Test ChangelogPreconditionContainer.
     */
    @Test
    void testContainer() {
        ChangelogPreconditionContainer container = new ChangelogPreconditionContainer(
                ChangelogPreconditionFailureAction.HALT,
                ChangelogPreconditionFailureAction.HALT,
                Arrays.asList(new DbmsPrecondition(DBMS_H2)));

        assertEquals(ChangelogPreconditionFailureAction.HALT, container.getOnFail());
        assertEquals(ChangelogPreconditionFailureAction.HALT, container.getOnError());
        assertEquals(1, container.getChecks().size());
        assertNotNull(container.toString());
    }
}
