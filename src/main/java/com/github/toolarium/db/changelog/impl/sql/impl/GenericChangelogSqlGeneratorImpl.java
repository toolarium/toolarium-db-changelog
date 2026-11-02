/*
 * GenericChangelogSqlGeneratorImpl.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog.impl.sql.impl;

import com.github.toolarium.db.changelog.config.ChangelogObjectQuotingStrategy;
import com.github.toolarium.db.changelog.impl.sql.IChangelogSqlGenerator;
import com.github.toolarium.db.changelog.model.IChangelogEntry;
import com.github.toolarium.db.changelog.model.column.AddColumnChangelogEntry;
import com.github.toolarium.db.changelog.model.column.DropColumnChangelogEntry;
import com.github.toolarium.db.changelog.model.column.IChangelogColumnDefinition;
import com.github.toolarium.db.changelog.model.column.ModifyColumnChangelogEntry;
import com.github.toolarium.db.changelog.model.column.RenameColumnChangelogEntry;
import com.github.toolarium.db.changelog.model.constraint.AddNotNullConstraintChangelogEntry;
import com.github.toolarium.db.changelog.model.constraint.DropNotNullConstraintChangelogEntry;
import com.github.toolarium.db.changelog.model.index.CreateIndexChangelogEntry;
import com.github.toolarium.db.changelog.model.index.DropIndexChangelogEntry;
import com.github.toolarium.db.changelog.model.sequence.CreateSequenceChangelogEntry;
import com.github.toolarium.db.changelog.model.sequence.DropSequenceChangelogEntry;
import com.github.toolarium.db.changelog.model.table.CreateTableChangelogEntry;
import com.github.toolarium.db.changelog.model.table.DropTableChangelogEntry;
import com.github.toolarium.db.changelog.model.table.RenameTableChangelogEntry;
import com.github.toolarium.db.changelog.model.view.CreateViewChangelogEntry;
import com.github.toolarium.db.changelog.model.view.DropViewChangelogEntry;
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
    /** ALTER TABLE prefix constant. */
    protected static final String ALTER_TABLE = "ALTER TABLE ";
    private ChangelogObjectQuotingStrategy quotingStrategy = ChangelogObjectQuotingStrategy.NONE;

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
        if (entry instanceof RenameTableChangelogEntry) {
            return generateRenameTable((RenameTableChangelogEntry) entry);
        }
        if (entry instanceof RenameColumnChangelogEntry) {
            return generateRenameColumn((RenameColumnChangelogEntry) entry);
        }
        if (entry instanceof ModifyColumnChangelogEntry) {
            return generateModifyColumn((ModifyColumnChangelogEntry) entry);
        }
        if (entry instanceof AddNotNullConstraintChangelogEntry) {
            return generateAddNotNull((AddNotNullConstraintChangelogEntry) entry);
        }
        if (entry instanceof DropNotNullConstraintChangelogEntry) {
            return generateDropNotNull((DropNotNullConstraintChangelogEntry) entry);
        }
        if (entry instanceof CreateSequenceChangelogEntry) {
            return generateCreateSequence((CreateSequenceChangelogEntry) entry);
        }
        if (entry instanceof DropSequenceChangelogEntry) {
            return generateDropSequence((DropSequenceChangelogEntry) entry);
        }
        if (entry instanceof CreateViewChangelogEntry) {
            return generateCreateView((CreateViewChangelogEntry) entry);
        }
        if (entry instanceof DropViewChangelogEntry) {
            return generateDropView((DropViewChangelogEntry) entry);
        }

        throw new IllegalArgumentException("Unsupported entry type for SQL generation: " + entry.getClass().getName());
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
                || entry instanceof DropIndexChangelogEntry
                || entry instanceof RenameTableChangelogEntry
                || entry instanceof RenameColumnChangelogEntry
                || entry instanceof ModifyColumnChangelogEntry
                || entry instanceof AddNotNullConstraintChangelogEntry
                || entry instanceof DropNotNullConstraintChangelogEntry
                || entry instanceof CreateSequenceChangelogEntry
                || entry instanceof DropSequenceChangelogEntry
                || entry instanceof CreateViewChangelogEntry
                || entry instanceof DropViewChangelogEntry;
    }


    /**
     * Generate CREATE TABLE SQL.
     *
     * @param entry the create table entry
     * @return the SQL statements
     */
    protected List<String> generateCreateTable(CreateTableChangelogEntry entry) {
        requireNonNull(entry.getTableName(), "tableName");
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
                sql.append(" DEFAULT '").append(escapeStringLiteral(col.getDefaultValue())).append("'");
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
        requireNonNull(entry.getTableName(), "tableName");
        return Collections.singletonList("DROP TABLE " + entry.getTableName());
    }


    /**
     * Generate ALTER TABLE ADD COLUMN SQL.
     *
     * @param entry the add column entry
     * @return the SQL statements
     */
    protected List<String> generateAddColumn(AddColumnChangelogEntry entry) {
        requireNonNull(entry.getTableName(), "tableName");
        List<String> statements = new ArrayList<>();
        for (IChangelogColumnDefinition col : entry.getColumns()) {
            StringBuilder sql = new StringBuilder();
            sql.append(ALTER_TABLE).append(entry.getTableName());
            sql.append(" ADD COLUMN ").append(col.getName()).append(" ").append(col.getType());

            if (!col.isNullable()) {
                sql.append(" NOT NULL");
            }
            if (col.getDefaultValue() != null) {
                sql.append(" DEFAULT '").append(escapeStringLiteral(col.getDefaultValue())).append("'");
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
            statements.add(ALTER_TABLE + entry.getTableName() + " DROP COLUMN " + columnName);
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


    /**
     * Generate ALTER TABLE RENAME SQL.
     *
     * @param entry the rename table entry
     * @return the SQL statements
     */
    protected List<String> generateRenameTable(RenameTableChangelogEntry entry) {
        return Collections.singletonList(
                ALTER_TABLE + entry.getOldTableName() + " RENAME TO " + entry.getNewTableName());
    }


    /**
     * Generate ALTER TABLE RENAME COLUMN SQL.
     *
     * @param entry the rename column entry
     * @return the SQL statements
     */
    protected List<String> generateRenameColumn(RenameColumnChangelogEntry entry) {
        return Collections.singletonList(
                ALTER_TABLE + entry.getTableName() + " RENAME COLUMN "
                        + entry.getOldColumnName() + " TO " + entry.getNewColumnName());
    }


    /**
     * Generate ALTER TABLE ALTER COLUMN SQL.
     *
     * @param entry the modify column entry
     * @return the SQL statements
     */
    protected List<String> generateModifyColumn(ModifyColumnChangelogEntry entry) {
        return Collections.singletonList(
                ALTER_TABLE + entry.getTableName() + " ALTER COLUMN "
                        + entry.getColumnName() + " " + entry.getNewType());
    }


    /**
     * Generate ALTER TABLE ALTER COLUMN SET NOT NULL SQL.
     *
     * @param entry the add not null entry
     * @return the SQL statements
     */
    protected List<String> generateAddNotNull(AddNotNullConstraintChangelogEntry entry) {
        return Collections.singletonList(
                ALTER_TABLE + entry.getTableName() + " ALTER COLUMN "
                        + entry.getColumnName() + " SET NOT NULL");
    }


    /**
     * Generate ALTER TABLE ALTER COLUMN DROP NOT NULL SQL.
     *
     * @param entry the drop not null entry
     * @return the SQL statements
     */
    protected List<String> generateDropNotNull(DropNotNullConstraintChangelogEntry entry) {
        return Collections.singletonList(
                ALTER_TABLE + entry.getTableName() + " ALTER COLUMN "
                        + entry.getColumnName() + " DROP NOT NULL");
    }


    /**
     * Generate CREATE SEQUENCE SQL.
     *
     * @param entry the create sequence entry
     * @return the SQL statements
     */
    protected List<String> generateCreateSequence(CreateSequenceChangelogEntry entry) {
        return Collections.singletonList("CREATE SEQUENCE " + entry.getSequenceName());
    }


    /**
     * Generate DROP SEQUENCE SQL.
     *
     * @param entry the drop sequence entry
     * @return the SQL statements
     */
    protected List<String> generateDropSequence(DropSequenceChangelogEntry entry) {
        return Collections.singletonList("DROP SEQUENCE " + entry.getSequenceName());
    }


    /**
     * Generate CREATE VIEW SQL.
     *
     * @param entry the create view entry
     * @return the SQL statements
     */
    protected List<String> generateCreateView(CreateViewChangelogEntry entry) {
        return Collections.singletonList(
                "CREATE OR REPLACE VIEW " + entry.getViewName() + " AS " + entry.getSelectQuery());
    }


    /**
     * Generate DROP VIEW SQL.
     *
     * @param entry the drop view entry
     * @return the SQL statements
     */
    protected List<String> generateDropView(DropViewChangelogEntry entry) {
        return Collections.singletonList("DROP VIEW " + entry.getViewName());
    }


    /**
     * Validate that a required value is not null.
     *
     * @param value the value to check
     * @param name the name for error messages
     * @throws IllegalArgumentException if the value is null
     */
    protected void requireNonNull(Object value, String name) {
        if (value == null) {
            throw new IllegalArgumentException("Required attribute [" + name + "] must not be null.");
        }
    }


    /**
     * Escape a string literal for safe inclusion in SQL by doubling single quotes.
     *
     * @param value the string value
     * @return the escaped value
     */
    protected String escapeStringLiteral(String value) {
        return value.replace("'", "''");
    }


    /**
     * Quote a SQL identifier according to the configured quoting strategy.
     *
     * @param identifier the identifier (table name, column name, etc.)
     * @return the optionally quoted identifier
     */
    protected String quoteIdentifier(String identifier) {
        if (quotingStrategy == ChangelogObjectQuotingStrategy.QUOTE_ALL) {
            return "\"" + identifier.replace("\"", "\"\"") + "\"";
        }
        return identifier;
    }


    /**
     * Set the object quoting strategy.
     *
     * @param quotingStrategy the quoting strategy
     */
    public void setObjectQuotingStrategy(ChangelogObjectQuotingStrategy quotingStrategy) {
        this.quotingStrategy = quotingStrategy;
    }
}
