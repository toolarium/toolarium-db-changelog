/*
 * ChangelogTrackerImpl.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog.impl.tracking.impl;


import com.github.toolarium.db.changelog.dto.ChangelogExecutionType;
import com.github.toolarium.db.changelog.dto.ChangelogTrackingEntry;
import com.github.toolarium.db.changelog.dto.IChangelogChangeSet;
import com.github.toolarium.db.changelog.dto.IChangelogChangeSetIdentity;
import com.github.toolarium.db.changelog.dto.IChangelogEntry;
import com.github.toolarium.db.changelog.dto.IChangelogTrackingEntry;
import com.github.toolarium.db.changelog.exception.ChangelogException;
import com.github.toolarium.db.changelog.impl.tracking.IChangelogTracker;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JDBC implementation of the changelog tracker.
 *
 * @author patrick
 */
public class ChangelogTrackerImpl implements IChangelogTracker {
    private static final Logger LOG = LoggerFactory.getLogger(ChangelogTrackerImpl.class);
    private static final String BRACKET_COLON_SPACE = "]: ";
    private static final String BRACKET_DOT = "].";
    private final String tableName;
    private final String libraryVersion;

    /**
     * Constructor for ChangelogTrackerImpl
     *
     * @param tableName the tracking table name
     * @param libraryVersion the library version
     */
    public ChangelogTrackerImpl(String tableName, String libraryVersion) {
        this.tableName = tableName;
        this.libraryVersion = libraryVersion;
    }

    /**
     * @see com.github.toolarium.db.changelog.impl.tracking.IChangelogTracker#ensureTrackingTable(Connection)
     */
    @Override
    public void ensureTrackingTable(Connection connection) throws ChangelogException {
        try {
            if (tableExists(connection)) {
                return;
            }

            if (LOG.isDebugEnabled()) {
                LOG.debug("Creating tracking table [" + tableName + BRACKET_DOT);
            }
            LOG.info("Creating tracking table [" + tableName + BRACKET_DOT);

            String sql = "CREATE TABLE " + tableName + " ("
                    + "id VARCHAR(255) NOT NULL, "
                    + "author VARCHAR(255) NOT NULL, "
                    + "filepath VARCHAR(512) NOT NULL, "
                    + "checksum VARCHAR(64), "
                    + "date_executed TIMESTAMP NOT NULL, "
                    + "order_executed INT NOT NULL, "
                    + "execution_type VARCHAR(16) NOT NULL, "
                    + "description VARCHAR(1024), "
                    + "comments VARCHAR(1024), "
                    + "tag VARCHAR(255), "
                    + "contexts VARCHAR(512), "
                    + "labels VARCHAR(512), "
                    + "deployment_id VARCHAR(36), "
                    + "library_version VARCHAR(32), "
                    + "PRIMARY KEY (id, author, filepath))";

            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Failed to create tracking table [" + e.getMessage(), e);
            }
            throw new ChangelogException("Failed to create tracking table [" + tableName + BRACKET_DOT, e);
        }
    }

