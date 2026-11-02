/*
 * ChangelogEntryParserUtilTest.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog.impl.parser.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.toolarium.db.changelog.exception.ChangelogParseException;
import com.github.toolarium.db.changelog.impl.resolver.IChangelogParameterResolver;
import com.github.toolarium.db.changelog.model.IChangelogEntry;
import com.github.toolarium.db.changelog.model.column.AddColumnChangelogEntry;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;


/**
 * Tests for ChangelogEntryParserUtil.
 *
 * @author patrick
 */
public class ChangelogEntryParserUtilTest {
    private static final String TEST_PATH = "test.db.yaml";
    private static final String TEST_TABLE = "users";
    private static final String TEST_COLUMN = "email";
    private final IChangelogParameterResolver resolver = new NoOpParameterResolver();


    /**
     * Test singleton instance.
     */
    @Test
    void testSingleton() {
        assertSame(ChangelogEntryParserUtil.getInstance(), ChangelogEntryParserUtil.getInstance());
    }


    /**
     * Test parsing sql entry.
     */
    @Test
    void testParseSql() throws ChangelogParseException {
        Map<String, Object> map = new HashMap<>();
        map.put("sql", "SELECT 1");

        IChangelogEntry entry = ChangelogEntryParserUtil.getInstance().parseEntry(map, resolver, TEST_PATH);
        assertTrue(entry instanceof SqlChangelogEntry);
        assertEquals("SELECT 1", ((SqlChangelogEntry) entry).getSql());
    }


    /**
     * Test parsing createTable entry.
     */
    @Test
    void testParseCreateTable() throws ChangelogParseException {
        Map<String, Object> colMap = new HashMap<>();
        colMap.put("name", "id");
        colMap.put("type", "INT");

        Map<String, Object> innerMap = new HashMap<>();
        innerMap.put("tableName", TEST_TABLE);
        innerMap.put("columns", Collections.singletonList(colMap));

        Map<String, Object> entryMap = new HashMap<>();
        entryMap.put("createTable", innerMap);

        IChangelogEntry entry = ChangelogEntryParserUtil.getInstance().parseEntry(entryMap, resolver, TEST_PATH);
        assertTrue(entry instanceof CreateTableChangelogEntry);
        assertEquals(TEST_TABLE, ((CreateTableChangelogEntry) entry).getTableName());
    }


    /**
     * Test parsing dropTable entry.
     */
    @Test
    void testParseDropTable() throws ChangelogParseException {
        Map<String, Object> innerMap = new HashMap<>();
        innerMap.put("tableName", TEST_TABLE);

        Map<String, Object> entryMap = new HashMap<>();
        entryMap.put("dropTable", innerMap);

        IChangelogEntry entry = ChangelogEntryParserUtil.getInstance().parseEntry(entryMap, resolver, TEST_PATH);
        assertTrue(entry instanceof DropTableChangelogEntry);
        assertEquals(TEST_TABLE, ((DropTableChangelogEntry) entry).getTableName());
    }


    /**
     * Test parsing addColumn entry.
     */
    @Test
    void testParseAddColumn() throws ChangelogParseException {
        Map<String, Object> colMap = new HashMap<>();
        colMap.put("name", TEST_COLUMN);
        colMap.put("type", "VARCHAR(255)");

        Map<String, Object> innerMap = new HashMap<>();
        innerMap.put("tableName", TEST_TABLE);
        innerMap.put("columns", Collections.singletonList(colMap));

        Map<String, Object> entryMap = new HashMap<>();
        entryMap.put("addColumn", innerMap);

        IChangelogEntry entry = ChangelogEntryParserUtil.getInstance().parseEntry(entryMap, resolver, TEST_PATH);
        assertTrue(entry instanceof AddColumnChangelogEntry);
        assertEquals(TEST_TABLE, ((AddColumnChangelogEntry) entry).getTableName());
    }


    /**
     * Test parsing dropColumn entry.
     */
    @Test
    void testParseDropColumn() throws ChangelogParseException {
        Map<String, Object> innerMap = new HashMap<>();
        innerMap.put("tableName", TEST_TABLE);
        innerMap.put("columns", Arrays.asList(TEST_COLUMN));

        Map<String, Object> entryMap = new HashMap<>();
        entryMap.put("dropColumn", innerMap);

        IChangelogEntry entry = ChangelogEntryParserUtil.getInstance().parseEntry(entryMap, resolver, TEST_PATH);
        assertTrue(entry instanceof DropColumnChangelogEntry);
        assertEquals(1, ((DropColumnChangelogEntry) entry).getColumnNames().size());
    }


