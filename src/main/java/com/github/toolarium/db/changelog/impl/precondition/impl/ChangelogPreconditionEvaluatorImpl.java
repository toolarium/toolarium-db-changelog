/*
 * ChangelogPreconditionEvaluatorImpl.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog.impl.precondition.impl;

import com.github.toolarium.db.changelog.impl.precondition.IChangelogPreconditionEvaluator;
import com.github.toolarium.db.changelog.impl.resolver.IChangelogParameterResolver;
import com.github.toolarium.db.changelog.model.precondition.AndPrecondition;
import com.github.toolarium.db.changelog.model.precondition.ChangeLogPropertyDefinedPrecondition;
import com.github.toolarium.db.changelog.model.precondition.ColumnExistsPrecondition;
import com.github.toolarium.db.changelog.model.precondition.DbmsPrecondition;
import com.github.toolarium.db.changelog.model.precondition.IChangelogPrecondition;
import com.github.toolarium.db.changelog.model.precondition.IndexExistsPrecondition;
import com.github.toolarium.db.changelog.model.precondition.NotPrecondition;
import com.github.toolarium.db.changelog.model.precondition.OrPrecondition;
import com.github.toolarium.db.changelog.model.precondition.RowCountPrecondition;
import com.github.toolarium.db.changelog.model.precondition.SequenceExistsPrecondition;
import com.github.toolarium.db.changelog.model.precondition.SqlCheckPrecondition;
import com.github.toolarium.db.changelog.model.precondition.TableExistsPrecondition;
import com.github.toolarium.db.changelog.model.precondition.TableIsEmptyPrecondition;
import com.github.toolarium.db.changelog.model.precondition.ViewExistsPrecondition;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Evaluates preconditions against a database connection using JDBC metadata.
 *
 * @author patrick
 */
public class ChangelogPreconditionEvaluatorImpl implements IChangelogPreconditionEvaluator {
    private static final Logger LOG = LoggerFactory.getLogger(ChangelogPreconditionEvaluatorImpl.class);
    private volatile IChangelogParameterResolver parameterResolver;


