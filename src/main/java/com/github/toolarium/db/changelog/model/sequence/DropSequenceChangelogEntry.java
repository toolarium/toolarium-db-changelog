/*
 * DropSequenceChangelogEntry.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog.model.sequence;

import com.github.toolarium.db.changelog.model.IChangelogEntry;


/**
 * Drops a sequence. No auto-rollback.
 *
 * @author patrick
 */
public class DropSequenceChangelogEntry implements IChangelogEntry {
    private final String sequenceName;


    /**
     * Constructor for DropSequenceChangelogEntry
     *
     * @param sequenceName the sequenceName
     */
    public DropSequenceChangelogEntry(String sequenceName) {
        this.sequenceName = sequenceName;
    }


    /**
     * Get the sequenceName.
     *
     * @return the sequenceName
     */
    public String getSequenceName() {
        return sequenceName;
    }


    /**
     * @see com.github.toolarium.db.changelog.model.IChangelogEntry#getDescription()
     */
    @Override
    public String getDescription() {
        return "dropSequence: " + sequenceName;
    }


    /**
     * @see com.github.toolarium.db.changelog.model.IChangelogEntry#supportsAutoRollback()
     */
    @Override
    public boolean supportsAutoRollback() {
        return false;
    }


    /**
     * @see com.github.toolarium.db.changelog.model.IChangelogEntry#createAutoRollback()
     */
    @Override
    public IChangelogEntry createAutoRollback() {
        return null;
    }


    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return java.util.Objects.hash(sequenceName);
    }


    /**
     * @see java.lang.Object#equals(Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        DropSequenceChangelogEntry other = (DropSequenceChangelogEntry) obj;
        return java.util.Objects.equals(sequenceName, other.sequenceName);
    }


    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "DropSequenceChangelogEntry[" + sequenceName + "]";
    }
}