    /**
     * Test parsing createIndex entry.
     */
    @Test
    void testParseCreateIndex() throws ChangelogParseException {
        Map<String, Object> innerMap = new HashMap<>();
        innerMap.put("tableName", TEST_TABLE);
        innerMap.put("indexName", "idx_email");
        innerMap.put("columns", Arrays.asList(TEST_COLUMN));

        Map<String, Object> entryMap = new HashMap<>();
        entryMap.put("createIndex", innerMap);

        IChangelogEntry entry = ChangelogEntryParserUtil.getInstance().parseEntry(entryMap, resolver, TEST_PATH);
        assertTrue(entry instanceof CreateIndexChangelogEntry);
        assertEquals("idx_email", ((CreateIndexChangelogEntry) entry).getIndexName());
    }


    /**
     * Test parsing dropIndex entry.
     */
    @Test
    void testParseDropIndex() throws ChangelogParseException {
        Map<String, Object> innerMap = new HashMap<>();
        innerMap.put("tableName", TEST_TABLE);
        innerMap.put("indexName", "idx_email");

        Map<String, Object> entryMap = new HashMap<>();
        entryMap.put("dropIndex", innerMap);

        IChangelogEntry entry = ChangelogEntryParserUtil.getInstance().parseEntry(entryMap, resolver, TEST_PATH);
        assertTrue(entry instanceof DropIndexChangelogEntry);
    }


    /**
     * Test parsing renameTable entry.
     */
    @Test
    void testParseRenameTable() throws ChangelogParseException {
        Map<String, Object> innerMap = new HashMap<>();
        innerMap.put("oldTableName", "old_table");
        innerMap.put("newTableName", "new_table");

        Map<String, Object> entryMap = new HashMap<>();
        entryMap.put("renameTable", innerMap);

        IChangelogEntry entry = ChangelogEntryParserUtil.getInstance().parseEntry(entryMap, resolver, TEST_PATH);
        assertTrue(entry instanceof RenameTableChangelogEntry);
    }


    /**
     * Test parsing renameColumn entry.
     */
    @Test
    void testParseRenameColumn() throws ChangelogParseException {
        Map<String, Object> innerMap = new HashMap<>();
        innerMap.put("tableName", TEST_TABLE);
        innerMap.put("oldColumnName", "old_col");
        innerMap.put("newColumnName", "new_col");

        Map<String, Object> entryMap = new HashMap<>();
        entryMap.put("renameColumn", innerMap);

        IChangelogEntry entry = ChangelogEntryParserUtil.getInstance().parseEntry(entryMap, resolver, TEST_PATH);
        assertTrue(entry instanceof RenameColumnChangelogEntry);
    }


    /**
     * Test parsing modifyColumn entry.
     */
    @Test
    void testParseModifyColumn() throws ChangelogParseException {
        Map<String, Object> innerMap = new HashMap<>();
        innerMap.put("tableName", TEST_TABLE);
        innerMap.put("columnName", TEST_COLUMN);
        innerMap.put("newType", "TEXT");

        Map<String, Object> entryMap = new HashMap<>();
        entryMap.put("modifyColumn", innerMap);

        IChangelogEntry entry = ChangelogEntryParserUtil.getInstance().parseEntry(entryMap, resolver, TEST_PATH);
        assertTrue(entry instanceof ModifyColumnChangelogEntry);
    }


    /**
     * Test parsing addNotNullConstraint entry.
     */
    @Test
    void testParseAddNotNull() throws ChangelogParseException {
        Map<String, Object> innerMap = new HashMap<>();
        innerMap.put("tableName", TEST_TABLE);
        innerMap.put("columnName", TEST_COLUMN);

        Map<String, Object> entryMap = new HashMap<>();
        entryMap.put("addNotNullConstraint", innerMap);

        IChangelogEntry entry = ChangelogEntryParserUtil.getInstance().parseEntry(entryMap, resolver, TEST_PATH);
        assertTrue(entry instanceof AddNotNullConstraintChangelogEntry);
    }


