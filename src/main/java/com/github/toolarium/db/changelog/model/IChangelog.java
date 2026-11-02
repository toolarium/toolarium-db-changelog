/*
 * IChangelog.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog.model;

import java.util.List;


/**
 * Root changelog container holding an ordered list of changesets.
 *
 * @author patrick
 */
public interface IChangelog {

    /**
     * Get the ordered list of changesets.
     *
     * @return the changesets
     */
    List<IChangelogChangeSet> getChangelogSets();

    /**
     * Get the physical file path of this changelog.
     *
     * @return the file path
     */
    String getFilepath();
}
