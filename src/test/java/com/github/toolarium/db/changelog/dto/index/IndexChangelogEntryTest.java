/*
 * IndexChangelogEntryTest.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog.dto.index;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.toolarium.db.changelog.dto.IChangelogEntry;
import java.util.Arrays;
import org.junit.jupiter.api.Test;


/**
 * Tests for index changelog entry DTOs.
 *
 * @author patrick
 */
public class IndexChangelogEntryTest {
    private static final String TABLE_T1 = "t1";
    private static final String IDX_EMAIL = "idx_email";


    /**
     * Test CreateIndexChangelogEntry.
     */
    @Test
    void testCreateIndex() {
        CreateIndexChangelogEntry entry = new CreateIndexChangelogEntry("users",
                IDX_EMAIL, Arrays.asList("email"), true);

        assertEquals("users", entry.getTableName());
        assertEquals(IDX_EMAIL, entry.getIndexName());
        assertEquals(1, entry.getColumnNames().size());
        assertTrue(entry.isUnique());
        assertTrue(entry.supportsAutoRollback());
        assertTrue(entry.getDescription().contains(IDX_EMAIL));

        IChangelogEntry rollback = entry.createAutoRollback();
        assertNotNull(rollback);
        assertTrue(rollback instanceof DropIndexChangelogEntry);
    }


    /**
     * Test CreateIndexChangelogEntry equals and hashCode.
     */
    @Test
    void testCreateIndexEquality() {
        CreateIndexChangelogEntry e1 = new CreateIndexChangelogEntry(TABLE_T1, "idx1", Arrays.asList("c1"), false);
        CreateIndexChangelogEntry e2 = new CreateIndexChangelogEntry(TABLE_T1, "idx1", Arrays.asList("c1"), false);
        CreateIndexChangelogEntry e3 = new CreateIndexChangelogEntry(TABLE_T1, "idx2", Arrays.asList("c1"), false);

        assertEquals(e1, e2);
        assertEquals(e1.hashCode(), e2.hashCode());
        assertNotEquals(e1, e3);
    }


    /**
     * Test DropIndexChangelogEntry.
     */
    @Test
    void testDropIndex() {
        DropIndexChangelogEntry entry = new DropIndexChangelogEntry("users", IDX_EMAIL);

        assertEquals("users", entry.getTableName());
        assertEquals(IDX_EMAIL, entry.getIndexName());
        assertFalse(entry.supportsAutoRollback());
        assertNull(entry.createAutoRollback());
        assertTrue(entry.getDescription().contains(IDX_EMAIL));
        assertNotNull(entry.toString());
    }


    /**
     * Test DropIndexChangelogEntry equals and hashCode.
     */
    @Test
    void testDropIndexEquality() {
        DropIndexChangelogEntry e1 = new DropIndexChangelogEntry(TABLE_T1, "idx1");
        DropIndexChangelogEntry e2 = new DropIndexChangelogEntry(TABLE_T1, "idx1");
        DropIndexChangelogEntry e3 = new DropIndexChangelogEntry(TABLE_T1, "idx2");

        assertEquals(e1, e2);
        assertEquals(e1.hashCode(), e2.hashCode());
        assertNotEquals(e1, e3);
    }
}
