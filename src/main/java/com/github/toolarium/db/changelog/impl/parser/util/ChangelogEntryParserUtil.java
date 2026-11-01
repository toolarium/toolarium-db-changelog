/*
 * ChangelogEntryParserUtil.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog.impl.parser.util;


import com.github.toolarium.db.changelog.dto.IChangelogEntry;
import com.github.toolarium.db.changelog.dto.column.AddColumnChangelogEntry;
import com.github.toolarium.db.changelog.dto.column.ChangelogColumnDefinition;
import com.github.toolarium.db.changelog.dto.column.DropColumnChangelogEntry;
import com.github.toolarium.db.changelog.dto.column.IChangelogColumnDefinition;
import com.github.toolarium.db.changelog.dto.column.ModifyColumnChangelogEntry;
import com.github.toolarium.db.changelog.dto.column.RenameColumnChangelogEntry;
import com.github.toolarium.db.changelog.dto.constraint.AddNotNullConstraintChangelogEntry;
import com.github.toolarium.db.changelog.dto.constraint.DropNotNullConstraintChangelogEntry;
import com.github.toolarium.db.changelog.dto.index.CreateIndexChangelogEntry;
import com.github.toolarium.db.changelog.dto.index.DropIndexChangelogEntry;
import com.github.toolarium.db.changelog.dto.sequence.CreateSequenceChangelogEntry;
import com.github.toolarium.db.changelog.dto.sequence.DropSequenceChangelogEntry;
import com.github.toolarium.db.changelog.dto.sql.SqlChangelogEntry;
import com.github.toolarium.db.changelog.dto.table.CreateTableChangelogEntry;
import com.github.toolarium.db.changelog.dto.table.DropTableChangelogEntry;
import com.github.toolarium.db.changelog.dto.table.RenameTableChangelogEntry;
import com.github.toolarium.db.changelog.dto.view.CreateViewChangelogEntry;
import com.github.toolarium.db.changelog.dto.view.DropViewChangelogEntry;
import com.github.toolarium.db.changelog.exception.ChangelogParseException;
import com.github.toolarium.db.changelog.impl.resolver.IChangelogParameterResolver;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Singleton utility for parsing changelog entries from map structures (used by YAML, XML, JSON parsers).
 *
 * @author patrick
 */
public final class ChangelogEntryParserUtil {

    private static final String KEY_TABLE_NAME = "tableName";


    /**
     * Private class, the only instance of the singleton which will be created by accessing the holder class.
     *
     * @author patrick
     */
    private static final class HOLDER {
        static final ChangelogEntryParserUtil INSTANCE = new ChangelogEntryParserUtil();
    }


    /**
     * Private constructor
     */
    private ChangelogEntryParserUtil() {
    }


    /**
     * Get the instance.
     *
     * @return the instance
     */
    public static ChangelogEntryParserUtil getInstance() {
        return HOLDER.INSTANCE;
    }


