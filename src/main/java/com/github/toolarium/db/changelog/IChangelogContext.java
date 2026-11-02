/*
 * IChangelogContext.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog;

import java.util.List;


/**
 * Holds the runtime execution context for changelog operations,
 * such as active contexts and labels for filtering changesets.
 *
 * @author patrick
 */
public interface IChangelogContext {

    /**
     * Get the active contexts for filtering changesets.
     *
     * @return the active contexts, never null
     */
    List<String> getActiveContexts();


    /**
     * Get the active labels for filtering changesets.
     *
     * @return the active labels, never null
     */
    List<String> getActiveLabels();
}
