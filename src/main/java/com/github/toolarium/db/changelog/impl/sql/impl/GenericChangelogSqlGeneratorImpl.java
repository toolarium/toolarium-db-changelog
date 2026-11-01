/*
 * GenericChangelogSqlGeneratorImpl.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog.impl.sql.impl;


import com.github.toolarium.db.changelog.dto.IChangelogEntry;
import com.github.toolarium.db.changelog.dto.column.AddColumnChangelogEntry;
import com.github.toolarium.db.changelog.dto.column.DropColumnChangelogEntry;
import com.github.toolarium.db.changelog.dto.column.IChangelogColumnDefinition;
import com.github.toolarium.db.changelog.dto.index.CreateIndexChangelogEntry;
import com.github.toolarium.db.changelog.dto.index.DropIndexChangelogEntry;
import com.github.toolarium.db.changelog.dto.table.CreateTableChangelogEntry;
import com.github.toolarium.db.changelog.dto.table.DropTableChangelogEntry;
import com.github.toolarium.db.changelog.impl.sql.IChangelogSqlGenerator;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Generic ANSI SQL generator. Produces standard SQL that works on most databases.
 * Database-specific generators can extend this class and override methods as needed.
 *
 * @author patrick
 */
public class GenericChangelogSqlGeneratorImpl implements IChangelogSqlGenerator {

    /**
     * @see com.github.toolarium.db.changelog.impl.sql.IChangelogSqlGenerator#generateSql(IChangelogEntry)
     */
    @Override
    public List<String> generateSql(IChangelogEntry entry) {
        if (entry instanceof CreateTableChangelogEntry) {
            return generateCreateTable((CreateTableChangelogEntry) entry);
        }
        if (entry instanceof DropTableChangelogEntry) {
            return generateDropTable((DropTableChangelogEntry) entry);
        }
        if (entry instanceof AddColumnChangelogEntry) {
            return generateAddColumn((AddColumnChangelogEntry) entry);
        }
        if (entry instanceof DropColumnChangelogEntry) {
            return generateDropColumn((DropColumnChangelogEntry) entry);
        }
        if (entry instanceof CreateIndexChangelogEntry) {
            return generateCreateIndex((CreateIndexChangelogEntry) entry);
        }
        if (entry instanceof DropIndexChangelogEntry) {
            return generateDropIndex((DropIndexChangelogEntry) entry);
        }

        return Collections.emptyList();
    }


    /**
     * @see com.github.toolarium.db.changelog.impl.sql.IChangelogSqlGenerator#supports(IChangelogEntry, Connection)
     */
    @Override
    public boolean supports(IChangelogEntry entry, Connection connection) {
        return entry instanceof CreateTableChangelogEntry
                || entry instanceof DropTableChangelogEntry
                || entry instanceof AddColumnChangelogEntry
                || entry instanceof DropColumnChangelogEntry
                || entry instanceof CreateIndexChangelogEntry
                || entry instanceof DropIndexChangelogEntry;
    }


    /**
     * Generate CREATE TABLE SQL.
     *
     * @param entry the create table entry
     * @return the SQL statements
     */
    protected List<String> generateCreateTable(CreateTableChangelogEntry entry) {
        StringBuilder sql = new StringBuilder();
        sql.append("CREATE TABLE ").append(entry.getTableName()).append(" (");

        List<String> primaryKeyColumns = new ArrayList<>();
        boolean first = true;

        for (IChangelogColumnDefinition col : entry.getColumns()) {
            if (!first) {
                sql.append(", ");
            }
            first = false;

            sql.append(col.getName()).append(" ").append(col.getType());

            if (col.isAutoIncrement()) {
                sql.append(" AUTO_INCREMENT");
            }
            if (!col.isNullable()) {
                sql.append(" NOT NULL");
            }
            if (col.isUnique()) {
                sql.append(" UNIQUE");
            }
            if (col.getDefaultValue() != null) {
                sql.append(" DEFAULT '").append(col.getDefaultValue()).append("'");
            }
            if (col.getDefaultValueComputed() != null) {
                sql.append(" DEFAULT ").append(col.getDefaultValueComputed());
            }
            if (col.isPrimaryKey()) {
                primaryKeyColumns.add(col.getName());
            }
        }

        if (!primaryKeyColumns.isEmpty()) {
            sql.append(", PRIMARY KEY (").append(String.join(", ", primaryKeyColumns)).append(")");
        }

        sql.append(")");
        return Collections.singletonList(sql.toString());
    }


    /**
     * Generate DROP TABLE SQL.
     *
     * @param entry the drop table entry
     * @return the SQL statements
     */
    protected List<String> generateDropTable(DropTableChangelogEntry entry) {
        return Collections.singletonList("DROP TABLE " + entry.getTableName());
    }


    /**
     * Generate ALTER TABLE ADD COLUMN SQL.
     *
     * @param entry the add column entry
     * @return the SQL statements
     */
    protected List<String> generateAddColumn(AddColumnChangelogEntry entry) {
        List<String> statements = new ArrayList<>();
        for (IChangelogColumnDefinition col : entry.getColumns()) {
            StringBuilder sql = new StringBuilder();
            sql.append("ALTER TABLE ").append(entry.getTableName());
            sql.append(" ADD COLUMN ").append(col.getName()).append(" ").append(col.getType());

            if (!col.isNullable()) {
                sql.append(" NOT NULL");
            }
            if (col.getDefaultValue() != null) {
                sql.append(" DEFAULT '").append(col.getDefaultValue()).append("'");
            }
            if (col.getDefaultValueComputed() != null) {
                sql.append(" DEFAULT ").append(col.getDefaultValueComputed());
            }
            statements.add(sql.toString());
        }
        return statements;
    }


    /**
     * Generate ALTER TABLE DROP COLUMN SQL.
     *
     * @param entry the drop column entry
     * @return the SQL statements
     */
    protected List<String> generateDropColumn(DropColumnChangelogEntry entry) {
        List<String> statements = new ArrayList<>();
        for (String columnName : entry.getColumnNames()) {
            statements.add("ALTER TABLE " + entry.getTableName() + " DROP COLUMN " + columnName);
        }
        return statements;
    }


    /**
     * Generate CREATE INDEX SQL.
     *
     * @param entry the create index entry
     * @return the SQL statements
     */
    protected List<String> generateCreateIndex(CreateIndexChangelogEntry entry) {
        StringBuilder sql = new StringBuilder();
        sql.append("CREATE ");
        if (entry.isUnique()) {
            sql.append("UNIQUE ");
        }
        sql.append("INDEX ").append(entry.getIndexName());
        sql.append(" ON ").append(entry.getTableName());
        sql.append(" (").append(String.join(", ", entry.getColumnNames())).append(")");
        return Collections.singletonList(sql.toString());
    }


    /**
     * Generate DROP INDEX SQL.
     *
     * @param entry the drop index entry
     * @return the SQL statements
     */
    protected List<String> generateDropIndex(DropIndexChangelogEntry entry) {
        return Collections.singletonList("DROP INDEX " + entry.getIndexName());
    }
}
