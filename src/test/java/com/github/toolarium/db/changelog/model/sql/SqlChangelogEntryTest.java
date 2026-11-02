/*
 * SqlChangelogEntryTest.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog.model.sql;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;


/**
 * Tests for SQL changelog entry DTOs.
 *
 * @author patrick
 */
public class SqlChangelogEntryTest {

    /**
     * Test SqlChangelogEntry defaults.
     */
    @Test
    void testSqlChangelogEntryDefaults() {
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
    void testSqlChangelogEntryCustom() {
        SqlChangelogEntry entry = new SqlChangelogEntry("SELECT 1", false, "GO", true, "postgresql");
        assertFalse(entry.isSplitStatements());
        assertEquals("GO", entry.getEndDelimiter());
        assertTrue(entry.isStripComments());
        assertEquals("postgresql", entry.getDbms());
    }


    /**
     * Test SqlChangelogEntry description truncation.
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
     * Test SqlFileChangelogEntry defaults.
     */
    @Test
    void testSqlFileChangelogEntryDefaults() {
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
}
