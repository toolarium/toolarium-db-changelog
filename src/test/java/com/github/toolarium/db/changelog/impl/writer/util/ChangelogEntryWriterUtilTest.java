/*
 * ChangelogEntryWriterUtilTest.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog.impl.writer.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.toolarium.db.changelog.model.column.AddColumnChangelogEntry;
import com.github.toolarium.db.changelog.model.column.ChangelogColumnDefinition;
import com.github.toolarium.db.changelog.model.column.DropColumnChangelogEntry;
import com.github.toolarium.db.changelog.model.column.ModifyColumnChangelogEntry;
import com.github.toolarium.db.changelog.model.column.RenameColumnChangelogEntry;
import com.github.toolarium.db.changelog.model.constraint.AddNotNullConstraintChangelogEntry;
import com.github.toolarium.db.changelog.model.constraint.DropNotNullConstraintChangelogEntry;
import com.github.toolarium.db.changelog.model.index.CreateIndexChangelogEntry;
import com.github.toolarium.db.changelog.model.index.DropIndexChangelogEntry;
import com.github.toolarium.db.changelog.model.sequence.CreateSequenceChangelogEntry;
import com.github.toolarium.db.changelog.model.sequence.DropSequenceChangelogEntry;
import com.github.toolarium.db.changelog.model.sql.SqlChangelogEntry;
import com.github.toolarium.db.changelog.model.table.CreateTableChangelogEntry;
import com.github.toolarium.db.changelog.model.table.DropTableChangelogEntry;
import com.github.toolarium.db.changelog.model.table.RenameTableChangelogEntry;
import com.github.toolarium.db.changelog.model.view.CreateViewChangelogEntry;
import com.github.toolarium.db.changelog.model.view.DropViewChangelogEntry;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;


/**
 * Tests for ChangelogEntryWriterUtil.
 *
 * @author patrick
 */
public class ChangelogEntryWriterUtilTest {
    private static final String TABLE_USERS = "users";
    private static final String TABLE_NAME_KEY = "tableName";
    private static final String COL_NAME = "name";

    /**
     * Test singleton instance.
     */
    @Test
    void testSingleton() {
        ChangelogEntryWriterUtil instance1 = ChangelogEntryWriterUtil.getInstance();
        ChangelogEntryWriterUtil instance2 = ChangelogEntryWriterUtil.getInstance();
        assertNotNull(instance1);
        assertSame(instance1, instance2);
    }


    /**
     * Test SQL entry serialization.
     */
    @Test
    void testSqlEntry() {
        SqlChangelogEntry entry = new SqlChangelogEntry("CREATE TABLE test (id INT)");
        Map<String, Object> map = ChangelogEntryWriterUtil.getInstance().toMap(entry);
        assertEquals("CREATE TABLE test (id INT)", map.get("sql"));
    }


    /**
     * Test createTable entry serialization.
     */
    @SuppressWarnings("unchecked")
    @Test
    void testCreateTableEntry() {
        ChangelogColumnDefinition idCol = new ChangelogColumnDefinition("id", "INT");
        idCol.setPrimaryKey(true).setNullable(false);
        ChangelogColumnDefinition nameCol = new ChangelogColumnDefinition(COL_NAME, "VARCHAR(100)");

        CreateTableChangelogEntry entry = new CreateTableChangelogEntry(TABLE_USERS, Arrays.asList(idCol, nameCol));
        Map<String, Object> map = ChangelogEntryWriterUtil.getInstance().toMap(entry);

        assertTrue(map.containsKey("createTable"));
        Map<String, Object> createTable = (Map<String, Object>) map.get("createTable");
        assertEquals(TABLE_USERS, createTable.get(TABLE_NAME_KEY));

        List<Map<String, Object>> columns = (List<Map<String, Object>>) createTable.get("columns");
        assertEquals(2, columns.size());
        assertEquals("id", columns.get(0).get(COL_NAME));
        assertEquals("INT", columns.get(0).get("type"));

        Map<String, Object> constraints = (Map<String, Object>) columns.get(0).get("constraints");
        assertNotNull(constraints);
        assertEquals(Boolean.TRUE, constraints.get("primaryKey"));
        assertEquals(Boolean.FALSE, constraints.get("nullable"));
    }


