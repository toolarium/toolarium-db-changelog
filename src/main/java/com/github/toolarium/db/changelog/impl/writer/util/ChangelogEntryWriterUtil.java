/*
 * ChangelogEntryWriterUtil.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog.impl.writer.util;

import com.github.toolarium.db.changelog.model.IChangelogEntry;
import com.github.toolarium.db.changelog.model.column.AddColumnChangelogEntry;
import com.github.toolarium.db.changelog.model.column.DropColumnChangelogEntry;
import com.github.toolarium.db.changelog.model.column.IChangelogColumnDefinition;
import com.github.toolarium.db.changelog.model.column.ModifyColumnChangelogEntry;
import com.github.toolarium.db.changelog.model.column.RenameColumnChangelogEntry;
import com.github.toolarium.db.changelog.model.constraint.AddNotNullConstraintChangelogEntry;
import com.github.toolarium.db.changelog.model.constraint.DropNotNullConstraintChangelogEntry;
import com.github.toolarium.db.changelog.model.dml.CustomChangelogEntry;
import com.github.toolarium.db.changelog.model.index.CreateIndexChangelogEntry;
import com.github.toolarium.db.changelog.model.index.DropIndexChangelogEntry;
import com.github.toolarium.db.changelog.model.sequence.CreateSequenceChangelogEntry;
import com.github.toolarium.db.changelog.model.sequence.DropSequenceChangelogEntry;
import com.github.toolarium.db.changelog.model.sql.SqlChangelogEntry;
import com.github.toolarium.db.changelog.model.sql.SqlFileChangelogEntry;
import com.github.toolarium.db.changelog.model.table.CreateTableChangelogEntry;
import com.github.toolarium.db.changelog.model.table.DropTableChangelogEntry;
import com.github.toolarium.db.changelog.model.table.RenameTableChangelogEntry;
import com.github.toolarium.db.changelog.model.view.CreateViewChangelogEntry;
import com.github.toolarium.db.changelog.model.view.DropViewChangelogEntry;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


/**
 * Singleton utility for serializing changelog entries to map structures (used by YAML, XML, JSON writers).
 *
 * @author patrick
 */
public final class ChangelogEntryWriterUtil {

    private static final String KEY_TABLE_NAME = "tableName";
    private static final String KEY_COLUMN_NAME = "columnName";


    /**
     * Private class, the only instance of the singleton which will be created by accessing the holder class.
     *
     * @author patrick
     */
    private static final class HOLDER {
        static final ChangelogEntryWriterUtil INSTANCE = new ChangelogEntryWriterUtil();
    }


    /**
     * Private constructor
     */
    private ChangelogEntryWriterUtil() {
    }


    /**
     * Get the instance.
     *
     * @return the instance
     */
    public static ChangelogEntryWriterUtil getInstance() {
        return HOLDER.INSTANCE;
    }


