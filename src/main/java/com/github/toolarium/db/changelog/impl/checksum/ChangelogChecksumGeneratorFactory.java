/*
 * ChangelogChecksumGeneratorFactory.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog.impl.checksum;

import com.github.toolarium.db.changelog.IChangelogChecksumGenerator;
import com.github.toolarium.db.changelog.impl.checksum.impl.ChangelogChecksumGeneratorImpl;


/**
 * Singleton factory for creating changelog checksum generator instances.
 *
 * @author patrick
 */
public final class ChangelogChecksumGeneratorFactory {

    /**
     * Private class, the only instance of the singleton which will be created by accessing the holder class.
     *
     * @author patrick
     */
    private static final class HOLDER {
        static final ChangelogChecksumGeneratorFactory INSTANCE = new ChangelogChecksumGeneratorFactory();
    }


    /**
     * Private constructor
     */
    private ChangelogChecksumGeneratorFactory() {
    }


    /**
     * Get the instance.
     *
     * @return the instance
     */
    public static ChangelogChecksumGeneratorFactory getInstance() {
        return HOLDER.INSTANCE;
    }


    /**
     * Create a SHA-256 based checksum generator.
     *
     * @return the checksum generator instance
     */
    public IChangelogChecksumGenerator getChecksumGenerator() {
        return new ChangelogChecksumGeneratorImpl();
    }
}
