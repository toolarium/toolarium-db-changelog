/*
 * ChangelogSqlRollbackGeneratorFactory.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog.impl.rollback;


import com.github.toolarium.db.changelog.impl.rollback.impl.ChangelogSqlRollbackGeneratorImpl;


/**
 * Singleton factory for creating SQL rollback generator instances.
 *
 * @author patrick
 */
public final class ChangelogSqlRollbackGeneratorFactory {

    /**
     * Private class, the only instance of the singleton which will be created by accessing the holder class.
     *
     * @author patrick
     */
    private static final class HOLDER {
        static final ChangelogSqlRollbackGeneratorFactory INSTANCE = new ChangelogSqlRollbackGeneratorFactory();
    }


    /**
     * Private constructor
     */
    private ChangelogSqlRollbackGeneratorFactory() {
    }


    /**
     * Get the instance.
     *
     * @return the instance
     */
    public static ChangelogSqlRollbackGeneratorFactory getInstance() {
        return HOLDER.INSTANCE;
    }


    /**
     * Get the SQL rollback generator.
     *
     * @return the rollback generator instance
     */
    public IChangelogSqlRollbackGenerator getSqlRollbackGenerator() {
        return new ChangelogSqlRollbackGeneratorImpl();
    }
}