    /**
     * Test parsing dropNotNullConstraint entry.
     */
    @Test
    void testParseDropNotNull() throws ChangelogParseException {
        Map<String, Object> innerMap = new HashMap<>();
        innerMap.put("tableName", TEST_TABLE);
        innerMap.put("columnName", TEST_COLUMN);

        Map<String, Object> entryMap = new HashMap<>();
        entryMap.put("dropNotNullConstraint", innerMap);

        IChangelogEntry entry = ChangelogEntryParserUtil.getInstance().parseEntry(entryMap, resolver, TEST_PATH);
        assertTrue(entry instanceof DropNotNullConstraintChangelogEntry);
    }


    /**
     * Test parsing createSequence entry.
     */
    @Test
    void testParseCreateSequence() throws ChangelogParseException {
        Map<String, Object> innerMap = new HashMap<>();
        innerMap.put("sequenceName", "user_id_seq");

        Map<String, Object> entryMap = new HashMap<>();
        entryMap.put("createSequence", innerMap);

        IChangelogEntry entry = ChangelogEntryParserUtil.getInstance().parseEntry(entryMap, resolver, TEST_PATH);
        assertTrue(entry instanceof CreateSequenceChangelogEntry);
    }


    /**
     * Test parsing dropSequence entry.
     */
    @Test
    void testParseDropSequence() throws ChangelogParseException {
        Map<String, Object> innerMap = new HashMap<>();
        innerMap.put("sequenceName", "user_id_seq");

        Map<String, Object> entryMap = new HashMap<>();
        entryMap.put("dropSequence", innerMap);

        IChangelogEntry entry = ChangelogEntryParserUtil.getInstance().parseEntry(entryMap, resolver, TEST_PATH);
        assertTrue(entry instanceof DropSequenceChangelogEntry);
    }


    /**
     * Test parsing createView entry.
     */
    @Test
    void testParseCreateView() throws ChangelogParseException {
        Map<String, Object> innerMap = new HashMap<>();
        innerMap.put("viewName", "user_summary");
        innerMap.put("selectQuery", "SELECT id, name FROM users");

        Map<String, Object> entryMap = new HashMap<>();
        entryMap.put("createView", innerMap);

        IChangelogEntry entry = ChangelogEntryParserUtil.getInstance().parseEntry(entryMap, resolver, TEST_PATH);
        assertTrue(entry instanceof CreateViewChangelogEntry);
    }


    /**
     * Test parsing dropView entry.
     */
    @Test
    void testParseDropView() throws ChangelogParseException {
        Map<String, Object> innerMap = new HashMap<>();
        innerMap.put("viewName", "user_summary");

        Map<String, Object> entryMap = new HashMap<>();
        entryMap.put("dropView", innerMap);

        IChangelogEntry entry = ChangelogEntryParserUtil.getInstance().parseEntry(entryMap, resolver, TEST_PATH);
        assertTrue(entry instanceof DropViewChangelogEntry);
    }


    /**
     * Test parsing tagDatabase entry.
     */
    @Test
    void testParseTagDatabase() throws ChangelogParseException {
        Map<String, Object> entryMap = new HashMap<>();
        entryMap.put("tagDatabase", "v1.0");

        IChangelogEntry entry = ChangelogEntryParserUtil.getInstance().parseEntry(entryMap, resolver, TEST_PATH);
        assertTrue(entry instanceof SqlChangelogEntry);
        assertTrue(((SqlChangelogEntry) entry).getSql().contains("v1.0"));
    }


    /**
     * Test unsupported entry type throws exception.
     */
    @Test
    void testUnsupportedEntryType() {
        Map<String, Object> entryMap = new HashMap<>();
        entryMap.put("unknownType", new HashMap<>());

        assertThrows(ChangelogParseException.class,
                () -> ChangelogEntryParserUtil.getInstance().parseEntry(entryMap, resolver, TEST_PATH));
    }


    /**
     * Test non-map value for DDL entry throws exception.
     */
    @Test
    void testNonMapValueThrows() {
        Map<String, Object> entryMap = new HashMap<>();
        entryMap.put("createTable", "not a map");

        assertThrows(ChangelogParseException.class,
                () -> ChangelogEntryParserUtil.getInstance().parseEntry(entryMap, resolver, TEST_PATH));
    }


