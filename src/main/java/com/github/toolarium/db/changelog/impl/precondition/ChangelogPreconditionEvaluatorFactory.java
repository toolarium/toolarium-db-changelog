/*
 * ChangelogPreconditionEvaluatorFactory.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog.impl.precondition;

import com.github.toolarium.db.changelog.impl.precondition.impl.ChangelogPreconditionEvaluatorImpl;


/**
 * Singleton factory for creating precondition evaluator instances.
 *
 * @author patrick
 */
public final class ChangelogPreconditionEvaluatorFactory {

    /**
     * Private class, the only instance of the singleton which will be created by accessing the holder class.
     *
     * @author patrick
     */
    private static final class HOLDER {
        static final ChangelogPreconditionEvaluatorFactory INSTANCE = new ChangelogPreconditionEvaluatorFactory();
    }


    /**
     * Private constructor
     */
    private ChangelogPreconditionEvaluatorFactory() {
    }


    /**
     * Get the instance.
     *
     * @return the instance
     */
    public static ChangelogPreconditionEvaluatorFactory getInstance() {
        return HOLDER.INSTANCE;
    }


    /**
     * Get a precondition evaluator.
     *
     * @return the evaluator instance
     */
    public IChangelogPreconditionEvaluator getPreconditionEvaluator() {
        return new ChangelogPreconditionEvaluatorImpl();
    }
}
