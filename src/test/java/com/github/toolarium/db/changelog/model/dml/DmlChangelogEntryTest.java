/*
 * DmlChangelogEntryTest.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog.model.dml;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;


/**
 * Tests for DML changelog entry classes.
 *
 * @author patrick
 */
public class DmlChangelogEntryTest {
    private static final String TEST_TABLE = "users";
    private static final String TEST_WHERE = "id = 1";
    private static final String TEST_TAG = "v1.0";


    /**
     * Test InsertChangelogEntry.
     */
    @Test
    void testInsertChangelogEntry() {
        InsertChangelogEntry entry = new InsertChangelogEntry(TEST_TABLE);

        assertEquals(TEST_TABLE, entry.getTableName());
        assertEquals("insert: " + TEST_TABLE, entry.getDescription());
        assertFalse(entry.supportsAutoRollback());
        assertNull(entry.createAutoRollback());
        assertNotNull(entry.toString());
        assertTrue(entry.toString().contains(TEST_TABLE));
    }


    /**
     * Test InsertChangelogEntry equals and hashCode.
     */
    @Test
    void testInsertEqualsHashCode() {
        InsertChangelogEntry e1 = new InsertChangelogEntry(TEST_TABLE);
        InsertChangelogEntry e2 = new InsertChangelogEntry(TEST_TABLE);
        InsertChangelogEntry e3 = new InsertChangelogEntry("orders");

        assertEquals(e1, e2);
        assertEquals(e1.hashCode(), e2.hashCode());
        assertNotEquals(e1, e3);
        assertEquals(e1, e1);
        assertNotEquals(e1, null);
        assertNotEquals(e1, "not an entry");
    }


    /**
     * Test UpdateChangelogEntry.
     */
    @Test
    void testUpdateChangelogEntry() {
        UpdateChangelogEntry entry = new UpdateChangelogEntry(TEST_TABLE, TEST_WHERE);

        assertEquals(TEST_TABLE, entry.getTableName());
        assertEquals(TEST_WHERE, entry.getWhereClause());
        assertEquals("update: " + TEST_TABLE, entry.getDescription());
        assertFalse(entry.supportsAutoRollback());
        assertNull(entry.createAutoRollback());
        assertNotNull(entry.toString());
    }


    /**
     * Test UpdateChangelogEntry equals and hashCode.
     */
    @Test
    void testUpdateEqualsHashCode() {
        UpdateChangelogEntry e1 = new UpdateChangelogEntry(TEST_TABLE, TEST_WHERE);
        UpdateChangelogEntry e2 = new UpdateChangelogEntry(TEST_TABLE, TEST_WHERE);
        UpdateChangelogEntry e3 = new UpdateChangelogEntry(TEST_TABLE, "id = 2");

        assertEquals(e1, e2);
        assertEquals(e1.hashCode(), e2.hashCode());
        assertNotEquals(e1, e3);
        assertNotEquals(e1, new UpdateChangelogEntry("orders", TEST_WHERE));
    }


    /**
     * Test DeleteChangelogEntry.
     */
    @Test
    void testDeleteChangelogEntry() {
        DeleteChangelogEntry entry = new DeleteChangelogEntry(TEST_TABLE, TEST_WHERE);

        assertEquals(TEST_TABLE, entry.getTableName());
        assertEquals(TEST_WHERE, entry.getWhereClause());
        assertEquals("delete: " + TEST_TABLE, entry.getDescription());
        assertFalse(entry.supportsAutoRollback());
        assertNull(entry.createAutoRollback());
        assertNotNull(entry.toString());
    }


    /**
     * Test DeleteChangelogEntry equals and hashCode.
     */
    @Test
    void testDeleteEqualsHashCode() {
        DeleteChangelogEntry e1 = new DeleteChangelogEntry(TEST_TABLE, TEST_WHERE);
        DeleteChangelogEntry e2 = new DeleteChangelogEntry(TEST_TABLE, TEST_WHERE);
        DeleteChangelogEntry e3 = new DeleteChangelogEntry("orders", TEST_WHERE);

        assertEquals(e1, e2);
        assertEquals(e1.hashCode(), e2.hashCode());
        assertNotEquals(e1, e3);
    }


    /**
     * Test TagDatabaseChangelogEntry.
     */
    @Test
    void testTagDatabaseChangelogEntry() {
        TagDatabaseChangelogEntry entry = new TagDatabaseChangelogEntry(TEST_TAG);

        assertEquals(TEST_TAG, entry.getTag());
        assertEquals("tagDatabase: " + TEST_TAG, entry.getDescription());
        assertFalse(entry.supportsAutoRollback());
        assertNull(entry.createAutoRollback());
        assertNotNull(entry.toString());
        assertTrue(entry.toString().contains(TEST_TAG));
    }


    /**
     * Test TagDatabaseChangelogEntry equals and hashCode.
     */
    @Test
    void testTagDatabaseEqualsHashCode() {
        TagDatabaseChangelogEntry e1 = new TagDatabaseChangelogEntry(TEST_TAG);
        TagDatabaseChangelogEntry e2 = new TagDatabaseChangelogEntry(TEST_TAG);
        TagDatabaseChangelogEntry e3 = new TagDatabaseChangelogEntry("v2.0");

        assertEquals(e1, e2);
        assertEquals(e1.hashCode(), e2.hashCode());
        assertNotEquals(e1, e3);
    }


    /**
     * Test UpdateChangelogEntry with null where clause.
     */
    @Test
    void testUpdateWithNullWhere() {
        UpdateChangelogEntry entry = new UpdateChangelogEntry(TEST_TABLE, null);

        assertEquals(TEST_TABLE, entry.getTableName());
        assertNull(entry.getWhereClause());
    }


    /**
     * Test DeleteChangelogEntry with null where clause.
     */
    @Test
    void testDeleteWithNullWhere() {
        DeleteChangelogEntry entry = new DeleteChangelogEntry(TEST_TABLE, null);

        assertEquals(TEST_TABLE, entry.getTableName());
        assertNull(entry.getWhereClause());
    }
}