    /**
     * Test createTable with column constraints.
     */
    @Test
    void testCreateTableWithConstraints() throws ChangelogParseException {
        Map<String, Object> constraints = new HashMap<>();
        constraints.put("primaryKey", "true");
        constraints.put("nullable", "false");

        Map<String, Object> colMap = new HashMap<>();
        colMap.put("name", "id");
        colMap.put("type", "BIGINT");
        colMap.put("autoIncrement", "true");
        colMap.put("constraints", constraints);

        Map<String, Object> innerMap = new HashMap<>();
        innerMap.put("tableName", TEST_TABLE);
        innerMap.put("columns", Collections.singletonList(colMap));

        Map<String, Object> entryMap = new HashMap<>();
        entryMap.put("createTable", innerMap);

        IChangelogEntry entry = ChangelogEntryParserUtil.getInstance().parseEntry(entryMap, resolver, TEST_PATH);
        assertNotNull(entry);
        assertTrue(entry instanceof CreateTableChangelogEntry);
        CreateTableChangelogEntry createTable = (CreateTableChangelogEntry) entry;
        assertEquals(1, createTable.getColumns().size());
        assertTrue(createTable.getColumns().get(0).isPrimaryKey());
    }


    /**
     * Test createTable with default value.
     */
    @Test
    void testCreateTableWithDefaultValue() throws ChangelogParseException {
        Map<String, Object> colMap = new HashMap<>();
        colMap.put("name", "status");
        colMap.put("type", "VARCHAR(20)");
        colMap.put("defaultValue", "active");

        Map<String, Object> innerMap = new HashMap<>();
        innerMap.put("tableName", TEST_TABLE);
        innerMap.put("columns", Collections.singletonList(colMap));

        Map<String, Object> entryMap = new HashMap<>();
        entryMap.put("createTable", innerMap);

        IChangelogEntry entry = ChangelogEntryParserUtil.getInstance().parseEntry(entryMap, resolver, TEST_PATH);
        CreateTableChangelogEntry createTable = (CreateTableChangelogEntry) entry;
        assertEquals("active", createTable.getColumns().get(0).getDefaultValue());
    }


    /**
     * Test createIndex with unique flag.
     */
    @Test
    void testCreateUniqueIndex() throws ChangelogParseException {
        Map<String, Object> innerMap = new HashMap<>();
        innerMap.put("tableName", TEST_TABLE);
        innerMap.put("indexName", "idx_unique_email");
        innerMap.put("unique", "true");
        innerMap.put("columns", Arrays.asList(TEST_COLUMN));

        Map<String, Object> entryMap = new HashMap<>();
        entryMap.put("createIndex", innerMap);

        IChangelogEntry entry = ChangelogEntryParserUtil.getInstance().parseEntry(entryMap, resolver, TEST_PATH);
        assertTrue(((CreateIndexChangelogEntry) entry).isUnique());
    }


    /**
     * Test dropColumn with single string column.
     */
    @Test
    void testDropColumnSingleString() throws ChangelogParseException {
        Map<String, Object> innerMap = new HashMap<>();
        innerMap.put("tableName", TEST_TABLE);
        innerMap.put("columns", TEST_COLUMN);

        Map<String, Object> entryMap = new HashMap<>();
        entryMap.put("dropColumn", innerMap);

        IChangelogEntry entry = ChangelogEntryParserUtil.getInstance().parseEntry(entryMap, resolver, TEST_PATH);
        assertTrue(entry instanceof DropColumnChangelogEntry);
        List<String> cols = ((DropColumnChangelogEntry) entry).getColumnNames();
        assertEquals(1, cols.size());
        assertEquals(TEST_COLUMN, cols.get(0));
    }


    /**
     * No-op parameter resolver that returns values as-is.
     */
    private static class NoOpParameterResolver implements IChangelogParameterResolver {

        /**
         * @see com.github.toolarium.db.changelog.impl.resolver.IChangelogParameterResolver#resolve(String)
         */
        @Override
        public String resolve(String value) {
            return value;
        }


        /**
         * @see com.github.toolarium.db.changelog.impl.resolver.IChangelogParameterResolver#setProperty(String, String)
         */
        @Override
        public void setProperty(String name, String value) {
            // no-op
        }


        /**
         * @see com.github.toolarium.db.changelog.impl.resolver.IChangelogParameterResolver#hasProperty(String)
         */
        @Override
        public boolean hasProperty(String name) {
            return false;
        }
    }
}
