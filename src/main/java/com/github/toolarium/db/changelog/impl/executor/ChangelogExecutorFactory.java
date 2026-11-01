/*
 * ChangelogExecutorFactory.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog.impl.executor;


import com.github.toolarium.db.changelog.impl.checksum.IChangelogChecksumGenerator;
import com.github.toolarium.db.changelog.impl.executor.impl.ChangelogExecutorImpl;
import com.github.toolarium.db.changelog.impl.tracking.IChangelogTracker;


/**
 * Singleton factory for creating changelog executor instances.
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
     * Create a changelog executor.
     *
     * @param tracker the tracking table manager
     * @param checksumGenerator the checksum generator
     * @return the executor instance
     */
    public IChangelogExecutor getExecutor(IChangelogTracker tracker, IChangelogChecksumGenerator checksumGenerator) {
        return new ChangelogExecutorImpl(tracker, checksumGenerator);
    }
}
