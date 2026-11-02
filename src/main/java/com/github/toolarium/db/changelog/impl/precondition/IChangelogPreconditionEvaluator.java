/*
 * IChangelogPreconditionEvaluator.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog.impl.precondition;

import com.github.toolarium.db.changelog.model.precondition.IChangelogPrecondition;
import java.sql.Connection;


/**
 * Evaluates preconditions against a database connection.
 *
 * @author patrick
 */
public interface IChangelogPreconditionEvaluator {

    /**
     * Evaluate a precondition.
     *
     * @param precondition the precondition to evaluate
     * @param connection the database connection
     * @return true if the precondition passes
     */
    boolean evaluate(IChangelogPrecondition precondition, Connection connection);
}
