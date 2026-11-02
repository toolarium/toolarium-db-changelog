/*
 * AdditionalPreconditionTest.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog.model.precondition;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;


/**
 * Tests for additional precondition model classes.
 *
 * @author patrick
 */
public class AdditionalPreconditionTest {
    private static final String TABLE_USERS = "users";


    /**
     * Test TableIsEmptyPrecondition.
     */
    @Test
    void testTableIsEmptyPrecondition() {
        TableIsEmptyPrecondition p = new TableIsEmptyPrecondition(TABLE_USERS);
        assertEquals("users", p.getTableName());
        assertEquals("tableIsEmpty: users", p.getDescription());
        assertEquals("tableIsEmpty: users", p.toString());
    }


    /**
     * Test TableIsEmptyPrecondition equals/hashCode.
     */
    @Test
    void testTableIsEmptyEqualsHashCode() {
        TableIsEmptyPrecondition p1 = new TableIsEmptyPrecondition(TABLE_USERS);
        TableIsEmptyPrecondition p2 = new TableIsEmptyPrecondition(TABLE_USERS);
        TableIsEmptyPrecondition p3 = new TableIsEmptyPrecondition("orders");

        assertEquals(p1, p2);
        assertEquals(p1.hashCode(), p2.hashCode());
        assertNotEquals(p1, p3);
        assertFalse(p1.equals(null));
        assertFalse(p1.equals("string"));
        assertTrue(p1.equals(p1));
    }


    /**
     * Test RowCountPrecondition.
     */
    @Test
    void testRowCountPrecondition() {
        RowCountPrecondition p = new RowCountPrecondition(TABLE_USERS, 42);
        assertEquals("users", p.getTableName());
        assertEquals(42, p.getExpectedCount());
        assertEquals("rowCount: users = 42", p.getDescription());
        assertEquals("rowCount: users = 42", p.toString());
    }


    /**
     * Test RowCountPrecondition equals/hashCode.
     */
    @Test
    void testRowCountEqualsHashCode() {
        RowCountPrecondition p1 = new RowCountPrecondition(TABLE_USERS, 10);
        RowCountPrecondition p2 = new RowCountPrecondition(TABLE_USERS, 10);
        RowCountPrecondition p3 = new RowCountPrecondition(TABLE_USERS, 20);

        assertEquals(p1, p2);
        assertEquals(p1.hashCode(), p2.hashCode());
        assertNotEquals(p1, p3);
        assertNotEquals(p1, new RowCountPrecondition("orders", 10));
        assertFalse(p1.equals(null));
        assertTrue(p1.equals(p1));
    }


    /**
     * Test ChangeLogPropertyDefinedPrecondition.
     */
    @Test
    void testChangeLogPropertyDefinedPrecondition() {
        ChangeLogPropertyDefinedPrecondition p = new ChangeLogPropertyDefinedPrecondition("db.host");
        assertEquals("db.host", p.getPropertyName());
        assertEquals("changeLogPropertyDefined: db.host", p.getDescription());
        assertEquals("changeLogPropertyDefined: db.host", p.toString());
    }


    /**
     * Test ChangeLogPropertyDefinedPrecondition equals/hashCode.
     */
    @Test
    void testChangeLogPropertyDefinedEqualsHashCode() {
        ChangeLogPropertyDefinedPrecondition p1 = new ChangeLogPropertyDefinedPrecondition("db.host");
        ChangeLogPropertyDefinedPrecondition p2 = new ChangeLogPropertyDefinedPrecondition("db.host");
        ChangeLogPropertyDefinedPrecondition p3 = new ChangeLogPropertyDefinedPrecondition("db.port");

        assertEquals(p1, p2);
        assertEquals(p1.hashCode(), p2.hashCode());
        assertNotEquals(p1, p3);
        assertFalse(p1.equals(null));
        assertTrue(p1.equals(p1));
    }
}