    /**
     * Test dropTable entry serialization.
     */
    @SuppressWarnings("unchecked")
    @Test
    void testDropTableEntry() {
        DropTableChangelogEntry entry = new DropTableChangelogEntry(TABLE_USERS);
        Map<String, Object> map = ChangelogEntryWriterUtil.getInstance().toMap(entry);

        assertTrue(map.containsKey("dropTable"));
        Map<String, Object> dropTable = (Map<String, Object>) map.get("dropTable");
        assertEquals(TABLE_USERS, dropTable.get(TABLE_NAME_KEY));
    }


    /**
     * Test renameTable entry serialization.
     */
    @SuppressWarnings("unchecked")
    @Test
    void testRenameTableEntry() {
        RenameTableChangelogEntry entry = new RenameTableChangelogEntry("old_table", "new_table");
        Map<String, Object> map = ChangelogEntryWriterUtil.getInstance().toMap(entry);

        assertTrue(map.containsKey("renameTable"));
        Map<String, Object> renameTable = (Map<String, Object>) map.get("renameTable");
        assertEquals("old_table", renameTable.get("oldTableName"));
        assertEquals("new_table", renameTable.get("newTableName"));
    }


    /**
     * Test addColumn entry serialization.
     */
    @SuppressWarnings("unchecked")
    @Test
    void testAddColumnEntry() {
        ChangelogColumnDefinition col = new ChangelogColumnDefinition("email", "VARCHAR(255)");
        AddColumnChangelogEntry entry = new AddColumnChangelogEntry(TABLE_USERS, Collections.singletonList(col));
        Map<String, Object> map = ChangelogEntryWriterUtil.getInstance().toMap(entry);

        assertTrue(map.containsKey("addColumn"));
        Map<String, Object> addColumn = (Map<String, Object>) map.get("addColumn");
        assertEquals(TABLE_USERS, addColumn.get(TABLE_NAME_KEY));
        List<Map<String, Object>> columns = (List<Map<String, Object>>) addColumn.get("columns");
        assertEquals(1, columns.size());
        assertEquals("email", columns.get(0).get(COL_NAME));
    }


    /**
     * Test dropColumn entry serialization.
     */
    @SuppressWarnings("unchecked")
    @Test
    void testDropColumnEntry() {
        DropColumnChangelogEntry entry = new DropColumnChangelogEntry(TABLE_USERS, Arrays.asList("email", "phone"));
        Map<String, Object> map = ChangelogEntryWriterUtil.getInstance().toMap(entry);

        assertTrue(map.containsKey("dropColumn"));
        Map<String, Object> dropColumn = (Map<String, Object>) map.get("dropColumn");
        assertEquals(TABLE_USERS, dropColumn.get(TABLE_NAME_KEY));
        List<String> columns = (List<String>) dropColumn.get("columns");
        assertEquals(2, columns.size());
    }


    /**
     * Test renameColumn entry serialization.
     */
    @SuppressWarnings("unchecked")
    @Test
    void testRenameColumnEntry() {
        RenameColumnChangelogEntry entry = new RenameColumnChangelogEntry(TABLE_USERS, "old_col", "new_col");
        Map<String, Object> map = ChangelogEntryWriterUtil.getInstance().toMap(entry);

        assertTrue(map.containsKey("renameColumn"));
        Map<String, Object> renameColumn = (Map<String, Object>) map.get("renameColumn");
        assertEquals(TABLE_USERS, renameColumn.get(TABLE_NAME_KEY));
        assertEquals("old_col", renameColumn.get("oldColumnName"));
        assertEquals("new_col", renameColumn.get("newColumnName"));
    }


