/*
 * ChangelogContextImpl.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog.impl;

import com.github.toolarium.db.changelog.IChangelogContext;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * Default implementation of the changelog execution context.
 *
 * @author patrick
 */
public class ChangelogContextImpl implements IChangelogContext {
    private final List<String> activeContexts;
    private final List<String> activeLabels;


    /**
     * Constructor for ChangelogContextImpl
     *
     * @param activeContexts the active contexts for filtering
     * @param activeLabels the active labels for filtering
     */
    public ChangelogContextImpl(List<String> activeContexts, List<String> activeLabels) {
        if (activeContexts != null) {
            this.activeContexts = Collections.unmodifiableList(new ArrayList<>(activeContexts));
        } else {
            this.activeContexts = Collections.emptyList();
        }
        if (activeLabels != null) {
            this.activeLabels = Collections.unmodifiableList(new ArrayList<>(activeLabels));
        } else {
            this.activeLabels = Collections.emptyList();
        }
    }


    /**
     * @see com.github.toolarium.db.changelog.IChangelogContext#getActiveContexts()
     */
    @Override
    public List<String> getActiveContexts() {
        return activeContexts;
    }


    /**
     * @see com.github.toolarium.db.changelog.IChangelogContext#getActiveLabels()
     */
    @Override
    public List<String> getActiveLabels() {
        return activeLabels;
    }


    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return java.util.Objects.hash(activeContexts, activeLabels);
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
        ChangelogContextImpl other = (ChangelogContextImpl) obj;
        return java.util.Objects.equals(activeContexts, other.activeContexts)
                && java.util.Objects.equals(activeLabels, other.activeLabels);
    }


    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "ChangelogContext[contexts=" + activeContexts + ", labels=" + activeLabels + "]";
    }
}
