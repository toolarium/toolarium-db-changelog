/*
 * ConstraintChangelogEntryTest.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog.dto.constraint;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.toolarium.db.changelog.dto.IChangelogEntry;
import org.junit.jupiter.api.Test;


/**
 * Tests for constraint changelog entry DTOs.
 *
 * @author patrick
 */
public class ConstraintChangelogEntryTest {
    private static final String TABLE_USERS = "users";
    private static final String COL_EMAIL = "email";


    /**
     * Test AddNotNullConstraint auto-rollback.
     */
    @Test
    void testAddNotNull() {
        AddNotNullConstraintChangelogEntry entry = new AddNotNullConstraintChangelogEntry(TABLE_USERS, COL_EMAIL);
        assertEquals(TABLE_USERS, entry.getTableName());
        assertEquals(COL_EMAIL, entry.getColumnName());
        assertTrue(entry.supportsAutoRollback());
        IChangelogEntry rollback = entry.createAutoRollback();
        assertNotNull(rollback);
        assertTrue(rollback instanceof DropNotNullConstraintChangelogEntry);
        assertNotNull(entry.toString());
    }


    /**
     * Test DropNotNullConstraint auto-rollback.
     */
    @Test
    void testDropNotNull() {
        DropNotNullConstraintChangelogEntry entry = new DropNotNullConstraintChangelogEntry(TABLE_USERS, COL_EMAIL);
        assertTrue(entry.supportsAutoRollback());
        IChangelogEntry rollback = entry.createAutoRollback();
        assertTrue(rollback instanceof AddNotNullConstraintChangelogEntry);
    }


    /**
     * Test AddPrimaryKey.
     */
    @Test
    void testAddPrimaryKey() {
        AddPrimaryKeyChangelogEntry entry = new AddPrimaryKeyChangelogEntry(TABLE_USERS, "pk_users", "id");
        assertEquals(TABLE_USERS, entry.getTableName());
        assertEquals("pk_users", entry.getConstraintName());
        assertTrue(entry.supportsAutoRollback());
        assertTrue(entry.createAutoRollback() instanceof DropPrimaryKeyChangelogEntry);
    }


    /**
     * Test DropPrimaryKey.
     */
    @Test
    void testDropPrimaryKey() {
        DropPrimaryKeyChangelogEntry entry = new DropPrimaryKeyChangelogEntry(TABLE_USERS, "pk_users");
        assertFalse(entry.supportsAutoRollback());
        assertNull(entry.createAutoRollback());
    }


    /**
     * Test AddForeignKey.
     */
    @Test
    void testAddForeignKey() {
        AddForeignKeyChangelogEntry entry = new AddForeignKeyChangelogEntry(
                "orders", "fk_orders_user", "user_id", TABLE_USERS, "id");
        assertEquals("orders", entry.getTableName());
        assertEquals("fk_orders_user", entry.getConstraintName());
        assertTrue(entry.supportsAutoRollback());
        assertTrue(entry.createAutoRollback() instanceof DropForeignKeyChangelogEntry);
    }


    /**
     * Test DropForeignKey.
     */
    @Test
    void testDropForeignKey() {
        DropForeignKeyChangelogEntry entry = new DropForeignKeyChangelogEntry("orders", "fk_orders_user");
        assertFalse(entry.supportsAutoRollback());
        assertNull(entry.createAutoRollback());
    }


    /**
     * Test AddUniqueConstraint.
     */
    @Test
    void testAddUniqueConstraint() {
        AddUniqueConstraintChangelogEntry entry = new AddUniqueConstraintChangelogEntry(
                TABLE_USERS, "uq_email", COL_EMAIL);
        assertTrue(entry.supportsAutoRollback());
        assertTrue(entry.createAutoRollback() instanceof DropUniqueConstraintChangelogEntry);
    }


    /**
     * Test DropUniqueConstraint.
     */
    @Test
    void testDropUniqueConstraint() {
        DropUniqueConstraintChangelogEntry entry = new DropUniqueConstraintChangelogEntry(TABLE_USERS, "uq_email");
        assertFalse(entry.supportsAutoRollback());
        assertNull(entry.createAutoRollback());
    }
}