    /**
     * Test modifyColumn entry serialization.
     */
    @SuppressWarnings("unchecked")
    @Test
    void testModifyColumnEntry() {
        ModifyColumnChangelogEntry entry = new ModifyColumnChangelogEntry(TABLE_USERS, COL_NAME, "VARCHAR(200)");
        Map<String, Object> map = ChangelogEntryWriterUtil.getInstance().toMap(entry);

        assertTrue(map.containsKey("modifyColumn"));
        Map<String, Object> modifyColumn = (Map<String, Object>) map.get("modifyColumn");
        assertEquals(TABLE_USERS, modifyColumn.get(TABLE_NAME_KEY));
        assertEquals(COL_NAME, modifyColumn.get("columnName"));
        assertEquals("VARCHAR(200)", modifyColumn.get("newType"));
    }


    /**
     * Test createIndex entry serialization.
     */
    @SuppressWarnings("unchecked")
    @Test
    void testCreateIndexEntry() {
        CreateIndexChangelogEntry entry = new CreateIndexChangelogEntry(TABLE_USERS, "idx_email", Collections.singletonList("email"), true);
        Map<String, Object> map = ChangelogEntryWriterUtil.getInstance().toMap(entry);

        assertTrue(map.containsKey("createIndex"));
        Map<String, Object> createIndex = (Map<String, Object>) map.get("createIndex");
        assertEquals(TABLE_USERS, createIndex.get(TABLE_NAME_KEY));
        assertEquals("idx_email", createIndex.get("indexName"));
        assertEquals(Boolean.TRUE, createIndex.get("unique"));
    }


    /**
     * Test dropIndex entry serialization.
     */
    @SuppressWarnings("unchecked")
    @Test
    void testDropIndexEntry() {
        DropIndexChangelogEntry entry = new DropIndexChangelogEntry(TABLE_USERS, "idx_email");
        Map<String, Object> map = ChangelogEntryWriterUtil.getInstance().toMap(entry);

        assertTrue(map.containsKey("dropIndex"));
        Map<String, Object> dropIndex = (Map<String, Object>) map.get("dropIndex");
        assertEquals(TABLE_USERS, dropIndex.get(TABLE_NAME_KEY));
        assertEquals("idx_email", dropIndex.get("indexName"));
    }


    /**
     * Test addNotNullConstraint entry serialization.
     */
    @SuppressWarnings("unchecked")
    @Test
    void testAddNotNullConstraintEntry() {
        AddNotNullConstraintChangelogEntry entry = new AddNotNullConstraintChangelogEntry(TABLE_USERS, COL_NAME);
        Map<String, Object> map = ChangelogEntryWriterUtil.getInstance().toMap(entry);

        assertTrue(map.containsKey("addNotNullConstraint"));
        Map<String, Object> addNotNull = (Map<String, Object>) map.get("addNotNullConstraint");
        assertEquals(TABLE_USERS, addNotNull.get(TABLE_NAME_KEY));
        assertEquals(COL_NAME, addNotNull.get("columnName"));
    }


    /**
     * Test dropNotNullConstraint entry serialization.
     */
    @SuppressWarnings("unchecked")
    @Test
    void testDropNotNullConstraintEntry() {
        DropNotNullConstraintChangelogEntry entry = new DropNotNullConstraintChangelogEntry(TABLE_USERS, COL_NAME);
        Map<String, Object> map = ChangelogEntryWriterUtil.getInstance().toMap(entry);

        assertTrue(map.containsKey("dropNotNullConstraint"));
        Map<String, Object> dropNotNull = (Map<String, Object>) map.get("dropNotNullConstraint");
        assertEquals(TABLE_USERS, dropNotNull.get(TABLE_NAME_KEY));
        assertEquals(COL_NAME, dropNotNull.get("columnName"));
    }


