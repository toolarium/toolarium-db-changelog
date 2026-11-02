/*
 * ChangelogDatabaseTypeUtil.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog.impl.sql.util;

import java.sql.Connection;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Singleton utility for resolving database-specific SQL type names.
 * Detects the database product via JDBC metadata and returns the appropriate type.
 *
 * @author patrick
 */
public final class ChangelogDatabaseTypeUtil {
    private static final Logger LOG = LoggerFactory.getLogger(ChangelogDatabaseTypeUtil.class);


    /**
     * Private class, the only instance of the singleton which will be created by accessing the holder class.
     *
     * @author patrick
     */
    private static final class HOLDER {
        static final ChangelogDatabaseTypeUtil INSTANCE = new ChangelogDatabaseTypeUtil();
    }


    /**
     * Private constructor
     */
    private ChangelogDatabaseTypeUtil() {
    }


    /**
     * Get the instance.
     *
     * @return the instance
     */
    public static ChangelogDatabaseTypeUtil getInstance() {
        return HOLDER.INSTANCE;
    }


    /**
     * Get the appropriate datetime column type for the database.
     * SQL Server uses TIMESTAMP as a binary rowversion type, so DATETIME is used instead.
     * All other databases use TIMESTAMP.
     *
     * @param connection the database connection
     * @return the datetime type string
     */
    public String getDateTimeType(Connection connection) {
        String dbName = getDatabaseProductName(connection);
        if (dbName.contains("microsoft")) {
            return "DATETIME";
        } else if (dbName.contains("sql server")) {
            return "DATETIME";
        }

        return "TIMESTAMP";
    }


    /**
     * Get the appropriate large text column type for the database.
     * MySQL and MariaDB use TEXT instead of CLOB.
     *
     * @param connection the database connection
     * @return the large text type string
     */
    public String getClobType(Connection connection) {
        String dbName = getDatabaseProductName(connection);
        if (dbName.contains("mysql")) {
            return "TEXT";
        } else if (dbName.contains("mariadb")) {
            return "TEXT";
        }
        
        return "CLOB";
    }


    /**
     * Get the database product name in lowercase.
     *
     * @param connection the database connection
     * @return the product name in lowercase, or empty string on error
     */
    private String getDatabaseProductName(Connection connection) {
        try {
            return connection.getMetaData().getDatabaseProductName().toLowerCase();
        } catch (SQLException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Failed to detect database product name: " + e.getMessage(), e);
            }
            return "";
        }
    }
}