    /**
     * Parse a changelog entry from a map structure.
     *
     * @param entryMap the entry map (e.g., {"createTable": {...}} or {"sql": "..."})
     * @param parameterResolver the parameter resolver
     * @param physicalPath the file path for error messages
     * @return the parsed entry
     * @throws ChangelogParseException if the entry type is unknown
     */
    @SuppressWarnings("unchecked")
    public IChangelogEntry parseEntry(Map<String, Object> entryMap, IChangelogParameterResolver parameterResolver,
                                      String physicalPath) throws ChangelogParseException {
        if (entryMap.containsKey("sql")) {
            return new SqlChangelogEntry(parameterResolver.resolve(entryMap.get("sql").toString()));
        }

        if (entryMap.containsKey("createTable")) {
            return parseCreateTable((Map<String, Object>) entryMap.get("createTable"), parameterResolver);
        }

        if (entryMap.containsKey("dropTable")) {
            return parseDropTable((Map<String, Object>) entryMap.get("dropTable"), parameterResolver);
        }

        if (entryMap.containsKey("addColumn")) {
            return parseAddColumn((Map<String, Object>) entryMap.get("addColumn"), parameterResolver);
        }

        if (entryMap.containsKey("dropColumn")) {
            return parseDropColumn((Map<String, Object>) entryMap.get("dropColumn"), parameterResolver);
        }

        if (entryMap.containsKey("createIndex")) {
            return parseCreateIndex((Map<String, Object>) entryMap.get("createIndex"), parameterResolver);
        }

        if (entryMap.containsKey("dropIndex")) {
            return parseDropIndex((Map<String, Object>) entryMap.get("dropIndex"), parameterResolver);
        }

        if (entryMap.containsKey("renameTable")) {
            return parseRenameTable((Map<String, Object>) entryMap.get("renameTable"), parameterResolver);
        }

        if (entryMap.containsKey("renameColumn")) {
            return parseRenameColumn((Map<String, Object>) entryMap.get("renameColumn"), parameterResolver);
        }

        if (entryMap.containsKey("modifyColumn")) {
            Map<String, Object> m = (Map<String, Object>) entryMap.get("modifyColumn");
            return new ModifyColumnChangelogEntry(
                    parameterResolver.resolve((String) m.get(KEY_TABLE_NAME)),
                    parameterResolver.resolve((String) m.get("columnName")),
                    parameterResolver.resolve((String) m.get("newType")));
        }

        if (entryMap.containsKey("addNotNullConstraint")) {
            Map<String, Object> m = (Map<String, Object>) entryMap.get("addNotNullConstraint");
            return new AddNotNullConstraintChangelogEntry(
                    parameterResolver.resolve((String) m.get(KEY_TABLE_NAME)),
                    parameterResolver.resolve((String) m.get("columnName")));
        }

        if (entryMap.containsKey("dropNotNullConstraint")) {
            Map<String, Object> m = (Map<String, Object>) entryMap.get("dropNotNullConstraint");
            return new DropNotNullConstraintChangelogEntry(
                    parameterResolver.resolve((String) m.get(KEY_TABLE_NAME)),
                    parameterResolver.resolve((String) m.get("columnName")));
        }

        if (entryMap.containsKey("createSequence")) {
            Map<String, Object> m = (Map<String, Object>) entryMap.get("createSequence");
            return new CreateSequenceChangelogEntry(
                    parameterResolver.resolve((String) m.get("sequenceName")));
        }

        if (entryMap.containsKey("dropSequence")) {
            Map<String, Object> m = (Map<String, Object>) entryMap.get("dropSequence");
            return new DropSequenceChangelogEntry(
                    parameterResolver.resolve((String) m.get("sequenceName")));
        }

        if (entryMap.containsKey("createView")) {
            Map<String, Object> m = (Map<String, Object>) entryMap.get("createView");
            return new CreateViewChangelogEntry(
                    parameterResolver.resolve((String) m.get("viewName")),
                    parameterResolver.resolve((String) m.get("selectQuery")));
        }

        if (entryMap.containsKey("dropView")) {
            Map<String, Object> m = (Map<String, Object>) entryMap.get("dropView");
            return new DropViewChangelogEntry(
                    parameterResolver.resolve((String) m.get("viewName")));
        }

        if (entryMap.containsKey("tagDatabase")) {
            String tag = parameterResolver.resolve(entryMap.get("tagDatabase").toString());
            return new SqlChangelogEntry("-- tag: " + tag, false, ";", false, null);
        }

        String entryType = entryMap.keySet().iterator().next();
        throw new ChangelogParseException("Unsupported change type [" + entryType + "] in [" + physicalPath + "].");
    }


    /**
     * Parse a createTable entry.
     *
     * @param map the createTable map
     * @param resolver the parameter resolver
     * @return the entry
     */
    @SuppressWarnings("unchecked")
    private IChangelogEntry parseCreateTable(Map<String, Object> map, IChangelogParameterResolver resolver) {
        String tableName = resolver.resolve((String) map.get(KEY_TABLE_NAME));
        List<Map<String, Object>> columnMaps = (List<Map<String, Object>>) map.get("columns");
        List<IChangelogColumnDefinition> columns = parseColumns(columnMaps, resolver);
        return new CreateTableChangelogEntry(tableName, columns);
    }


    /**
     * Parse a dropTable entry.
     *
     * @param map the dropTable map
     * @param resolver the parameter resolver
     * @return the entry
     */
    private IChangelogEntry parseDropTable(Map<String, Object> map, IChangelogParameterResolver resolver) {
        String tableName = resolver.resolve((String) map.get(KEY_TABLE_NAME));
        return new DropTableChangelogEntry(tableName);
    }


    /**
     * Parse an addColumn entry.
     *
     * @param map the addColumn map
     * @param resolver the parameter resolver
     * @return the entry
     */
    @SuppressWarnings("unchecked")
    private IChangelogEntry parseAddColumn(Map<String, Object> map, IChangelogParameterResolver resolver) {
        String tableName = resolver.resolve((String) map.get(KEY_TABLE_NAME));
        List<Map<String, Object>> columnMaps = (List<Map<String, Object>>) map.get("columns");
        List<IChangelogColumnDefinition> columns = parseColumns(columnMaps, resolver);
        return new AddColumnChangelogEntry(tableName, columns);
    }


