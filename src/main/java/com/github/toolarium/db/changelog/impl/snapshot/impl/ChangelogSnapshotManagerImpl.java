/*
 * ChangelogSnapshotManagerImpl.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog.impl.snapshot.impl;

import com.github.toolarium.db.changelog.config.ChangelogConfiguration;
import com.github.toolarium.db.changelog.exception.ChangelogException;
import com.github.toolarium.db.changelog.impl.snapshot.IChangelogSnapshotManager;
import com.github.toolarium.db.changelog.impl.sql.util.ChangelogDatabaseTypeUtil;
import com.github.toolarium.db.changelog.model.IChangelogChangeSetIdentity;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * JDBC implementation of the snapshot manager.
 * Captures table column metadata before changeset execution and stores it in DB_CHANGELOG_SNAPSHOT.
 *
 * @author patrick
 */
public class ChangelogSnapshotManagerImpl implements IChangelogSnapshotManager {
    private static final Logger LOG = LoggerFactory.getLogger(ChangelogSnapshotManagerImpl.class);
    private static final String BRACKET_DOT = "].";
    private final String tableName;


    /**
     * Constructor for ChangelogSnapshotManagerImpl
     *
     * @param tableName the snapshot table name
     */
    public ChangelogSnapshotManagerImpl(String tableName) {
        ChangelogConfiguration.validateIdentifier(tableName, "snapshotTableName");
        this.tableName = tableName;
    }


