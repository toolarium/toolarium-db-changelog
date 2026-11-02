/*
 * ChangelogExecutorFactory.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog.impl.executor;

import com.github.toolarium.db.changelog.IChangelogChecksumGenerator;
import com.github.toolarium.db.changelog.IChangelogExecutor;
import com.github.toolarium.db.changelog.IChangelogTracker;
import com.github.toolarium.db.changelog.config.ChangelogRollbackPolicy;
import com.github.toolarium.db.changelog.impl.executor.impl.ChangelogExecutorImpl;
import com.github.toolarium.db.changelog.impl.sql.ChangelogSqlGeneratorFactory;


/**
 * Singleton factory for creating changelog executor instances.
 * The returned executor is not thread-safe; each caller must pass a dedicated
 * {@link java.sql.Connection} per thread. The changelog runner serializes
 * access via the changelog lock.
 *
 * @author patrick
 */
public final class ChangelogExecutorFactory {

    /**
     * Private class, the only instance of the singleton which will be created by accessing the holder class.
     *
     * @author patrick
     */
    private static final class HOLDER {
        static final ChangelogExecutorFactory INSTANCE = new ChangelogExecutorFactory();
    }


    /**
     * Private constructor
     */
    private ChangelogExecutorFactory() {
    }


    /**
     * Get the instance.
     *
     * @return the instance
     */
    public static ChangelogExecutorFactory getInstance() {
        return HOLDER.INSTANCE;
    }


    /**
     * Get a changelog executor with default rollback policy (WARN).
     *
     * @param tracker the tracking table manager
     * @param checksumGenerator the checksum generator
     * @return the executor instance
     */
    public IChangelogExecutor getExecutor(IChangelogTracker tracker, IChangelogChecksumGenerator checksumGenerator) {
        return getExecutor(tracker, checksumGenerator, ChangelogRollbackPolicy.WARN);
    }


    /**
     * Get a changelog executor with the specified rollback policy.
     *
     * @param tracker the tracking table manager
     * @param checksumGenerator the checksum generator
     * @param rollbackPolicy the rollback policy for missing rollback definitions
     * @return the executor instance
     */
    public IChangelogExecutor getExecutor(IChangelogTracker tracker, IChangelogChecksumGenerator checksumGenerator,
                                          ChangelogRollbackPolicy rollbackPolicy) {
        return new ChangelogExecutorImpl(tracker, checksumGenerator,
                ChangelogSqlGeneratorFactory.getInstance().getSqlGenerator(), rollbackPolicy);
    }
}
