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

import com.github.toolarium.db.changelog.ICustomChangelogEntry;
import com.github.toolarium.db.changelog.exception.ChangelogException;
import java.sql.Connection;
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


    /**
     * Test SetTableRemarksChangelogEntry.
     */
    @Test
    void testSetTableRemarks() {
        SetTableRemarksChangelogEntry entry = new SetTableRemarksChangelogEntry(TEST_TABLE, "Main user table");

        assertEquals(TEST_TABLE, entry.getTableName());
        assertEquals("Main user table", entry.getRemarks());
        assertTrue(entry.getDescription().contains(TEST_TABLE));
        assertFalse(entry.supportsAutoRollback());
        assertNull(entry.createAutoRollback());
    }


    /**
     * Test SetTableRemarksChangelogEntry equals and hashCode.
     */
    @Test
    void testSetTableRemarksEqualsHashCode() {
        SetTableRemarksChangelogEntry e1 = new SetTableRemarksChangelogEntry(TEST_TABLE, "comment");
        SetTableRemarksChangelogEntry e2 = new SetTableRemarksChangelogEntry(TEST_TABLE, "comment");
        SetTableRemarksChangelogEntry e3 = new SetTableRemarksChangelogEntry(TEST_TABLE, "other");

        assertEquals(e1, e2);
        assertEquals(e1.hashCode(), e2.hashCode());
        assertNotEquals(e1, e3);
    }


    /**
     * Test SetColumnRemarksChangelogEntry.
     */
    @Test
    void testSetColumnRemarks() {
        SetColumnRemarksChangelogEntry entry = new SetColumnRemarksChangelogEntry(TEST_TABLE, "email", "User email address");

        assertEquals(TEST_TABLE, entry.getTableName());
        assertEquals("email", entry.getColumnName());
        assertEquals("User email address", entry.getRemarks());
        assertTrue(entry.getDescription().contains("email"));
        assertFalse(entry.supportsAutoRollback());
    }


    /**
     * Test SetColumnRemarksChangelogEntry equals and hashCode.
     */
    @Test
    void testSetColumnRemarksEqualsHashCode() {
        SetColumnRemarksChangelogEntry e1 = new SetColumnRemarksChangelogEntry(TEST_TABLE, "col", "remark");
        SetColumnRemarksChangelogEntry e2 = new SetColumnRemarksChangelogEntry(TEST_TABLE, "col", "remark");
        SetColumnRemarksChangelogEntry e3 = new SetColumnRemarksChangelogEntry(TEST_TABLE, "col", "other");

        assertEquals(e1, e2);
        assertEquals(e1.hashCode(), e2.hashCode());
        assertNotEquals(e1, e3);
    }


    /**
     * Test LoadDataChangelogEntry.
     */
    @Test
    void testLoadData() {
        LoadDataChangelogEntry entry = new LoadDataChangelogEntry(TEST_TABLE, "data/users.csv", null, null);

        assertEquals(TEST_TABLE, entry.getTableName());
        assertEquals("data/users.csv", entry.getFilePath());
        assertEquals(",", entry.getSeparator());
        assertEquals("UTF-8", entry.getEncoding());
        assertTrue(entry.getDescription().contains(TEST_TABLE));
        assertTrue(entry.getDescription().contains("users.csv"));
        assertFalse(entry.supportsAutoRollback());
    }


    /**
     * Test LoadDataChangelogEntry with custom separator and encoding.
     */
    @Test
    void testLoadDataCustom() {
        LoadDataChangelogEntry entry = new LoadDataChangelogEntry(TEST_TABLE, "data/users.tsv", "\t", "ISO-8859-1");

        assertEquals("\t", entry.getSeparator());
        assertEquals("ISO-8859-1", entry.getEncoding());
    }


    /**
     * Test LoadDataChangelogEntry equals and hashCode.
     */
    @Test
    void testLoadDataEqualsHashCode() {
        LoadDataChangelogEntry e1 = new LoadDataChangelogEntry(TEST_TABLE, "data.csv", null, null);
        LoadDataChangelogEntry e2 = new LoadDataChangelogEntry(TEST_TABLE, "data.csv", null, null);
        LoadDataChangelogEntry e3 = new LoadDataChangelogEntry(TEST_TABLE, "other.csv", null, null);

        assertEquals(e1, e2);
        assertEquals(e1.hashCode(), e2.hashCode());
        assertNotEquals(e1, e3);
    }


    /**
     * Test LoadUpdateDataChangelogEntry.
     */
    @Test
    void testLoadUpdateData() {
        LoadUpdateDataChangelogEntry entry = new LoadUpdateDataChangelogEntry(
                TEST_TABLE, "data/users.csv", "id", null, null);

        assertEquals(TEST_TABLE, entry.getTableName());
        assertEquals("data/users.csv", entry.getFilePath());
        assertEquals("id", entry.getPrimaryKeyColumn());
        assertEquals(",", entry.getSeparator());
        assertEquals("UTF-8", entry.getEncoding());
        assertTrue(entry.getDescription().contains(TEST_TABLE));
        assertFalse(entry.supportsAutoRollback());
    }


    /**
     * Test LoadUpdateDataChangelogEntry equals and hashCode.
     */
    @Test
    void testLoadUpdateDataEqualsHashCode() {
        LoadUpdateDataChangelogEntry e1 = new LoadUpdateDataChangelogEntry(TEST_TABLE, "data.csv", "id", null, null);
        LoadUpdateDataChangelogEntry e2 = new LoadUpdateDataChangelogEntry(TEST_TABLE, "data.csv", "id", null, null);
        LoadUpdateDataChangelogEntry e3 = new LoadUpdateDataChangelogEntry(TEST_TABLE, "data.csv", "code", null, null);

        assertEquals(e1, e2);
        assertEquals(e1.hashCode(), e2.hashCode());
        assertNotEquals(e1, e3);
    }


    /**
     * Test CustomChangelogEntry.
     */
    @Test
    void testCustomChangelogEntry() {
        CustomChangelogEntry entry = new CustomChangelogEntry("com.example.MyMigration", null);

        assertEquals("com.example.MyMigration", entry.getClassName());
        assertNull(entry.getCustomEntry());
        assertTrue(entry.getDescription().contains("com.example.MyMigration"));
        assertFalse(entry.supportsAutoRollback());
        assertNull(entry.createAutoRollback());
    }


    /**
     * Test CustomChangelogEntry equals and hashCode.
     */
    @Test
    void testCustomChangelogEntryEqualsHashCode() {
        CustomChangelogEntry e1 = new CustomChangelogEntry("com.example.A", null);
        CustomChangelogEntry e2 = new CustomChangelogEntry("com.example.A", null);
        CustomChangelogEntry e3 = new CustomChangelogEntry("com.example.B", null);

        assertEquals(e1, e2);
        assertEquals(e1.hashCode(), e2.hashCode());
        assertNotEquals(e1, e3);
    }


    /**
     * Test CustomChangelogEntry with a real ICustomChangelogEntry implementation.
     */
    @Test
    void testCustomChangelogEntryWithImplementation() {
        ICustomChangelogEntry custom = new TestCustomMigration();
        CustomChangelogEntry entry = new CustomChangelogEntry("TestCustomMigration", custom);

        assertNotNull(entry.getCustomEntry());
        assertTrue(entry.getDescription().contains("Test migration"));
        assertTrue(entry.supportsAutoRollback());
    }


    /**
     * Test ICustomChangelogEntry implementation for testing.
     */
    private static final class TestCustomMigration implements ICustomChangelogEntry {

        /**
         * @see com.github.toolarium.db.changelog.ICustomChangelogEntry#execute(Connection)
         */
        @Override
        public void execute(Connection connection) throws ChangelogException {
            // no-op for test
        }


        /**
         * @see com.github.toolarium.db.changelog.ICustomChangelogEntry#rollback(Connection)
         */
        @Override
        public void rollback(Connection connection) throws ChangelogException {
            // no-op for test
        }


        /**
         * @see com.github.toolarium.db.changelog.ICustomChangelogEntry#getDescription()
         */
        @Override
        public String getDescription() {
            return "Test migration";
        }


        /**
         * @see com.github.toolarium.db.changelog.ICustomChangelogEntry#supportsRollback()
         */
        @Override
        public boolean supportsRollback() {
            return true;
        }
    }
}
