/*
 * ChangelogSqlGeneratorFactory.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog.impl.sql;

import com.github.toolarium.db.changelog.impl.sql.impl.GenericChangelogSqlGeneratorImpl;


/**
 * Singleton factory for creating changelog SQL generator instances.
 *
 * @author patrick
 */
public final class ChangelogSqlGeneratorFactory {

    /**
     * Private class, the only instance of the singleton which will be created by accessing the holder class.
     *
     * @author patrick
     */
    private static final class HOLDER {
        static final ChangelogSqlGeneratorFactory INSTANCE = new ChangelogSqlGeneratorFactory();
    }


    /**
     * Private constructor
     */
    private ChangelogSqlGeneratorFactory() {
    }


    /**
     * Get the instance.
     *
     * @return the instance
     */
    public static ChangelogSqlGeneratorFactory getInstance() {
        return HOLDER.INSTANCE;
    }


    /**
     * Get the generic ANSI SQL generator.
     *
     * @return the SQL generator instance
     */
    public IChangelogSqlGenerator getSqlGenerator() {
        return new GenericChangelogSqlGeneratorImpl();
    }
}
