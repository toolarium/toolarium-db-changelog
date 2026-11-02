/*
 * ChangelogSqlRollbackPatternTest.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog.impl.rollback.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.regex.Matcher;
import org.junit.jupiter.api.Test;


/**
 * Tests for the individual SQL rollback patterns.
 *
 * @author patrick
 */
public class ChangelogSqlRollbackPatternTest {

    /**
     * Test CREATE TABLE pattern with simple table.
     */
    @Test
    void testCreateTableSimple() {
        Matcher m = ChangelogSqlRollbackGeneratorImpl.CREATE_TABLE.matcher("CREATE TABLE users (id INT)");
        assertTrue(m.find());
        assertEquals("users", m.group(1));
    }


    /**
     * Test CREATE TABLE pattern with IF NOT EXISTS.
     */
    @Test
    void testCreateTableIfNotExists() {
        Matcher m = ChangelogSqlRollbackGeneratorImpl.CREATE_TABLE.matcher("CREATE TABLE IF NOT EXISTS my_table (id INT)");
        assertTrue(m.find());
        assertEquals("my_table", m.group(1));
    }


    /**
     * Test CREATE TABLE pattern with leading whitespace.
     */
    @Test
    void testCreateTableWithWhitespace() {
        Matcher m = ChangelogSqlRollbackGeneratorImpl.CREATE_TABLE.matcher("   CREATE TABLE  schema.users (id INT)");
        assertTrue(m.find());
        assertEquals("schema.users", m.group(1));
    }


    /**
     * Test CREATE TABLE pattern is case insensitive.
     */
    @Test
    void testCreateTableCaseInsensitive() {
        Matcher m = ChangelogSqlRollbackGeneratorImpl.CREATE_TABLE.matcher("create table MyTable (id int)");
        assertTrue(m.find());
        assertEquals("MyTable", m.group(1));
    }


    /**
     * Test ALTER ADD COLUMN pattern with COLUMN keyword.
     */
    @Test
    void testAlterAddColumnWithKeyword() {
        Matcher m = ChangelogSqlRollbackGeneratorImpl.ALTER_ADD_COLUMN.matcher("ALTER TABLE users ADD COLUMN email VARCHAR(255)");
        assertTrue(m.find());
        assertEquals("users", m.group(1));
        assertEquals("email", m.group(2));
    }


    /**
     * Test ALTER ADD COLUMN pattern without COLUMN keyword.
     */
    @Test
    void testAlterAddColumnWithoutKeyword() {
        Matcher m = ChangelogSqlRollbackGeneratorImpl.ALTER_ADD_COLUMN.matcher("ALTER TABLE users ADD phone VARCHAR(20)");
        assertTrue(m.find());
        assertEquals("users", m.group(1));
        assertEquals("phone", m.group(2));
    }


    /**
     * Test CREATE INDEX pattern with simple index.
     */
    @Test
    void testCreateIndex() {
        Matcher m = ChangelogSqlRollbackGeneratorImpl.CREATE_INDEX.matcher("CREATE INDEX idx_email ON users (email)");
        assertTrue(m.find());
        assertEquals("idx_email", m.group(1));
    }


    /**
     * Test CREATE UNIQUE INDEX pattern.
     */
    @Test
    void testCreateUniqueIndex() {
        Matcher m = ChangelogSqlRollbackGeneratorImpl.CREATE_INDEX.matcher("CREATE UNIQUE INDEX idx_uniq ON users (email)");
        assertTrue(m.find());
        assertEquals("idx_uniq", m.group(1));
    }


    /**
     * Test CREATE INDEX IF NOT EXISTS.
     */
    @Test
    void testCreateIndexIfNotExists() {
        Matcher m = ChangelogSqlRollbackGeneratorImpl.CREATE_INDEX.matcher("CREATE INDEX IF NOT EXISTS idx_new ON users (id)");
        assertTrue(m.find());
        assertEquals("idx_new", m.group(1));
    }


    /**
     * Test CREATE VIEW pattern.
     */
    @Test
    void testCreateView() {
        Matcher m = ChangelogSqlRollbackGeneratorImpl.CREATE_VIEW.matcher("CREATE VIEW user_summary AS SELECT id FROM users");
        assertTrue(m.find());
        assertEquals("user_summary", m.group(1));
    }


    /**
     * Test CREATE OR REPLACE VIEW pattern.
     */
    @Test
    void testCreateOrReplaceView() {
        Matcher m = ChangelogSqlRollbackGeneratorImpl.CREATE_VIEW.matcher("CREATE OR REPLACE VIEW v_report AS SELECT 1");
        assertTrue(m.find());
        assertEquals("v_report", m.group(1));
    }


    /**
     * Test CREATE SEQUENCE pattern.
     */
    @Test
    void testCreateSequence() {
        Matcher m = ChangelogSqlRollbackGeneratorImpl.CREATE_SEQUENCE.matcher("CREATE SEQUENCE user_id_seq");
        assertTrue(m.find());
        assertEquals("user_id_seq", m.group(1));
    }


    /**
     * Test CREATE SEQUENCE IF NOT EXISTS.
     */
    @Test
    void testCreateSequenceIfNotExists() {
        Matcher m = ChangelogSqlRollbackGeneratorImpl.CREATE_SEQUENCE.matcher("CREATE SEQUENCE IF NOT EXISTS order_seq START WITH 1");
        assertTrue(m.find());
        assertEquals("order_seq", m.group(1));
    }


    /**
     * Test RENAME TABLE pattern.
     */
    @Test
    void testRenameTable() {
        Matcher m = ChangelogSqlRollbackGeneratorImpl.RENAME_TABLE.matcher("ALTER TABLE old_name RENAME TO new_name");
        assertTrue(m.find());
        assertEquals("old_name", m.group(1));
        assertEquals("new_name", m.group(2));
    }


    /**
     * Test RENAME COLUMN pattern.
     */
    @Test
    void testRenameColumn() {
        Matcher m = ChangelogSqlRollbackGeneratorImpl.RENAME_COLUMN.matcher("ALTER TABLE users RENAME COLUMN old_col TO new_col");
        assertTrue(m.find());
        assertEquals("users", m.group(1));
        assertEquals("old_col", m.group(2));
        assertEquals("new_col", m.group(3));
    }


    /**
     * Test patterns do not match unrelated SQL.
     */
    @Test
    void testNoMatchOnUnrelatedSql() {
        assertFalse(ChangelogSqlRollbackGeneratorImpl.CREATE_TABLE.matcher("DROP TABLE users").find());
        assertFalse(ChangelogSqlRollbackGeneratorImpl.ALTER_ADD_COLUMN.matcher("ALTER TABLE users DROP COLUMN email").find());
        assertFalse(ChangelogSqlRollbackGeneratorImpl.CREATE_INDEX.matcher("DROP INDEX idx_email").find());
        assertFalse(ChangelogSqlRollbackGeneratorImpl.CREATE_VIEW.matcher("DROP VIEW v_report").find());
        assertFalse(ChangelogSqlRollbackGeneratorImpl.CREATE_SEQUENCE.matcher("DROP SEQUENCE seq1").find());
    }
}