    /**
     * Test createSequence entry serialization.
     */
    @SuppressWarnings("unchecked")
    @Test
    void testCreateSequenceEntry() {
        CreateSequenceChangelogEntry entry = new CreateSequenceChangelogEntry("user_seq");
        Map<String, Object> map = ChangelogEntryWriterUtil.getInstance().toMap(entry);

        assertTrue(map.containsKey("createSequence"));
        Map<String, Object> createSeq = (Map<String, Object>) map.get("createSequence");
        assertEquals("user_seq", createSeq.get("sequenceName"));
    }


    /**
     * Test dropSequence entry serialization.
     */
    @SuppressWarnings("unchecked")
    @Test
    void testDropSequenceEntry() {
        DropSequenceChangelogEntry entry = new DropSequenceChangelogEntry("user_seq");
        Map<String, Object> map = ChangelogEntryWriterUtil.getInstance().toMap(entry);

        assertTrue(map.containsKey("dropSequence"));
        Map<String, Object> dropSeq = (Map<String, Object>) map.get("dropSequence");
        assertEquals("user_seq", dropSeq.get("sequenceName"));
    }


    /**
     * Test createView entry serialization.
     */
    @SuppressWarnings("unchecked")
    @Test
    void testCreateViewEntry() {
        CreateViewChangelogEntry entry = new CreateViewChangelogEntry("user_view", "SELECT id, name FROM users");
        Map<String, Object> map = ChangelogEntryWriterUtil.getInstance().toMap(entry);

        assertTrue(map.containsKey("createView"));
        Map<String, Object> createView = (Map<String, Object>) map.get("createView");
        assertEquals("user_view", createView.get("viewName"));
        assertEquals("SELECT id, name FROM users", createView.get("selectQuery"));
    }


    /**
     * Test dropView entry serialization.
     */
    @SuppressWarnings("unchecked")
    @Test
    void testDropViewEntry() {
        DropViewChangelogEntry entry = new DropViewChangelogEntry("user_view");
        Map<String, Object> map = ChangelogEntryWriterUtil.getInstance().toMap(entry);

        assertTrue(map.containsKey("dropView"));
        Map<String, Object> dropView = (Map<String, Object>) map.get("dropView");
        assertEquals("user_view", dropView.get("viewName"));
    }


    /**
     * Test column definition serialization with constraints.
     */
    @SuppressWarnings("unchecked")
    @Test
    void testColumnMapWithConstraints() {
        ChangelogColumnDefinition col = new ChangelogColumnDefinition("id", "BIGINT");
        col.setPrimaryKey(true).setNullable(false).setAutoIncrement(true);
        col.setDefaultValue("0");

        Map<String, Object> map = ChangelogEntryWriterUtil.getInstance().toColumnMap(col);
        assertEquals("id", map.get(COL_NAME));
        assertEquals("BIGINT", map.get("type"));
        assertEquals(Boolean.TRUE, map.get("autoIncrement"));
        assertEquals("0", map.get("defaultValue"));

        Map<String, Object> constraints = (Map<String, Object>) map.get("constraints");
        assertNotNull(constraints);
        assertEquals(Boolean.TRUE, constraints.get("primaryKey"));
        assertEquals(Boolean.FALSE, constraints.get("nullable"));
    }


    /**
     * Test column definition serialization without constraints (default values).
     */
    @Test
    void testColumnMapWithoutConstraints() {
        ChangelogColumnDefinition col = new ChangelogColumnDefinition(COL_NAME, "VARCHAR(100)");
        Map<String, Object> map = ChangelogEntryWriterUtil.getInstance().toColumnMap(col);
        assertEquals(COL_NAME, map.get(COL_NAME));
        assertEquals("VARCHAR(100)", map.get("type"));
        assertFalse(map.containsKey("constraints"));
        assertFalse(map.containsKey("autoIncrement"));
        assertFalse(map.containsKey("defaultValue"));
    }
}
