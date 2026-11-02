/*
 * ChangelogChecksumGeneratorImpl.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog.impl.checksum.impl;

import com.github.toolarium.db.changelog.impl.checksum.IChangelogChecksumGenerator;
import com.github.toolarium.db.changelog.model.IChangelogChangeSet;
import com.github.toolarium.db.changelog.model.IChangelogEntry;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


/**
 * SHA-256 based checksum generator.
 * The checksum covers the change entries and their descriptions.
 * It intentionally excludes: comments, preconditions, rollback blocks, context/label attributes.
 *
 * @author patrick
 */
public class ChangelogChecksumGeneratorImpl implements IChangelogChecksumGenerator {
    private static final char[] HEX_CHARS = "0123456789abcdef".toCharArray();

    /**
     * @see com.github.toolarium.db.changelog.impl.checksum.IChangelogChecksumGenerator#computeChecksum(IChangelogChangeSet)
     */
    @Override
    public String computeChecksum(IChangelogChangeSet changeSet) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            for (IChangelogEntry entry : changeSet.getEntries()) {
                String description = entry.getDescription();
                if (description != null) {
                    digest.update(description.getBytes(StandardCharsets.UTF_8));
                }
            }

            return bytesToHex(digest.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }


    /**
     * Convert bytes to hex string.
     *
     * @param bytes the bytes
     * @return the hex string
     */
    private String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int i = 0; i < bytes.length; i++) {
            int v = bytes[i] & 0xFF;
            hexChars[i * 2] = HEX_CHARS[v >>> 4];
            hexChars[i * 2 + 1] = HEX_CHARS[v & 0x0F];
        }
        return new String(hexChars);
    }
}