    /**
     * Convert a changelog entry to a map structure suitable for serialization.
     *
     * @param entry the changelog entry
     * @return the map representation (e.g., {"sql": "..."} or {"createTable": {...}})
     */
    public Map<String, Object> toMap(IChangelogEntry entry) {
        Map<String, Object> result = new LinkedHashMap<>();

        if (entry instanceof SqlFileChangelogEntry) {
            result.put("sqlFile", toSqlFileMap((SqlFileChangelogEntry) entry));
        } else if (entry instanceof SqlChangelogEntry) {
            result.put("sql", ((SqlChangelogEntry) entry).getSql());
        } else if (entry instanceof CreateTableChangelogEntry) {
            result.put("createTable", toCreateTableMap((CreateTableChangelogEntry) entry));
        } else if (entry instanceof DropTableChangelogEntry) {
            result.put("dropTable", toDropTableMap((DropTableChangelogEntry) entry));
        } else if (entry instanceof RenameTableChangelogEntry) {
            result.put("renameTable", toRenameTableMap((RenameTableChangelogEntry) entry));
        } else if (entry instanceof AddColumnChangelogEntry) {
            result.put("addColumn", toAddColumnMap((AddColumnChangelogEntry) entry));
        } else if (entry instanceof DropColumnChangelogEntry) {
            result.put("dropColumn", toDropColumnMap((DropColumnChangelogEntry) entry));
        } else if (entry instanceof RenameColumnChangelogEntry) {
            result.put("renameColumn", toRenameColumnMap((RenameColumnChangelogEntry) entry));
        } else if (entry instanceof ModifyColumnChangelogEntry) {
            result.put("modifyColumn", toModifyColumnMap((ModifyColumnChangelogEntry) entry));
        } else if (entry instanceof CreateIndexChangelogEntry) {
            result.put("createIndex", toCreateIndexMap((CreateIndexChangelogEntry) entry));
        } else if (entry instanceof DropIndexChangelogEntry) {
            result.put("dropIndex", toDropIndexMap((DropIndexChangelogEntry) entry));
        } else if (entry instanceof AddNotNullConstraintChangelogEntry) {
            result.put("addNotNullConstraint", toAddNotNullMap((AddNotNullConstraintChangelogEntry) entry));
        } else if (entry instanceof DropNotNullConstraintChangelogEntry) {
            result.put("dropNotNullConstraint", toDropNotNullMap((DropNotNullConstraintChangelogEntry) entry));
        } else if (entry instanceof CreateSequenceChangelogEntry) {
            result.put("createSequence", toCreateSequenceMap((CreateSequenceChangelogEntry) entry));
        } else if (entry instanceof DropSequenceChangelogEntry) {
            result.put("dropSequence", toDropSequenceMap((DropSequenceChangelogEntry) entry));
        } else if (entry instanceof CreateViewChangelogEntry) {
            result.put("createView", toCreateViewMap((CreateViewChangelogEntry) entry));
        } else if (entry instanceof DropViewChangelogEntry) {
            result.put("dropView", toDropViewMap((DropViewChangelogEntry) entry));
        } else if (entry instanceof CustomChangelogEntry) {
            result.put("customChange", toCustomChangeMap((CustomChangelogEntry) entry));
        }

        return result;
    }


    /**
     * Convert a column definition to a map structure.
     *
     * @param column the column definition
     * @return the map representation
     */
    public Map<String, Object> toColumnMap(IChangelogColumnDefinition column) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("name", column.getName());
        map.put("type", column.getType());

        if (column.isAutoIncrement()) {
            map.put("autoIncrement", Boolean.TRUE);
        }

        Map<String, Object> constraints = new LinkedHashMap<>();
        if (column.isPrimaryKey()) {
            constraints.put("primaryKey", Boolean.TRUE);
        }
        if (!column.isNullable()) {
            constraints.put("nullable", Boolean.FALSE);
        }
        if (column.isUnique()) {
            constraints.put("unique", Boolean.TRUE);
        }
        if (!constraints.isEmpty()) {
            map.put("constraints", constraints);
        }

        if (column.getDefaultValue() != null) {
            map.put("defaultValue", column.getDefaultValue());
        }
        if (column.getDefaultValueComputed() != null) {
            map.put("defaultValueComputed", column.getDefaultValueComputed());
        }

