/*
 * ChangelogSqlRollbackGeneratorTest.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog.impl.rollback;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;


/**
 * Tests for Level 1 SQL rollback generator.
 *
 * @author patrick
 */
public class ChangelogSqlRollbackGeneratorTest {
    private final IChangelogSqlRollbackGenerator generator =
            ChangelogSqlRollbackGeneratorFactory.getInstance().getSqlRollbackGenerator();


    /**
     * Test factory singleton.
     */
    @Test
    void testFactory() {
        assertSame(ChangelogSqlRollbackGeneratorFactory.getInstance(),
                ChangelogSqlRollbackGeneratorFactory.getInstance());
        assertNotNull(generator);
    }


    /**
     * Test CREATE TABLE generates DROP TABLE.
     */
    @Test
    void testCreateTable() {
        assertEquals("DROP TABLE users",
                generator.generateRollbackSql("CREATE TABLE users (id INT PRIMARY KEY)"));
        assertTrue(generator.canGenerateRollback("CREATE TABLE users (id INT)"));
    }


    /**
     * Test CREATE TABLE IF NOT EXISTS.
     */
    @Test
    void testCreateTableIfNotExists() {
        assertEquals("DROP TABLE users",
                generator.generateRollbackSql("CREATE TABLE IF NOT EXISTS users (id INT)"));
    }


    /**
     * Test ALTER TABLE ADD COLUMN generates DROP COLUMN.
     */
    @Test
    void testAddColumn() {
        assertEquals("ALTER TABLE users DROP COLUMN phone",
                generator.generateRollbackSql("ALTER TABLE users ADD COLUMN phone VARCHAR(20)"));
    }


    /**
     * Test ALTER TABLE ADD without COLUMN keyword.
     */
    @Test
    void testAddWithoutColumnKeyword() {
        assertEquals("ALTER TABLE users DROP COLUMN email",
                generator.generateRollbackSql("ALTER TABLE users ADD email VARCHAR(255)"));
    }


    /**
     * Test CREATE INDEX generates DROP INDEX.
     */
    @Test
    void testCreateIndex() {
        assertEquals("DROP INDEX idx_email",
                generator.generateRollbackSql("CREATE INDEX idx_email ON users (email)"));
    }


    /**
     * Test CREATE UNIQUE INDEX.
     */
    @Test
    void testCreateUniqueIndex() {
        assertEquals("DROP INDEX idx_email",
                generator.generateRollbackSql("CREATE UNIQUE INDEX idx_email ON users (email)"));
    }


    /**
     * Test CREATE VIEW generates DROP VIEW.
     */
    @Test
    void testCreateView() {
        assertEquals("DROP VIEW user_summary",
                generator.generateRollbackSql("CREATE VIEW user_summary AS SELECT id, name FROM users"));
    }


    /**
     * Test CREATE OR REPLACE VIEW.
     */
    @Test
    void testCreateOrReplaceView() {
        assertEquals("DROP VIEW user_summary",
                generator.generateRollbackSql("CREATE OR REPLACE VIEW user_summary AS SELECT id FROM users"));
    }


    /**
     * Test CREATE SEQUENCE generates DROP SEQUENCE.
     */
    @Test
    void testCreateSequence() {
        assertEquals("DROP SEQUENCE user_id_seq",
                generator.generateRollbackSql("CREATE SEQUENCE user_id_seq"));
    }


    /**
     * Test RENAME TABLE generates reverse rename.
     */
    @Test
    void testRenameTable() {
        assertEquals("ALTER TABLE new_users RENAME TO old_users",
                generator.generateRollbackSql("ALTER TABLE old_users RENAME TO new_users"));
    }


    /**
     * Test RENAME COLUMN generates reverse rename.
     */
    @Test
    void testRenameColumn() {
        assertEquals("ALTER TABLE users RENAME COLUMN new_name TO old_name",
                generator.generateRollbackSql("ALTER TABLE users RENAME COLUMN old_name TO new_name"));
    }


    /**
     * Test unsupported SQL returns null.
     */
    @Test
    void testUnsupported() {
        assertNull(generator.generateRollbackSql("DROP TABLE users"));
        assertNull(generator.generateRollbackSql("INSERT INTO users VALUES (1, 'test')"));
        assertNull(generator.generateRollbackSql("UPDATE users SET name = 'test'"));
        assertNull(generator.generateRollbackSql("DELETE FROM users"));
        assertFalse(generator.canGenerateRollback("DROP TABLE users"));
    }


    /**
     * Test null input.
     */
    @Test
    void testNullInput() {
        assertNull(generator.generateRollbackSql(null));
        assertFalse(generator.canGenerateRollback(null));
    }


    /**
     * Test case insensitivity.
     */
    @Test
    void testCaseInsensitive() {
        assertEquals("DROP TABLE Users",
                generator.generateRollbackSql("create table Users (id int)"));
    }
}
