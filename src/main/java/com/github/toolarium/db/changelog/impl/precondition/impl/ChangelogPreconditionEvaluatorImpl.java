/*
 * ChangelogPreconditionEvaluatorImpl.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog.impl.precondition.impl;

import com.github.toolarium.db.changelog.impl.precondition.IChangelogPreconditionEvaluator;
import com.github.toolarium.db.changelog.model.precondition.AndPrecondition;
import com.github.toolarium.db.changelog.model.precondition.ColumnExistsPrecondition;
import com.github.toolarium.db.changelog.model.precondition.DbmsPrecondition;
import com.github.toolarium.db.changelog.model.precondition.IChangelogPrecondition;
import com.github.toolarium.db.changelog.model.precondition.IndexExistsPrecondition;
import com.github.toolarium.db.changelog.model.precondition.NotPrecondition;
import com.github.toolarium.db.changelog.model.precondition.OrPrecondition;
import com.github.toolarium.db.changelog.model.precondition.SequenceExistsPrecondition;
import com.github.toolarium.db.changelog.model.precondition.SqlCheckPrecondition;
import com.github.toolarium.db.changelog.model.precondition.TableExistsPrecondition;
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
        try (ResultSet rs = meta.getTables(null, null, "%", new String[]{"TABLE"})) {
            while (rs.next()) {
                if (precondition.getTableName().equalsIgnoreCase(rs.getString("TABLE_NAME"))) {
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
        try (ResultSet rs = meta.getColumns(null, null, "%", "%")) {
            while (rs.next()) {
                if (precondition.getTableName().equalsIgnoreCase(rs.getString("TABLE_NAME"))
                        && precondition.getColumnName().equalsIgnoreCase(rs.getString("COLUMN_NAME"))) {
                    return true;
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
        DatabaseMetaData meta = connection.getMetaData();
        try (ResultSet rs = meta.getIndexInfo(null, null, "%", false, true)) {
            while (rs.next()) {
                String indexName = rs.getString("INDEX_NAME");
                if (indexName != null && precondition.getIndexName().equalsIgnoreCase(indexName)) {
                    return true;
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
        try (ResultSet rs = meta.getTables(null, null, "%", new String[]{"VIEW"})) {
            while (rs.next()) {
                if (precondition.getViewName().equalsIgnoreCase(rs.getString("TABLE_NAME"))) {
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
        try (PreparedStatement stmt = connection.prepareStatement(precondition.getSql());
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                String result = rs.getString(1);
                return precondition.getExpectedResult().equals(result);
            }
        }
        return false;
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
