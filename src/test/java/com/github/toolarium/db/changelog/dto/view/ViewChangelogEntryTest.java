/*
 * ViewChangelogEntryTest.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog.dto.view;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.toolarium.db.changelog.dto.IChangelogEntry;
import org.junit.jupiter.api.Test;


/**
 * Tests for view changelog entry DTOs.
 *
 * @author patrick
 */
public class ViewChangelogEntryTest {

    /**
     * Test CreateView auto-rollback.
     */
    @Test
    void testCreateView() {
        CreateViewChangelogEntry entry = new CreateViewChangelogEntry("user_summary", "SELECT id, name FROM users");
        assertEquals("user_summary", entry.getViewName());
        assertEquals("SELECT id, name FROM users", entry.getSelectQuery());
        assertTrue(entry.supportsAutoRollback());
        IChangelogEntry rollback = entry.createAutoRollback();
        assertNotNull(rollback);
        assertTrue(rollback instanceof DropViewChangelogEntry);
        assertNotNull(entry.toString());
        assertTrue(entry.getDescription().contains("user_summary"));
    }


    /**
     * Test DropView.
     */
    @Test
    void testDropView() {
        DropViewChangelogEntry entry = new DropViewChangelogEntry("user_summary");
        assertEquals("user_summary", entry.getViewName());
        assertFalse(entry.supportsAutoRollback());
        assertNull(entry.createAutoRollback());
    }


    /**
     * Test equals and hashCode.
     */
    @Test
    void testEquality() {
        CreateViewChangelogEntry e1 = new CreateViewChangelogEntry("v1", "SELECT 1");
        CreateViewChangelogEntry e2 = new CreateViewChangelogEntry("v1", "SELECT 1");

        assertEquals(e1, e2);
        assertEquals(e1.hashCode(), e2.hashCode());
    }
}
