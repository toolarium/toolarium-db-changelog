/*
 * IChangelogChecksumGenerator.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog.impl.checksum;


import com.github.toolarium.db.changelog.dto.IChangelogChangeSet;

/**
 * Computes checksums for changesets.
 *
 * @author patrick
 */
public interface IChangelogChecksumGenerator {

    /**
     * Compute the checksum for a changeset.
     *
     * @param changeSet the changeset
     * @return the SHA-256 hex string
     */
    String computeChecksum(IChangelogChangeSet changeSet);
}
