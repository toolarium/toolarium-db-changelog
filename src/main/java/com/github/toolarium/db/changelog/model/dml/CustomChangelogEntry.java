/*
 * CustomChangelogEntry.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog.model.dml;

import com.github.toolarium.db.changelog.ICustomChangelogEntry;
import com.github.toolarium.db.changelog.model.IChangelogEntry;


/**
 * Wraps a user-provided ICustomChangelogEntry as an IChangelogEntry for use in changesets.
 *
 * @author patrick
 */
public class CustomChangelogEntry implements IChangelogEntry {
    private final String className;
    private final ICustomChangelogEntry customEntry;


    /**
     * Constructor for CustomChangelogEntry
     *
     * @param className the fully qualified class name
     * @param customEntry the custom entry instance
     */
    public CustomChangelogEntry(String className, ICustomChangelogEntry customEntry) {
        this.className = className;
        this.customEntry = customEntry;
    }


    /**
     * Get the fully qualified class name.
     *
     * @return the class name
     */
    public String getClassName() {
        return className;
    }


    /**
     * Get the custom entry instance.
     *
     * @return the custom entry
     */
    public ICustomChangelogEntry getCustomEntry() {
        return customEntry;
    }


    /**
     * @see com.github.toolarium.db.changelog.model.IChangelogEntry#getDescription()
     */
    @Override
    public String getDescription() {
        if (customEntry != null) {
            return "customChange: " + customEntry.getDescription();
        }
        return "customChange: " + className;
    }


    /**
     * @see com.github.toolarium.db.changelog.model.IChangelogEntry#supportsAutoRollback()
     */
    @Override
    public boolean supportsAutoRollback() {
        if (customEntry != null) {
            return customEntry.supportsRollback();
        }
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
        return java.util.Objects.hash(className);
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
        CustomChangelogEntry other = (CustomChangelogEntry) obj;
        return java.util.Objects.equals(className, other.className);
    }


    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return getDescription();
    }
}
