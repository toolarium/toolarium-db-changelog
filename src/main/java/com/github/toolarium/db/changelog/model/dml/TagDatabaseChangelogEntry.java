/*
 * TagDatabaseChangelogEntry.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog.model.dml;

import com.github.toolarium.db.changelog.model.IChangelogEntry;


/**
 * Tags the current database state for rollback.
 *
 * @author patrick
 */
public class TagDatabaseChangelogEntry implements IChangelogEntry {
    private final String tag;


    /**
     * Constructor for TagDatabaseChangelogEntry
     *
     * @param tag the tag
     */
    public TagDatabaseChangelogEntry(String tag) {
        this.tag = tag;
    }


    /**
     * Get the tag.
     *
     * @return the tag
     */
    public String getTag() {
        return tag;
    }


    /**
     * @see com.github.toolarium.db.changelog.model.IChangelogEntry#getDescription()
     */
    @Override
    public String getDescription() {
        return "tagDatabase: " + tag;
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
        return java.util.Objects.hash(tag);
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
        TagDatabaseChangelogEntry other = (TagDatabaseChangelogEntry) obj;
        return java.util.Objects.equals(tag, other.tag);
    }


    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return getDescription();
    }
}
