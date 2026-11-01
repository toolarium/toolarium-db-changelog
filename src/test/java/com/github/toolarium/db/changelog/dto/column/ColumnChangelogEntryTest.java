/*
 * ColumnChangelogEntryTest.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog.dto.column;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.toolarium.db.changelog.dto.IChangelogEntry;
import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.Test;


/**
 * Tests for column changelog entry DTOs and ChangelogColumnDefinition.
 *
 * @author patrick
 */
public class ColumnChangelogEntryTest {
    private static final String TABLE_USERS = "users";


    /**
     * Test ChangelogColumnDefinition implements IChangelogColumnDefinition.
     */
    @Test
    void testColumnDefinition() {
        ChangelogColumnDefinition col = new ChangelogColumnDefinition("email", "varchar(255)");
        col.setNullable(false).setUnique(true).setDefaultValue("test@example.com");

        assertTrue(col instanceof IChangelogColumnDefinition);
        assertEquals("email", col.getName());
        assertEquals("varchar(255)", col.getType());
        assertFalse(col.isNullable());
        assertTrue(col.isUnique());
        assertFalse(col.isPrimaryKey());
        assertFalse(col.isAutoIncrement());
        assertEquals("test@example.com", col.getDefaultValue());
        assertNull(col.getDefaultValueComputed());
        assertNotNull(col.toString());
    }


    /**
     * Test ChangelogColumnDefinition with computed default.
     */
    @Test
    void testColumnDefinitionComputed() {
        ChangelogColumnDefinition col = new ChangelogColumnDefinition("created_at", "timestamp");
        col.setDefaultValueComputed("CURRENT_TIMESTAMP");

        assertNull(col.getDefaultValue());
        assertEquals("CURRENT_TIMESTAMP", col.getDefaultValueComputed());
    }


    /**
     * Test ChangelogColumnDefinition equals and hashCode.
     */
    @Test
    void testColumnDefinitionEquality() {
        ChangelogColumnDefinition c1 = new ChangelogColumnDefinition("id", "INT");
        ChangelogColumnDefinition c2 = new ChangelogColumnDefinition("id", "INT");
        ChangelogColumnDefinition c3 = new ChangelogColumnDefinition("name", "VARCHAR");

        assertEquals(c1, c2);
        assertEquals(c1.hashCode(), c2.hashCode());
        assertNotEquals(c1, c3);
    }


    /**
     * Test AddColumnChangelogEntry.
     */
    @Test
    void testAddColumn() {
        ChangelogColumnDefinition col = new ChangelogColumnDefinition("phone", "varchar(20)");
        AddColumnChangelogEntry entry = new AddColumnChangelogEntry(TABLE_USERS, Arrays.asList(col));

        assertEquals(TABLE_USERS, entry.getTableName());
        assertEquals(1, entry.getColumns().size());
        assertTrue(entry.supportsAutoRollback());
        assertTrue(entry.getDescription().contains(TABLE_USERS));

        IChangelogEntry rollback = entry.createAutoRollback();
        assertNotNull(rollback);
        assertTrue(rollback instanceof DropColumnChangelogEntry);
    }


    /**
     * Test AddColumnChangelogEntry equals and hashCode.
     */
    @Test
    void testAddColumnEquality() {
        AddColumnChangelogEntry e1 = new AddColumnChangelogEntry("t1", Collections.emptyList());
        AddColumnChangelogEntry e2 = new AddColumnChangelogEntry("t1", Collections.emptyList());
        AddColumnChangelogEntry e3 = new AddColumnChangelogEntry("t2", Collections.emptyList());

        assertEquals(e1, e2);
        assertEquals(e1.hashCode(), e2.hashCode());
        assertNotEquals(e1, e3);
    }


    /**
     * Test DropColumnChangelogEntry.
     */
    @Test
    void testDropColumn() {
        DropColumnChangelogEntry entry = new DropColumnChangelogEntry(TABLE_USERS,
                Arrays.asList("phone", "fax"));

        assertEquals(TABLE_USERS, entry.getTableName());
        assertEquals(2, entry.getColumnNames().size());
        assertFalse(entry.supportsAutoRollback());
        assertNull(entry.createAutoRollback());
        assertTrue(entry.getDescription().contains(TABLE_USERS));
        assertNotNull(entry.toString());
    }


    /**
     * Test DropColumnChangelogEntry equals and hashCode.
     */
    @Test
    void testDropColumnEquality() {
        DropColumnChangelogEntry e1 = new DropColumnChangelogEntry("t1", Arrays.asList("c1"));
        DropColumnChangelogEntry e2 = new DropColumnChangelogEntry("t1", Arrays.asList("c1"));
        DropColumnChangelogEntry e3 = new DropColumnChangelogEntry("t1", Arrays.asList("c2"));

        assertEquals(e1, e2);
        assertEquals(e1.hashCode(), e2.hashCode());
        assertNotEquals(e1, e3);
    }
}