        return map;
    }


    /**
     * Convert a createTable entry to a map.
     *
     * @param entry the entry
     * @return the map
     */
    private Map<String, Object> toCreateTableMap(CreateTableChangelogEntry entry) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put(KEY_TABLE_NAME, entry.getTableName());
        List<Map<String, Object>> columns = new ArrayList<>();
        for (IChangelogColumnDefinition col : entry.getColumns()) {
            columns.add(toColumnMap(col));
        }
        map.put("columns", columns);
        return map;
    }


    /**
     * Convert a dropTable entry to a map.
     *
     * @param entry the entry
     * @return the map
     */
    private Map<String, Object> toDropTableMap(DropTableChangelogEntry entry) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put(KEY_TABLE_NAME, entry.getTableName());
        return map;
    }


    /**
     * Convert a renameTable entry to a map.
     *
     * @param entry the entry
     * @return the map
     */
    private Map<String, Object> toRenameTableMap(RenameTableChangelogEntry entry) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("oldTableName", entry.getOldTableName());
        map.put("newTableName", entry.getNewTableName());
        return map;
    }


    /**
     * Convert an addColumn entry to a map.
     *
     * @param entry the entry
     * @return the map
     */
    private Map<String, Object> toAddColumnMap(AddColumnChangelogEntry entry) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put(KEY_TABLE_NAME, entry.getTableName());
        List<Map<String, Object>> columns = new ArrayList<>();
        for (IChangelogColumnDefinition col : entry.getColumns()) {
            columns.add(toColumnMap(col));
        }
        map.put("columns", columns);
        return map;
    }


    /**
     * Convert a dropColumn entry to a map.
     *
     * @param entry the entry
     * @return the map
     */
    private Map<String, Object> toDropColumnMap(DropColumnChangelogEntry entry) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put(KEY_TABLE_NAME, entry.getTableName());
        map.put("columns", entry.getColumnNames());
        return map;
    }


    /**
     * Convert a renameColumn entry to a map.
     *
     * @param entry the entry
     * @return the map
     */
    private Map<String, Object> toRenameColumnMap(RenameColumnChangelogEntry entry) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put(KEY_TABLE_NAME, entry.getTableName());
        map.put("oldColumnName", entry.getOldColumnName());
        map.put("newColumnName", entry.getNewColumnName());
        return map;
    }


    /**
     * Convert a modifyColumn entry to a map.
     *
     * @param entry the entry
     * @return the map
     */
    private Map<String, Object> toModifyColumnMap(ModifyColumnChangelogEntry entry) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put(KEY_TABLE_NAME, entry.getTableName());
        map.put(KEY_COLUMN_NAME, entry.getColumnName());
        map.put("newType", entry.getNewType());
        return map;
    }


    /**
     * Convert a createIndex entry to a map.
     *
     * @param entry the entry
     * @return the map
     */
    private Map<String, Object> toCreateIndexMap(CreateIndexChangelogEntry entry) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put(KEY_TABLE_NAME, entry.getTableName());
        map.put("indexName", entry.getIndexName());
        map.put("columns", entry.getColumnNames());
        if (entry.isUnique()) {
            map.put("unique", Boolean.TRUE);
        }
        return map;
    }


    /**
     * Convert a dropIndex entry to a map.
     *
     * @param entry the entry
     * @return the map
     */
    private Map<String, Object> toDropIndexMap(DropIndexChangelogEntry entry) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put(KEY_TABLE_NAME, entry.getTableName());
        map.put("indexName", entry.getIndexName());
        return map;
    }


    /**
     * Convert an addNotNullConstraint entry to a map.
     *
     * @param entry the entry
     * @return the map
     */
    private Map<String, Object> toAddNotNullMap(AddNotNullConstraintChangelogEntry entry) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put(KEY_TABLE_NAME, entry.getTableName());
        map.put(KEY_COLUMN_NAME, entry.getColumnName());
        return map;
    }


    /**
     * Convert a dropNotNullConstraint entry to a map.
     *
     * @param entry the entry
     * @return the map
     */
    private Map<String, Object> toDropNotNullMap(DropNotNullConstraintChangelogEntry entry) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put(KEY_TABLE_NAME, entry.getTableName());
        map.put(KEY_COLUMN_NAME, entry.getColumnName());
        return map;
    }


    /**
     * Convert a createSequence entry to a map.
     *
     * @param entry the entry
     * @return the map
     */
    private Map<String, Object> toCreateSequenceMap(CreateSequenceChangelogEntry entry) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("sequenceName", entry.getSequenceName());
        return map;
    }


    /**
     * Convert a dropSequence entry to a map.
     *
     * @param entry the entry
     * @return the map
     */
    private Map<String, Object> toDropSequenceMap(DropSequenceChangelogEntry entry) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("sequenceName", entry.getSequenceName());
        return map;
    }


    /**
     * Convert a createView entry to a map.
     *
     * @param entry the entry
     * @return the map
     */
    private Map<String, Object> toCreateViewMap(CreateViewChangelogEntry entry) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("viewName", entry.getViewName());
        map.put("selectQuery", entry.getSelectQuery());
        return map;
    }


    /**
     * Convert a dropView entry to a map.
     *
     * @param entry the entry
     * @return the map
     */
    private Map<String, Object> toDropViewMap(DropViewChangelogEntry entry) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("viewName", entry.getViewName());
        return map;
    }


    /**
     * Convert a customChange entry to a map.
     *
     * @param entry the entry
     * @return the map
     */
    private Map<String, Object> toCustomChangeMap(CustomChangelogEntry entry) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("class", entry.getClassName());
        return map;
    }


    /**
     * Convert a sqlFile entry to a map.
     *
     * @param entry the entry
     * @return the map
     */
    private Map<String, Object> toSqlFileMap(SqlFileChangelogEntry entry) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("path", entry.getPath());
        if (entry.getEncoding() != null && !"UTF-8".equals(entry.getEncoding())) {
            map.put("encoding", entry.getEncoding());
        }
        if (entry.isRelativeToChangelogFile()) {
            map.put("relativeToChangelogFile", Boolean.TRUE);
        }
        return map;
    }
}
