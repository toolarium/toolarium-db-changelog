/*
 * TableChangelogEntryTest.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog.dto.table;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.toolarium.db.changelog.dto.IChangelogEntry;
import com.github.toolarium.db.changelog.dto.column.ChangelogColumnDefinition;
import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.Test;


/**
 * Tests for table changelog entry DTOs.
 *
 * @author patrick
 */
public class TableChangelogEntryTest {
    private static final String TABLE_USERS = "users";


    /**
     * Test CreateTableChangelogEntry.
     */
    @Test
    void testCreateTable() {
        ChangelogColumnDefinition col = new ChangelogColumnDefinition("id", "INT");
        col.setPrimaryKey(true).setNullable(false);

        CreateTableChangelogEntry entry = new CreateTableChangelogEntry(TABLE_USERS,
                Arrays.asList(col));

        assertEquals(TABLE_USERS, entry.getTableName());
        assertEquals(1, entry.getColumns().size());
        assertTrue(entry.supportsAutoRollback());
        assertTrue(entry.getDescription().contains(TABLE_USERS));
        assertNotNull(entry.toString());

        IChangelogEntry rollback = entry.createAutoRollback();
        assertNotNull(rollback);
        assertTrue(rollback instanceof DropTableChangelogEntry);
    }


    /**
     * Test CreateTableChangelogEntry equals and hashCode.
     */
    @Test
    void testCreateTableEquality() {
        CreateTableChangelogEntry e1 = new CreateTableChangelogEntry("t1", Collections.emptyList());
        CreateTableChangelogEntry e2 = new CreateTableChangelogEntry("t1", Collections.emptyList());
        CreateTableChangelogEntry e3 = new CreateTableChangelogEntry("t2", Collections.emptyList());

        assertEquals(e1, e2);
        assertEquals(e1.hashCode(), e2.hashCode());
        assertNotEquals(e1, e3);
    }


    /**
     * Test DropTableChangelogEntry.
     */
    @Test
    void testDropTable() {
        DropTableChangelogEntry entry = new DropTableChangelogEntry(TABLE_USERS);

        assertEquals(TABLE_USERS, entry.getTableName());
        assertFalse(entry.supportsAutoRollback());
        assertNull(entry.createAutoRollback());
        assertTrue(entry.getDescription().contains(TABLE_USERS));
        assertNotNull(entry.toString());
    }


    /**
     * Test DropTableChangelogEntry equals and hashCode.
     */
    @Test
    void testDropTableEquality() {
        DropTableChangelogEntry e1 = new DropTableChangelogEntry("t1");
        DropTableChangelogEntry e2 = new DropTableChangelogEntry("t1");
        DropTableChangelogEntry e3 = new DropTableChangelogEntry("t2");

        assertEquals(e1, e2);
        assertEquals(e1.hashCode(), e2.hashCode());
        assertNotEquals(e1, e3);
    }
}
