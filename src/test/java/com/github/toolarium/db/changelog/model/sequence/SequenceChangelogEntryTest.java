/*
 * SequenceChangelogEntryTest.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog.model.sequence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.toolarium.db.changelog.model.IChangelogEntry;
import org.junit.jupiter.api.Test;


/**
 * Tests for sequence changelog entry DTOs.
 *
 * @author patrick
 */
public class SequenceChangelogEntryTest {

    /**
     * Test CreateSequence auto-rollback.
     */
    @Test
    void testCreateSequence() {
        CreateSequenceChangelogEntry entry = new CreateSequenceChangelogEntry("user_id_seq");
        assertEquals("user_id_seq", entry.getSequenceName());
        assertTrue(entry.supportsAutoRollback());
        IChangelogEntry rollback = entry.createAutoRollback();
        assertNotNull(rollback);
        assertTrue(rollback instanceof DropSequenceChangelogEntry);
        assertNotNull(entry.toString());
        assertTrue(entry.getDescription().contains("user_id_seq"));
    }


    /**
     * Test DropSequence.
     */
    @Test
    void testDropSequence() {
        DropSequenceChangelogEntry entry = new DropSequenceChangelogEntry("user_id_seq");
        assertEquals("user_id_seq", entry.getSequenceName());
        assertFalse(entry.supportsAutoRollback());
        assertNull(entry.createAutoRollback());
    }


    /**
     * Test equals and hashCode.
     */
    @Test
    void testEquality() {
        CreateSequenceChangelogEntry e1 = new CreateSequenceChangelogEntry("seq1");
        CreateSequenceChangelogEntry e2 = new CreateSequenceChangelogEntry("seq1");

        assertEquals(e1, e2);
        assertEquals(e1.hashCode(), e2.hashCode());
    }
}
