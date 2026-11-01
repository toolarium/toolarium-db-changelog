/*
 * ChangelogDtoTest.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog.dto;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;


/**
 * Tests for DTO equals, hashCode, toString, and behavior.
 *
 * @author patrick
 */
public class ChangelogDtoTest {
    private static final String TEST_AUTHOR = "author";
    private static final String TEST_FILEPATH = "file.db.yaml";


    /**
     * Test ChangelogChangeSetIdentity equals, hashCode, toString.
     */
    @Test
    void testChangeSetIdentity() {
        ChangelogChangeSetIdentity id1 = new ChangelogChangeSetIdentity("cs-1", TEST_AUTHOR, TEST_FILEPATH);
        ChangelogChangeSetIdentity id2 = new ChangelogChangeSetIdentity("cs-1", TEST_AUTHOR, TEST_FILEPATH);
        ChangelogChangeSetIdentity id3 = new ChangelogChangeSetIdentity("cs-2", TEST_AUTHOR, TEST_FILEPATH);

        assertEquals(id1, id2);
        assertEquals(id1.hashCode(), id2.hashCode());
        assertNotEquals(id1, id3);
        assertNotNull(id1.toString());
        assertTrue(id1.toString().contains("cs-1"));
    }


    /**
     * Test Changelog equals, hashCode, toString.
     */
    @Test
    void testChangelog() {
        Changelog cl1 = new Changelog(TEST_FILEPATH, Collections.emptyList());
        Changelog cl2 = new Changelog(TEST_FILEPATH, Collections.emptyList());
        Changelog cl3 = new Changelog("other.db.yaml", Collections.emptyList());

        assertEquals(cl1, cl2);
        assertEquals(cl1.hashCode(), cl2.hashCode());
        assertNotEquals(cl1, cl3);
        assertTrue(cl1.toString().contains(TEST_FILEPATH));
        assertTrue(cl1.toString().contains("0 changeset"));
    }


    /**
     * Test ChangelogChangeSet equals, hashCode, toString.
     */
    @Test
    void testChangelogChangeSet() {
        ChangelogChangeSetIdentity id = new ChangelogChangeSetIdentity("cs-1", TEST_AUTHOR, "f.db.yaml");
        List<IChangelogEntry> entries = Collections.singletonList(new SqlChangelogEntry("SELECT 1"));
        ChangelogChangeSet cs1 = new ChangelogChangeSet(id, entries);
        ChangelogChangeSet cs2 = new ChangelogChangeSet(id, entries);

        assertEquals(cs1, cs2);
        assertEquals(cs1.hashCode(), cs2.hashCode());
        assertTrue(cs1.toString().contains("cs-1"));
        assertEquals(TEST_AUTHOR, cs1.getIdentity().getAuthor());
        assertNotNull(cs1.getContexts());
        assertTrue(cs1.getContexts().isEmpty());
        assertTrue(cs1.getLabels().isEmpty());
        assertTrue(cs1.isFailOnError());
        assertTrue(cs1.isRunInTransaction());
        assertFalse(cs1.isRunAlways());
        assertFalse(cs1.isRunOnChange());
    }


    /**
     * Test ChangelogChangeSet with all attributes.
     */
    @Test
    void testChangelogChangeSetFullAttributes() {
        ChangelogChangeSetIdentity id = new ChangelogChangeSetIdentity("cs-full", "dev", "f.db.yaml");
        List<IChangelogEntry> entries = Collections.singletonList(new SqlChangelogEntry("SELECT 1"));
        List<IChangelogEntry> rollback = Collections.singletonList(new SqlChangelogEntry("SELECT 0"));
        List<String> contexts = Arrays.asList("dev", "test");
        List<String> labels = Arrays.asList("v1.0");

        ChangelogChangeSet cs = new ChangelogChangeSet(id, entries, rollback, "my comment",
                true, true, false, false, contexts, labels);

        assertTrue(cs.isRunAlways());
        assertTrue(cs.isRunOnChange());
        assertFalse(cs.isFailOnError());
        assertFalse(cs.isRunInTransaction());
        assertEquals("my comment", cs.getComment());
        assertEquals(2, cs.getContexts().size());
        assertEquals("v1.0", cs.getLabels().get(0));
        assertFalse(cs.getRollbackEntries().isEmpty());
    }


    /**
     * Test ChangelogChangeSet with null contexts and labels defaults to empty list.
     */
    @Test
    void testChangelogChangeSetNullContextsLabels() {
        ChangelogChangeSetIdentity id = new ChangelogChangeSetIdentity("cs-null", "dev", "f.db.yaml");
        List<IChangelogEntry> entries = Collections.singletonList(new SqlChangelogEntry("SELECT 1"));

        ChangelogChangeSet cs = new ChangelogChangeSet(id, entries, Collections.emptyList(), null,
                false, false, true, true, null, null);

        assertNotNull(cs.getContexts());
        assertTrue(cs.getContexts().isEmpty());
        assertNotNull(cs.getLabels());
        assertTrue(cs.getLabels().isEmpty());
    }