    /**
     * @see com.github.toolarium.db.changelog.impl.precondition.IChangelogPreconditionEvaluator#evaluate(IChangelogPrecondition, Connection)
     */
    @Override
    public boolean evaluate(IChangelogPrecondition precondition, Connection connection) {
        try {
            return doEvaluate(precondition, connection);
        } catch (SQLException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Precondition evaluation failed [" + precondition + "]: " + e.getMessage(), e);
            }
            return false;
        }
    }


    /**
     * Set the parameter resolver for evaluating changeLogPropertyDefined preconditions.
     *
     * @param parameterResolver the parameter resolver
     */
    public void setParameterResolver(IChangelogParameterResolver parameterResolver) {
        this.parameterResolver = parameterResolver;
    }


    /**
     * Evaluate a precondition.
     *
     * @param precondition the precondition
     * @param connection the connection
     * @return true if passes
     * @throws SQLException on database error
     */
    private boolean doEvaluate(IChangelogPrecondition precondition, Connection connection) throws SQLException {
        if (precondition instanceof DbmsPrecondition) {
            return evaluateDbms((DbmsPrecondition) precondition, connection);
        }
        if (precondition instanceof TableExistsPrecondition) {
            return evaluateTableExists((TableExistsPrecondition) precondition, connection);
        }
        if (precondition instanceof ColumnExistsPrecondition) {
            return evaluateColumnExists((ColumnExistsPrecondition) precondition, connection);
        }
        if (precondition instanceof IndexExistsPrecondition) {
            return evaluateIndexExists((IndexExistsPrecondition) precondition, connection);
        }
        if (precondition instanceof ViewExistsPrecondition) {
            return evaluateViewExists((ViewExistsPrecondition) precondition, connection);
        }
        if (precondition instanceof SequenceExistsPrecondition) {
            return evaluateSequenceExists((SequenceExistsPrecondition) precondition, connection);
        }
        if (precondition instanceof SqlCheckPrecondition) {
            return evaluateSqlCheck((SqlCheckPrecondition) precondition, connection);
        }
        if (precondition instanceof TableIsEmptyPrecondition) {
            return evaluateTableIsEmpty((TableIsEmptyPrecondition) precondition, connection);
        }
        if (precondition instanceof RowCountPrecondition) {
            return evaluateRowCount((RowCountPrecondition) precondition, connection);
        }
        if (precondition instanceof ChangeLogPropertyDefinedPrecondition) {
            return evaluateChangeLogPropertyDefined((ChangeLogPropertyDefinedPrecondition) precondition);
        }
        if (precondition instanceof AndPrecondition) {
            return evaluateAnd((AndPrecondition) precondition, connection);
        }
        if (precondition instanceof OrPrecondition) {
            return evaluateOr((OrPrecondition) precondition, connection);
        }
        if (precondition instanceof NotPrecondition) {
            return !evaluate(((NotPrecondition) precondition).getPrecondition(), connection);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Unknown precondition type: " + precondition.getClass().getName());
        }
        return false;
    }


    /**
     * Evaluate dbms precondition.
     *
     * @param precondition the precondition
     * @param connection the connection
     * @return true if the database matches
     * @throws SQLException on error
     */
    private boolean evaluateDbms(DbmsPrecondition precondition, Connection connection) throws SQLException {
        String productName = connection.getMetaData().getDatabaseProductName();
        String[] dbmsTypes = precondition.getDbms().split(",");
        for (String dbms : dbmsTypes) {
            if (productName.toLowerCase().contains(dbms.trim().toLowerCase())) {
                return true;
            }
        }
        return false;
    }


    /**
     * Evaluate tableExists precondition.
     *
     * @param precondition the precondition
     * @param connection the connection
     * @return true if the table exists
     * @throws SQLException on error
     */
    private boolean evaluateTableExists(TableExistsPrecondition precondition, Connection connection) throws SQLException {
        DatabaseMetaData meta = connection.getMetaData();
        String name = precondition.getTableName();
        // Try exact case, uppercase, and lowercase to handle database-specific identifier storage
        String[] candidates = {name, name.toUpperCase(), name.toLowerCase()};
        for (String candidate : candidates) {
            try (ResultSet rs = meta.getTables(null, null, candidate, new String[]{"TABLE"})) {
                if (rs.next()) {
                    return true;
                }
            }
        }
        return false;
    }


    /**
     * Evaluate columnExists precondition.
     *
     * @param precondition the precondition
     * @param connection the connection
     * @return true if the column exists
     * @throws SQLException on error
     */
    private boolean evaluateColumnExists(ColumnExistsPrecondition precondition, Connection connection) throws SQLException {
        DatabaseMetaData meta = connection.getMetaData();
        String tableName = precondition.getTableName();
        String columnName = precondition.getColumnName();
        String[] tableCandidates = {tableName, tableName.toUpperCase(), tableName.toLowerCase()};
        String[] columnCandidates = {columnName, columnName.toUpperCase(), columnName.toLowerCase()};
        for (String tbl : tableCandidates) {
            for (String col : columnCandidates) {
                try (ResultSet rs = meta.getColumns(null, null, tbl, col)) {
                    if (rs.next()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }


    /**
     * Evaluate indexExists precondition.
     *
     * @param precondition the precondition
     * @param connection the connection
     * @return true if the index exists
     * @throws SQLException on error
     */
    private boolean evaluateIndexExists(IndexExistsPrecondition precondition, Connection connection) throws SQLException {
        // Use INFORMATION_SCHEMA query for index lookup (avoids wildcard getIndexInfo which many drivers reject)
        String sql = "SELECT 1 FROM INFORMATION_SCHEMA.INDEXES WHERE INDEX_NAME = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, precondition.getIndexName().toUpperCase());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return true;
                }
            }
        } catch (SQLException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Index lookup via INFORMATION_SCHEMA failed, trying metadata: " + e.getMessage(), e);
            }
        }

        // Fallback: try metadata with all tables via catalog scan
        DatabaseMetaData meta = connection.getMetaData();
        try (ResultSet tables = meta.getTables(null, null, "%", new String[]{"TABLE"})) {
            while (tables.next()) {
                String tbl = tables.getString("TABLE_NAME");
                try (ResultSet rs = meta.getIndexInfo(null, null, tbl, false, true)) {
                    while (rs.next()) {
                        String indexName = rs.getString("INDEX_NAME");
                        if (indexName != null && precondition.getIndexName().equalsIgnoreCase(indexName)) {
                            return true;
                        }
                    }
                } catch (SQLException ex) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Index lookup failed for table [" + tbl + "]: " + ex.getMessage(), ex);
                    }
                }
            }
        }
        return false;
    }


    /**
     * Evaluate viewExists precondition.
     *
     * @param precondition the precondition
     * @param connection the connection
     * @return true if the view exists
     * @throws SQLException on error
     */
    private boolean evaluateViewExists(ViewExistsPrecondition precondition, Connection connection) throws SQLException {
        DatabaseMetaData meta = connection.getMetaData();
        String name = precondition.getViewName();
        String[] candidates = {name, name.toUpperCase(), name.toLowerCase()};
        for (String candidate : candidates) {
            try (ResultSet rs = meta.getTables(null, null, candidate, new String[]{"VIEW"})) {
                if (rs.next()) {
                    return true;
                }
            }
        }
        return false;
    }


    /**
     * Evaluate sequenceExists precondition.
     *
     * @param precondition the precondition
     * @param connection the connection
     * @return true if the sequence exists
     * @throws SQLException on error
     */
    private boolean evaluateSequenceExists(SequenceExistsPrecondition precondition, Connection connection) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT SEQUENCE_NAME FROM INFORMATION_SCHEMA.SEQUENCES WHERE SEQUENCE_NAME = ?")) {
            stmt.setString(1, precondition.getSequenceName().toUpperCase());
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Sequence check via INFORMATION_SCHEMA failed, sequence may not exist: " + e.getMessage(), e);
            }
            return false;
        }
    }


    /**
     * Evaluate sqlCheck precondition.
     *
     * @param precondition the precondition
     * @param connection the connection
     * @return true if the SQL result matches expected
     * @throws SQLException on error
     */
    private boolean evaluateSqlCheck(SqlCheckPrecondition precondition, Connection connection) throws SQLException {
        boolean wasReadOnly = connection.isReadOnly();
        try {
            connection.setReadOnly(true);
            try (PreparedStatement stmt = connection.prepareStatement(precondition.getSql());
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String result = rs.getString(1);
                    return precondition.getExpectedResult().equals(result);
                }
            }
        } finally {
            connection.setReadOnly(wasReadOnly);
        }
        return false;
    }


    /**
     * Evaluate tableIsEmpty precondition.
     *
     * @param precondition the precondition
     * @param connection the connection
     * @return true if the table has zero rows
     * @throws SQLException on error
     */
    private boolean evaluateTableIsEmpty(TableIsEmptyPrecondition precondition, Connection connection) throws SQLException {
        String sql = "SELECT COUNT(*) FROM " + precondition.getTableName();
        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getLong(1) == 0;
            }
        }
        return false;
    }


    /**
     * Evaluate rowCount precondition.
     *
     * @param precondition the precondition
     * @param connection the connection
     * @return true if the row count matches expected
     * @throws SQLException on error
     */
    private boolean evaluateRowCount(RowCountPrecondition precondition, Connection connection) throws SQLException {
        String sql = "SELECT COUNT(*) FROM " + precondition.getTableName();
        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getLong(1) == precondition.getExpectedCount();
            }
        }
        return false;
    }


    /**
     * Evaluate changeLogPropertyDefined precondition.
     *
     * @param precondition the precondition
     * @return true if the property is defined
     */
    private boolean evaluateChangeLogPropertyDefined(ChangeLogPropertyDefinedPrecondition precondition) {
        if (parameterResolver == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("No parameter resolver set for changeLogPropertyDefined precondition.");
            }
            return false;
        }
        return parameterResolver.hasProperty(precondition.getPropertyName());
    }


    /**
     * Evaluate AND combinator.
     *
     * @param precondition the AND precondition
     * @param connection the connection
     * @return true if all preconditions pass
     */
    private boolean evaluateAnd(AndPrecondition precondition, Connection connection) {
        for (IChangelogPrecondition p : precondition.getPreconditions()) {
            if (!evaluate(p, connection)) {
                return false;
            }
        }
        return true;
    }


    /**
     * Evaluate OR combinator.
     *
     * @param precondition the OR precondition
     * @param connection the connection
     * @return true if any precondition passes
     */
    private boolean evaluateOr(OrPrecondition precondition, Connection connection) {
        for (IChangelogPrecondition p : precondition.getPreconditions()) {
            if (evaluate(p, connection)) {
                return true;
            }
        }
        return false;
    }
}