    /**
     * Parse a dropColumn entry.
     *
     * @param map the dropColumn map
     * @param resolver the parameter resolver
     * @return the entry
     */
    @SuppressWarnings("unchecked")
    private IChangelogEntry parseDropColumn(Map<String, Object> map, IChangelogParameterResolver resolver) {
        String tableName = resolver.resolve((String) map.get(KEY_TABLE_NAME));
        Object columnsObj = map.get("columns");
        List<String> columnNames = new ArrayList<>();
        if (columnsObj instanceof List) {
            for (Object col : (List<Object>) columnsObj) {
                columnNames.add(resolver.resolve(col.toString()));
            }
        } else if (columnsObj instanceof String) {
            columnNames.add(resolver.resolve((String) columnsObj));
        }
        return new DropColumnChangelogEntry(tableName, columnNames);
    }


    /**
     * Parse a createIndex entry.
     *
     * @param map the createIndex map
     * @param resolver the parameter resolver
     * @return the entry
     */
    @SuppressWarnings("unchecked")
    private IChangelogEntry parseCreateIndex(Map<String, Object> map, IChangelogParameterResolver resolver) {
        String tableName = resolver.resolve((String) map.get(KEY_TABLE_NAME));
        String indexName = resolver.resolve((String) map.get("indexName"));
        boolean unique = Boolean.parseBoolean(String.valueOf(map.getOrDefault("unique", "false")));
        List<String> columnNames = new ArrayList<>();
        Object columnsObj = map.get("columns");
        if (columnsObj instanceof List) {
            for (Object col : (List<Object>) columnsObj) {
                columnNames.add(resolver.resolve(col.toString()));
            }
        }
        return new CreateIndexChangelogEntry(tableName, indexName, columnNames, unique);
    }


    /**
     * Parse a dropIndex entry.
     *
     * @param map the dropIndex map
     * @param resolver the parameter resolver
     * @return the entry
     */
    private IChangelogEntry parseDropIndex(Map<String, Object> map, IChangelogParameterResolver resolver) {
        String tableName = resolver.resolve((String) map.get(KEY_TABLE_NAME));
        String indexName = resolver.resolve((String) map.get("indexName"));
        return new DropIndexChangelogEntry(tableName, indexName);
    }


    /**
     * Parse a renameTable entry.
     *
     * @param map the renameTable map
     * @param resolver the parameter resolver
     * @return the entry
     */
    private IChangelogEntry parseRenameTable(Map<String, Object> map, IChangelogParameterResolver resolver) {
        String oldName = resolver.resolve((String) map.get("oldTableName"));
        String newName = resolver.resolve((String) map.get("newTableName"));
        return new RenameTableChangelogEntry(oldName, newName);
    }


    /**
     * Parse a renameColumn entry.
     *
     * @param map the renameColumn map
     * @param resolver the parameter resolver
     * @return the entry
     */
    private IChangelogEntry parseRenameColumn(Map<String, Object> map, IChangelogParameterResolver resolver) {
        String tableName = resolver.resolve((String) map.get(KEY_TABLE_NAME));
        String oldName = resolver.resolve((String) map.get("oldColumnName"));
        String newName = resolver.resolve((String) map.get("newColumnName"));
        return new RenameColumnChangelogEntry(tableName, oldName, newName);
    }


    /**
     * Parse a list of column definitions from maps.
     *
     * @param columnMaps the column maps
     * @param resolver the parameter resolver
     * @return the column definitions
     */
    @SuppressWarnings("unchecked")
    private List<IChangelogColumnDefinition> parseColumns(List<Map<String, Object>> columnMaps,
                                                          IChangelogParameterResolver resolver) {
        if (columnMaps == null) {
            return Collections.emptyList();
        }

        List<IChangelogColumnDefinition> columns = new ArrayList<>();
        for (Map<String, Object> colMap : columnMaps) {
            String name = resolver.resolve((String) colMap.get("name"));
            String type = resolver.resolve((String) colMap.get("type"));
            ChangelogColumnDefinition col = new ChangelogColumnDefinition(name, type);

            if (Boolean.parseBoolean(String.valueOf(colMap.getOrDefault("autoIncrement", "false")))) {
                col.setAutoIncrement(true);
            }

            Map<String, Object> constraints = (Map<String, Object>) colMap.get("constraints");
            if (constraints != null) {
                if (Boolean.parseBoolean(String.valueOf(constraints.getOrDefault("primaryKey", "false")))) {
                    col.setPrimaryKey(true);
                }
                if (!Boolean.parseBoolean(String.valueOf(constraints.getOrDefault("nullable", "true")))) {
                    col.setNullable(false);
                }
                if (Boolean.parseBoolean(String.valueOf(constraints.getOrDefault("unique", "false")))) {
                    col.setUnique(true);
                }
            }

            if (colMap.containsKey("defaultValue")) {
                col.setDefaultValue(resolver.resolve(colMap.get("defaultValue").toString()));
            }
            if (colMap.containsKey("defaultValueComputed")) {
                col.setDefaultValueComputed(resolver.resolve(colMap.get("defaultValueComputed").toString()));
            }

            columns.add(col);
        }
        return columns;
    }
}