    /**
     * Test SqlChangelogEntry attributes and defaults.
     */
    @Test
    void testSqlChangelogEntry() {
        SqlChangelogEntry entry = new SqlChangelogEntry("CREATE TABLE t (id INT)");
        assertEquals("CREATE TABLE t (id INT)", entry.getSql());
        assertTrue(entry.isSplitStatements());
        assertEquals(";", entry.getEndDelimiter());
        assertFalse(entry.isStripComments());
        assertNull(entry.getDbms());
        assertFalse(entry.supportsAutoRollback());
        assertNull(entry.createAutoRollback());
        assertTrue(entry.getDescription().contains("CREATE TABLE"));
    }


    /**
     * Test SqlChangelogEntry with custom attributes.
     */
    @Test
    void testSqlChangelogEntryCustomAttributes() {
        SqlChangelogEntry entry = new SqlChangelogEntry("SELECT 1", false, "GO", true, "postgresql");
        assertFalse(entry.isSplitStatements());
        assertEquals("GO", entry.getEndDelimiter());
        assertTrue(entry.isStripComments());
        assertEquals("postgresql", entry.getDbms());
    }


    /**
     * Test SqlChangelogEntry description truncation for long SQL.
     */
    @Test
    void testSqlChangelogEntryLongDescription() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            sb.append("SELECT * FROM very_long_table_name ");
        }
        SqlChangelogEntry entry = new SqlChangelogEntry(sb.toString());
        assertTrue(entry.getDescription().length() <= 85);
        assertTrue(entry.getDescription().endsWith("..."));
    }


    /**
     * Test SqlChangelogEntry with null SQL.
     */
    @Test
    void testSqlChangelogEntryNullSql() {
        SqlChangelogEntry entry = new SqlChangelogEntry(null);
        assertEquals("sql", entry.getDescription());
    }


    /**
     * Test SqlFileChangelogEntry extends SqlChangelogEntry.
     */
    @Test
    void testSqlFileChangelogEntry() {
        SqlFileChangelogEntry entry = new SqlFileChangelogEntry("sql/init.sql");
        assertEquals("sql/init.sql", entry.getPath());
        assertEquals("UTF-8", entry.getEncoding());
        assertTrue(entry.isRelativeToChangelogFile());
        assertTrue(entry.isSplitStatements());
        assertEquals(";", entry.getEndDelimiter());
        assertFalse(entry.isStripComments());
        assertNull(entry.getDbms());
        assertEquals("sqlFile: sql/init.sql", entry.getDescription());
        assertFalse(entry.supportsAutoRollback());
        assertTrue(entry instanceof SqlChangelogEntry);
    }


    /**
     * Test SqlFileChangelogEntry with custom attributes.
     */
    @Test
    void testSqlFileChangelogEntryCustom() {
        SqlFileChangelogEntry entry = new SqlFileChangelogEntry("sql/proc.sql",
                false, "GO", true, "ISO-8859-1", false, "sqlserver");
        assertEquals("sql/proc.sql", entry.getPath());
        assertFalse(entry.isSplitStatements());
        assertEquals("GO", entry.getEndDelimiter());
        assertTrue(entry.isStripComments());
        assertEquals("ISO-8859-1", entry.getEncoding());
        assertFalse(entry.isRelativeToChangelogFile());
        assertEquals("sqlserver", entry.getDbms());
    }


    /**
     * Test ChangelogTrackingEntry equals, hashCode, toString.
     */
    @Test
    void testChangelogTrackingEntry() {
        ChangelogTrackingEntry e1 = new ChangelogTrackingEntry();
        e1.setId("id-1");
        e1.setAuthor(TEST_AUTHOR);
        e1.setFilepath(TEST_FILEPATH);
        e1.setExecutionType(ChangelogExecutionType.EXECUTED);

        ChangelogTrackingEntry e2 = new ChangelogTrackingEntry();
        e2.setId("id-1");
        e2.setAuthor(TEST_AUTHOR);
        e2.setFilepath(TEST_FILEPATH);

        assertEquals(e1, e2);
        assertEquals(e1.hashCode(), e2.hashCode());
        assertTrue(e1.toString().contains("id-1"));
        assertEquals(ChangelogExecutionType.EXECUTED, e1.getExecutionType());
    }


    /**
     * Test ChangelogExecutionType values.
     */
    @Test
    void testChangelogExecutionType() {
        assertEquals(5, ChangelogExecutionType.values().length);
        assertNotNull(ChangelogExecutionType.valueOf("EXECUTED"));
        assertNotNull(ChangelogExecutionType.valueOf("RERAN"));
        assertNotNull(ChangelogExecutionType.valueOf("ROLLED_BACK"));
        assertNotNull(ChangelogExecutionType.valueOf("SKIPPED"));
        assertNotNull(ChangelogExecutionType.valueOf("MARK_RAN"));
    }
}