    /**
     * @see com.github.toolarium.db.changelog.impl.snapshot.IChangelogSnapshotManager#ensureSnapshotTable(Connection)
     */
    @Override
    public void ensureSnapshotTable(Connection connection) throws ChangelogException {
        try {
            if (tableExists(connection)) {
                return;
            }

            if (LOG.isDebugEnabled()) {
                LOG.debug("Creating snapshot table [" + tableName + BRACKET_DOT);
            }

            String dateTimeType = ChangelogDatabaseTypeUtil.getInstance().getDateTimeType(connection);
            String clobType = ChangelogDatabaseTypeUtil.getInstance().getClobType(connection);
            String sql = "CREATE TABLE " + tableName + " ("
                    + "changeset_id VARCHAR(255) NOT NULL, "
                    + "changeset_author VARCHAR(255) NOT NULL, "
                    + "changeset_filepath VARCHAR(512) NOT NULL, "
                    + "table_name VARCHAR(255) NOT NULL, "
                    + "snapshot_data " + clobType + " NOT NULL, "
                    + "date_captured " + dateTimeType + " NOT NULL, "
                    + "PRIMARY KEY (changeset_id, changeset_author, changeset_filepath, table_name))";

            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.executeUpdate();
            }

            LOG.info("Created snapshot table [" + tableName + BRACKET_DOT);
        } catch (SQLException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Failed to create snapshot table: " + e.getMessage(), e);
            }
            throw new ChangelogException("Failed to create snapshot table [" + tableName + BRACKET_DOT, e);
        }
    }


    /**
     * @see com.github.toolarium.db.changelog.impl.snapshot.IChangelogSnapshotManager#captureSnapshot(Connection, IChangelogChangeSetIdentity, String)
     */
    @Override
    public void captureSnapshot(Connection connection, IChangelogChangeSetIdentity identity,
                                String snapshotTableName) throws ChangelogException {
        try {
            String snapshotData = buildSnapshotData(connection, snapshotTableName);

            String sql = "INSERT INTO " + tableName
                    + " (changeset_id, changeset_author, changeset_filepath, table_name, snapshot_data, date_captured) "
                    + "VALUES (?, ?, ?, ?, ?, ?)";

            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, identity.getId());
                stmt.setString(2, identity.getAuthor());
                stmt.setString(3, identity.getFilepath());
                stmt.setString(4, snapshotTableName);
                stmt.setString(5, snapshotData);
                stmt.setTimestamp(6, Timestamp.from(Instant.now()));
                stmt.executeUpdate();
            }

            if (LOG.isDebugEnabled()) {
                LOG.debug("Captured snapshot for [" + identity + "] table [" + snapshotTableName + BRACKET_DOT);
            }
        } catch (SQLException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Failed to capture snapshot: " + e.getMessage(), e);
            }
            throw new ChangelogException("Failed to capture snapshot for [" + identity + BRACKET_DOT, e);
        }
    }


    /**
     * @see com.github.toolarium.db.changelog.impl.snapshot.IChangelogSnapshotManager#getSnapshot(Connection, IChangelogChangeSetIdentity, String)
     */
    @Override
    public String getSnapshot(Connection connection, IChangelogChangeSetIdentity identity,
                              String snapshotTableName) throws ChangelogException {
        String sql = "SELECT snapshot_data FROM " + tableName
                + " WHERE changeset_id = ? AND changeset_author = ? AND changeset_filepath = ? AND table_name = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, identity.getId());
            stmt.setString(2, identity.getAuthor());
            stmt.setString(3, identity.getFilepath());
            stmt.setString(4, snapshotTableName);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("snapshot_data");
                }
            }
        } catch (SQLException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Failed to read snapshot: " + e.getMessage(), e);
            }
            throw new ChangelogException("Failed to read snapshot for [" + identity + BRACKET_DOT, e);
        }

        return null;
    }


    /**
     * @see com.github.toolarium.db.changelog.impl.snapshot.IChangelogSnapshotManager#removeSnapshot(Connection, IChangelogChangeSetIdentity)
     */
    @Override
    public void removeSnapshot(Connection connection, IChangelogChangeSetIdentity identity) throws ChangelogException {
        String sql = "DELETE FROM " + tableName
                + " WHERE changeset_id = ? AND changeset_author = ? AND changeset_filepath = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, identity.getId());
            stmt.setString(2, identity.getAuthor());
            stmt.setString(3, identity.getFilepath());
            stmt.executeUpdate();
        } catch (SQLException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Failed to remove snapshot: " + e.getMessage(), e);
            }
            throw new ChangelogException("Failed to remove snapshot for [" + identity + BRACKET_DOT, e);
        }
    }


    /**
     * @see com.github.toolarium.db.changelog.impl.snapshot.IChangelogSnapshotManager#cleanupSnapshots(Connection, int)
     */
    @Override
    public void cleanupSnapshots(Connection connection, int retentionCount) throws ChangelogException {
        String sql = "DELETE FROM " + tableName + " WHERE date_captured < "
                + "(SELECT MIN(date_captured) FROM (SELECT date_captured FROM " + tableName
                + " ORDER BY date_captured DESC LIMIT ?))";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, retentionCount);
            int deleted = stmt.executeUpdate();
            if (deleted > 0) {
                LOG.info("Cleaned up " + deleted + " old snapshot(s).");
            }
        } catch (SQLException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Failed to cleanup snapshots: " + e.getMessage(), e);
            }
            throw new ChangelogException("Failed to cleanup snapshots.", e);
        }
    }


    /**
     * Build snapshot data from JDBC DatabaseMetaData for a table.
     * Captures column names, types, sizes, nullable status.
     *
     * @param connection the connection
     * @param targetTable the table to snapshot
     * @return the snapshot data as a formatted string
     * @throws SQLException on error
     */
    private String buildSnapshotData(Connection connection, String targetTable) throws SQLException {
        DatabaseMetaData meta = connection.getMetaData();
        StringBuilder sb = new StringBuilder();

        try (ResultSet columns = meta.getColumns(null, null, targetTable, null)) {
            while (columns.next()) {
                if (sb.length() > 0) {
                    sb.append("; ");
                }
                sb.append(columns.getString("COLUMN_NAME"));
                sb.append(" ").append(columns.getString("TYPE_NAME"));
                int size = columns.getInt("COLUMN_SIZE");
                if (size > 0) {
                    sb.append("(").append(size).append(")");
                }
                if ("NO".equals(columns.getString("IS_NULLABLE"))) {
                    sb.append(" NOT NULL");
                }
            }
        }

        // Also try uppercase for case-sensitive databases
        if (sb.length() == 0) {
            try (ResultSet columns = meta.getColumns(null, null, targetTable.toUpperCase(), null)) {
                while (columns.next()) {
                    if (sb.length() > 0) {
                        sb.append("; ");
                    }
                    sb.append(columns.getString("COLUMN_NAME"));
                    sb.append(" ").append(columns.getString("TYPE_NAME"));
                    int size = columns.getInt("COLUMN_SIZE");
                    if (size > 0) {
                        sb.append("(").append(size).append(")");
                    }
                    if ("NO".equals(columns.getString("IS_NULLABLE"))) {
                        sb.append(" NOT NULL");
                    }
                }
            }
        }

        return sb.toString();
    }


    /**
     * Check if the snapshot table exists.
     *
     * @param connection the connection
     * @return true if the table exists
     * @throws SQLException on error
     */
    private boolean tableExists(Connection connection) throws SQLException {
        DatabaseMetaData meta = connection.getMetaData();
        try (ResultSet rs = meta.getTables(null, null, tableName, new String[]{"TABLE"})) {
            if (rs.next()) {
                return true;
            }
        }
        try (ResultSet rs = meta.getTables(null, null, tableName.toUpperCase(), new String[]{"TABLE"})) {
            if (rs.next()) {
                return true;
            }
        }
        return false;
    }
}
