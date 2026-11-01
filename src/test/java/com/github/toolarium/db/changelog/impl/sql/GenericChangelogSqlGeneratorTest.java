/*
 * GenericChangelogSqlGeneratorTest.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog.impl.sql;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.toolarium.db.changelog.dto.column.AddColumnChangelogEntry;
import com.github.toolarium.db.changelog.dto.column.ChangelogColumnDefinition;
import com.github.toolarium.db.changelog.dto.column.DropColumnChangelogEntry;
import com.github.toolarium.db.changelog.dto.index.CreateIndexChangelogEntry;
import com.github.toolarium.db.changelog.dto.index.DropIndexChangelogEntry;
import com.github.toolarium.db.changelog.dto.table.CreateTableChangelogEntry;
import com.github.toolarium.db.changelog.dto.table.DropTableChangelogEntry;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;


/**
 * Tests for GenericChangelogSqlGeneratorImpl SQL generation.
 *
 * @author patrick
 */
public class GenericChangelogSqlGeneratorTest {
    private static final String TABLE_USERS = "users";
    private static final String TABLE_T = "t";
    private final IChangelogSqlGenerator generator = ChangelogSqlGeneratorFactory.getInstance().getSqlGenerator();


    /**
     * Test CREATE TABLE SQL generation.
     */
    @Test
    void testCreateTable() {
        ChangelogColumnDefinition idCol = new ChangelogColumnDefinition("id", "BIGINT");
        idCol.setPrimaryKey(true).setNullable(false).setAutoIncrement(true);

        ChangelogColumnDefinition nameCol = new ChangelogColumnDefinition("name", "VARCHAR(100)");
        nameCol.setNullable(false);

        ChangelogColumnDefinition emailCol = new ChangelogColumnDefinition("email", "VARCHAR(255)");
        emailCol.setUnique(true);

        CreateTableChangelogEntry entry = new CreateTableChangelogEntry(TABLE_USERS,
                Arrays.asList(idCol, nameCol, emailCol));

        List<String> sql = generator.generateSql(entry);
        assertEquals(1, sql.size());
        String stmt = sql.get(0);

        assertTrue(stmt.startsWith("CREATE TABLE users ("));
        assertTrue(stmt.contains("id BIGINT AUTO_INCREMENT NOT NULL"));
        assertTrue(stmt.contains("name VARCHAR(100) NOT NULL"));
        assertTrue(stmt.contains("email VARCHAR(255) UNIQUE"));
        assertTrue(stmt.contains("PRIMARY KEY (id)"));
    }


    /**
     * Test CREATE TABLE with default values.
     */
    @Test
    void testCreateTableWithDefaults() {
        ChangelogColumnDefinition col = new ChangelogColumnDefinition("status", "VARCHAR(20)");
        col.setDefaultValue("active");

        ChangelogColumnDefinition tsCol = new ChangelogColumnDefinition("created_at", "TIMESTAMP");
        tsCol.setDefaultValueComputed("CURRENT_TIMESTAMP");

        CreateTableChangelogEntry entry = new CreateTableChangelogEntry("orders",
                Arrays.asList(col, tsCol));

        List<String> sql = generator.generateSql(entry);
        String stmt = sql.get(0);

        assertTrue(stmt.contains("DEFAULT 'active'"));
        assertTrue(stmt.contains("DEFAULT CURRENT_TIMESTAMP"));
    }


    /**
     * Test DROP TABLE SQL generation.
     */
    @Test
    void testDropTable() {
        DropTableChangelogEntry entry = new DropTableChangelogEntry(TABLE_USERS);

        List<String> sql = generator.generateSql(entry);
        assertEquals(1, sql.size());
        assertEquals("DROP TABLE users", sql.get(0));
    }


