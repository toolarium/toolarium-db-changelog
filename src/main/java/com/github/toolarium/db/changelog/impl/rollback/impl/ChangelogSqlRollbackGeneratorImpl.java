/*
 * ChangelogSqlRollbackGeneratorImpl.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog.impl.rollback.impl;


import com.github.toolarium.db.changelog.impl.rollback.IChangelogSqlRollbackGenerator;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Parses raw SQL statements and generates rollback SQL for common DDL patterns.
 * Supports CREATE TABLE, ALTER TABLE ADD COLUMN, CREATE INDEX, CREATE VIEW,
 * CREATE SEQUENCE, RENAME TABLE, RENAME COLUMN.
 *
 * @author patrick
 */
public class ChangelogSqlRollbackGeneratorImpl implements IChangelogSqlRollbackGenerator {
    private static final Pattern CREATE_TABLE = Pattern.compile(
            "(?i)^\\s*CREATE\\s+TABLE\\s+(?:IF\\s+NOT\\s+EXISTS\\s+)?(\\S+)");
    private static final Pattern ALTER_ADD_COLUMN = Pattern.compile(
            "(?i)^\\s*ALTER\\s+TABLE\\s+(\\S+)\\s+ADD\\s+(?:COLUMN\\s+)?(\\S+)");
    private static final Pattern CREATE_INDEX = Pattern.compile(
            "(?i)^\\s*CREATE\\s+(?:UNIQUE\\s+)?INDEX\\s+(?:IF\\s+NOT\\s+EXISTS\\s+)?(\\S+)");
    private static final Pattern CREATE_VIEW = Pattern.compile(
            "(?i)^\\s*CREATE\\s+(?:OR\\s+REPLACE\\s+)?VIEW\\s+(\\S+)");
    private static final Pattern CREATE_SEQUENCE = Pattern.compile(
            "(?i)^\\s*CREATE\\s+SEQUENCE\\s+(?:IF\\s+NOT\\s+EXISTS\\s+)?(\\S+)");
    private static final Pattern RENAME_TABLE = Pattern.compile(
            "(?i)^\\s*ALTER\\s+TABLE\\s+(\\S+)\\s+RENAME\\s+TO\\s+(\\S+)");
    private static final Pattern RENAME_COLUMN = Pattern.compile(
            "(?i)^\\s*ALTER\\s+TABLE\\s+(\\S+)\\s+RENAME\\s+COLUMN\\s+(\\S+)\\s+TO\\s+(\\S+)");


    /**
     * @see com.github.toolarium.db.changelog.impl.rollback.IChangelogSqlRollbackGenerator#generateRollbackSql(String)
     */
    @Override
    public String generateRollbackSql(String sql) {
        if (sql == null) {
            return null;
        }

        String trimmed = sql.trim();
        String upper = trimmed.toUpperCase(Locale.ROOT);

        if (upper.startsWith("CREATE TABLE")) {
            return matchCreateTable(trimmed);
        }
        if (upper.startsWith("ALTER TABLE") && upper.contains("ADD")) {
            return matchAlterAddColumn(trimmed);
        }
        if (upper.startsWith("CREATE") && upper.contains("INDEX")) {
            return matchCreateIndex(trimmed);
        }
        if (upper.startsWith("CREATE") && upper.contains("VIEW")) {
            return matchCreateView(trimmed);
        }
        if (upper.startsWith("CREATE SEQUENCE")) {
            return matchCreateSequence(trimmed);
        }
        if (upper.startsWith("ALTER TABLE") && upper.contains("RENAME TO")) {
            return matchRenameTable(trimmed);
        }
        if (upper.startsWith("ALTER TABLE") && upper.contains("RENAME COLUMN")) {
            return matchRenameColumn(trimmed);
        }

        return null;
    }


    /**
     * @see com.github.toolarium.db.changelog.impl.rollback.IChangelogSqlRollbackGenerator#canGenerateRollback(String)
     */
    @Override
    public boolean canGenerateRollback(String sql) {
        return generateRollbackSql(sql) != null;
    }


    /**
     * Match CREATE TABLE and generate DROP TABLE.
     *
     * @param sql the SQL
     * @return the rollback SQL or null
     */
    private String matchCreateTable(String sql) {
        Matcher m = CREATE_TABLE.matcher(sql);
        if (m.find()) {
            return "DROP TABLE " + m.group(1);
        }
        return null;
    }


    /**
     * Match ALTER TABLE ADD COLUMN and generate DROP COLUMN.
     *
     * @param sql the SQL
     * @return the rollback SQL or null
     */
    private String matchAlterAddColumn(String sql) {
        Matcher m = ALTER_ADD_COLUMN.matcher(sql);
        if (m.find()) {
            return "ALTER TABLE " + m.group(1) + " DROP COLUMN " + m.group(2);
        }
        return null;
    }


    /**
     * Match CREATE INDEX and generate DROP INDEX.
     *
     * @param sql the SQL
     * @return the rollback SQL or null
     */
    private String matchCreateIndex(String sql) {
        Matcher m = CREATE_INDEX.matcher(sql);
        if (m.find()) {
            return "DROP INDEX " + m.group(1);
        }
        return null;
    }


    /**
     * Match CREATE VIEW and generate DROP VIEW.
     *
     * @param sql the SQL
     * @return the rollback SQL or null
     */
    private String matchCreateView(String sql) {
        Matcher m = CREATE_VIEW.matcher(sql);
        if (m.find()) {
            return "DROP VIEW " + m.group(1);
        }
        return null;
    }


    /**
     * Match CREATE SEQUENCE and generate DROP SEQUENCE.
     *
     * @param sql the SQL
     * @return the rollback SQL or null
     */
    private String matchCreateSequence(String sql) {
        Matcher m = CREATE_SEQUENCE.matcher(sql);
        if (m.find()) {
            return "DROP SEQUENCE " + m.group(1);
        }
        return null;
    }


    /**
     * Match ALTER TABLE RENAME TO and generate reverse rename.
     *
     * @param sql the SQL
     * @return the rollback SQL or null
     */
    private String matchRenameTable(String sql) {
        Matcher m = RENAME_TABLE.matcher(sql);
        if (m.find()) {
            return "ALTER TABLE " + m.group(2) + " RENAME TO " + m.group(1);
        }
        return null;
    }


    /**
     * Match ALTER TABLE RENAME COLUMN and generate reverse rename.
     *
     * @param sql the SQL
     * @return the rollback SQL or null
     */
    private String matchRenameColumn(String sql) {
        Matcher m = RENAME_COLUMN.matcher(sql);
        if (m.find()) {
            return "ALTER TABLE " + m.group(1) + " RENAME COLUMN " + m.group(3) + " TO " + m.group(2);
        }
        return null;
    }
}
