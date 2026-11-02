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
        TableExistsPrecondition p = new TableExistsPrecondition("users");
        assertEquals("users", p.getTableName());
        assertTrue(p.getDescription().contains("users"));
    }


    /**
     * Test ColumnExistsPrecondition.
     */
    @Test
    void testColumnExists() {
        ColumnExistsPrecondition p = new ColumnExistsPrecondition("users", "email");
        assertEquals("users", p.getTableName());
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