    /**
     * Test ADD COLUMN SQL generation.
     */
    @Test
    void testAddColumn() {
        ChangelogColumnDefinition col1 = new ChangelogColumnDefinition("phone", "VARCHAR(20)");
        ChangelogColumnDefinition col2 = new ChangelogColumnDefinition("age", "INT");
        col2.setNullable(false);

        AddColumnChangelogEntry entry = new AddColumnChangelogEntry(TABLE_USERS,
                Arrays.asList(col1, col2));

        List<String> sql = generator.generateSql(entry);
        assertEquals(2, sql.size());
        assertEquals("ALTER TABLE users ADD COLUMN phone VARCHAR(20)", sql.get(0));
        assertEquals("ALTER TABLE users ADD COLUMN age INT NOT NULL", sql.get(1));
    }


    /**
     * Test DROP COLUMN SQL generation.
     */
    @Test
    void testDropColumn() {
        DropColumnChangelogEntry entry = new DropColumnChangelogEntry(TABLE_USERS,
                Arrays.asList("phone", "fax"));

        List<String> sql = generator.generateSql(entry);
        assertEquals(2, sql.size());
        assertEquals("ALTER TABLE users DROP COLUMN phone", sql.get(0));
        assertEquals("ALTER TABLE users DROP COLUMN fax", sql.get(1));
    }


    /**
     * Test CREATE INDEX SQL generation.
     */
    @Test
    void testCreateIndex() {
        CreateIndexChangelogEntry entry = new CreateIndexChangelogEntry(TABLE_USERS,
                "idx_email", Arrays.asList("email"), false);

        List<String> sql = generator.generateSql(entry);
        assertEquals(1, sql.size());
        assertEquals("CREATE INDEX idx_email ON users (email)", sql.get(0));
    }


    /**
     * Test CREATE UNIQUE INDEX SQL generation.
     */
    @Test
    void testCreateUniqueIndex() {
        CreateIndexChangelogEntry entry = new CreateIndexChangelogEntry(TABLE_USERS,
                "idx_email", Arrays.asList("email"), true);

        List<String> sql = generator.generateSql(entry);
        assertTrue(sql.get(0).contains("UNIQUE INDEX"));
    }


    /**
     * Test CREATE INDEX with multiple columns.
     */
    @Test
    void testCreateCompositeIndex() {
        CreateIndexChangelogEntry entry = new CreateIndexChangelogEntry(TABLE_USERS,
                "idx_name_email", Arrays.asList("last_name", "first_name"), false);

        List<String> sql = generator.generateSql(entry);
        assertEquals("CREATE INDEX idx_name_email ON users (last_name, first_name)", sql.get(0));
    }


    /**
     * Test DROP INDEX SQL generation.
     */
    @Test
    void testDropIndex() {
        DropIndexChangelogEntry entry = new DropIndexChangelogEntry(TABLE_USERS, "idx_email");

        List<String> sql = generator.generateSql(entry);
        assertEquals(1, sql.size());
        assertEquals("DROP INDEX idx_email", sql.get(0));
    }


    /**
     * Test unsupported entry returns empty list.
     */
    @Test
    void testUnsupportedEntryReturnsEmpty() {
        List<String> sql = generator.generateSql(new com.github.toolarium.db.changelog.dto.sql.SqlChangelogEntry("SELECT 1"));
        assertTrue(sql.isEmpty());
    }


    /**
     * Test supports method.
     */
    @Test
    void testSupports() {
        assertTrue(generator.supports(new CreateTableChangelogEntry(TABLE_T, Collections.emptyList()), null));
        assertTrue(generator.supports(new DropTableChangelogEntry(TABLE_T), null));
        assertTrue(generator.supports(new AddColumnChangelogEntry(TABLE_T, Collections.emptyList()), null));
        assertTrue(generator.supports(new DropColumnChangelogEntry(TABLE_T, Collections.emptyList()), null));
        assertTrue(generator.supports(new CreateIndexChangelogEntry(TABLE_T, "i", Collections.emptyList(), false), null));
        assertTrue(generator.supports(new DropIndexChangelogEntry(TABLE_T, "i"), null));
        assertFalse(generator.supports(new com.github.toolarium.db.changelog.dto.sql.SqlChangelogEntry("SELECT 1"), null));
    }
}