    /**
     * @see com.github.toolarium.db.changelog.impl.tracking.IChangelogTracker#getAppliedChangeSets(Connection)
     */
    @Override
    public List<IChangelogTrackingEntry> getAppliedChangeSets(Connection connection) throws ChangelogException {
        List<IChangelogTrackingEntry> result = new ArrayList<>();
        String sql = "SELECT id, author, filepath, checksum, date_executed, order_executed, "
                + "execution_type, description, comments, tag, deployment_id "
                + "FROM " + tableName + " ORDER BY order_executed";

        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                ChangelogTrackingEntry entry = new ChangelogTrackingEntry();
                entry.setId(rs.getString("id"));
                entry.setAuthor(rs.getString("author"));
                entry.setFilepath(rs.getString("filepath"));
                entry.setChecksum(rs.getString("checksum"));
                Timestamp ts = rs.getTimestamp("date_executed");
                if (ts != null) {
                    entry.setDateExecuted(ts.toInstant());
                }
                entry.setOrderExecuted(rs.getInt("order_executed"));
                entry.setExecutionType(ChangelogExecutionType.valueOf(rs.getString("execution_type")));
                entry.setDescription(rs.getString("description"));
                entry.setComments(rs.getString("comments"));
                entry.setTag(rs.getString("tag"));
                entry.setDeploymentId(rs.getString("deployment_id"));
                result.add(entry);
            }
        } catch (SQLException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Failed to read tracking table [" + e.getMessage(), e);
            }
            throw new ChangelogException("Failed to read tracking table [" + tableName + BRACKET_DOT, e);
        }

        return result;
    }

    /**
     * @see com.github.toolarium.db.changelog.impl.tracking.IChangelogTracker#markChangeSetExecuted(Connection, IChangelogChangeSet, String, ChangelogExecutionType, String)
     */
    @Override
    public void markChangeSetExecuted(Connection connection, IChangelogChangeSet changeSet, String checksum,
                                      ChangelogExecutionType executionType, String deploymentId) throws ChangelogException {
        int nextOrder = getNextOrderExecuted(connection);
        String description = buildDescription(changeSet);

        String sql = "INSERT INTO " + tableName
                + " (id, author, filepath, checksum, date_executed, order_executed, "
                + "execution_type, description, comments, deployment_id, library_version) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, changeSet.getIdentity().getId());
            stmt.setString(2, changeSet.getIdentity().getAuthor());
            stmt.setString(3, changeSet.getIdentity().getFilepath());
            stmt.setString(4, checksum);
            stmt.setTimestamp(5, Timestamp.from(Instant.now()));
            stmt.setInt(6, nextOrder);
            stmt.setString(7, executionType.name());
            stmt.setString(8, description);
            stmt.setString(9, changeSet.getComment());
            stmt.setString(10, deploymentId);
            stmt.setString(11, libraryVersion);
            stmt.executeUpdate();
        } catch (SQLException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Failed to record changeset [" + e.getMessage(), e);
            }
            throw new ChangelogException("Failed to record changeset [" + changeSet.getIdentity() + BRACKET_DOT, e);
        }
    }

    /**
     * @see com.github.toolarium.db.changelog.impl.tracking.IChangelogTracker#removeChangeSetEntry(Connection, IChangelogChangeSetIdentity)
     */
    @Override
    public void removeChangeSetEntry(Connection connection, IChangelogChangeSetIdentity identity) throws ChangelogException {
        String sql = "DELETE FROM " + tableName + " WHERE id = ? AND author = ? AND filepath = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, identity.getId());
            stmt.setString(2, identity.getAuthor());
            stmt.setString(3, identity.getFilepath());
            stmt.executeUpdate();
        } catch (SQLException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Failed to remove changeset entry." + e.getMessage(), e);
            }
            throw new ChangelogException("Failed to remove changeset entry.", e);
        }
    }

    /**
     * @see com.github.toolarium.db.changelog.impl.tracking.IChangelogTracker#tagLastApplied(Connection, String)
     */
    @Override
    public void tagLastApplied(Connection connection, String tagName) throws ChangelogException {
        String sql = "UPDATE " + tableName + " SET tag = ? WHERE order_executed = "
                + "(SELECT MAX(order_executed) FROM " + tableName + ")";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, tagName);
            int updated = stmt.executeUpdate();
            if (updated == 0) {
                throw new ChangelogException("No changesets found to tag.");
            }
        } catch (SQLException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Failed to tag last changeset." + e.getMessage(), e);
            }
            throw new ChangelogException("Failed to tag last changeset.", e);
        }
    }

    /**
     * @see com.github.toolarium.db.changelog.impl.tracking.IChangelogTracker#clearChecksums(Connection)
     */
    @Override
    public void clearChecksums(Connection connection) throws ChangelogException {
        String sql = "UPDATE " + tableName + " SET checksum = NULL";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            int updated = stmt.executeUpdate();
            LOG.info("Cleared checksums for " + updated + " changeset(s) in [" + tableName + BRACKET_DOT);
        } catch (SQLException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Failed to clear checksums." + e.getMessage(), e);
            }
            throw new ChangelogException("Failed to clear checksums.", e);
        }
    }

    /**
     * Check if the tracking table exists.
     *
     * @param connection the connection
     * @return true if the table exists
     * @throws SQLException on error
     */
    private boolean tableExists(Connection connection) throws SQLException {
        DatabaseMetaData meta = connection.getMetaData();
        try (ResultSet rs = meta.getTables(null, null, "%", new String[]{"TABLE"})) {
            while (rs.next()) {
                if (tableName.equalsIgnoreCase(rs.getString("TABLE_NAME"))) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Get the next order_executed value.
     *
     * @param connection the connection
     * @return the next order value
     * @throws ChangelogException on error
     */
    private int getNextOrderExecuted(Connection connection) throws ChangelogException {
        String sql = "SELECT MAX(order_executed) FROM " + tableName;
        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1) + 1;
            }
            return 1;
        } catch (SQLException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Failed to determine next order_executed." + e.getMessage(), e);
            }
            throw new ChangelogException("Failed to determine next order_executed.", e);
        }
    }

    /**
     * Build a description string from the changeset entries.
     *
     * @param changeSet the changeset
     * @return the description
     */
    private String buildDescription(IChangelogChangeSet changeSet) {
        StringBuilder sb = new StringBuilder();
        for (IChangelogEntry entry : changeSet.getEntries()) {
            if (sb.length() > 0) {
                sb.append("; ");
            }
            sb.append(entry.getDescription());
        }
        if (sb.length() > 1024) {
            return sb.substring(0, 1021) + "...";
        }
        return sb.toString();
    }
}
