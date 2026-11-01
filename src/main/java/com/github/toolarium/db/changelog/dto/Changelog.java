/*
 * Changelog.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog.dto;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Default implementation of a changelog.
 *
 * @author patrick
 */
public class Changelog implements IChangelog {
    private final String filepath;
    private final List<IChangelogChangeSet> changeSets;

    /**
     * Constructor for Changelog
     *
     * @param filepath the physical file path
     * @param changeSets the ordered list of changesets
     */
    public Changelog(String filepath, List<IChangelogChangeSet> changeSets) {
        this.filepath = filepath;
        this.changeSets = Collections.unmodifiableList(new ArrayList<>(changeSets));
    }

    /**
     * @see com.github.toolarium.db.changelog.dto.IChangelog#getChangelogSets()
     */
    @Override
    public List<IChangelogChangeSet> getChangelogSets() {
        return changeSets;
    }

    /**
     * @see com.github.toolarium.db.changelog.dto.IChangelog#getFilepath()
     */
    @Override
    public String getFilepath() {
        return filepath;
    }


    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return java.util.Objects.hash(filepath);
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
        return java.util.Objects.equals(filepath, ((Changelog) obj).filepath);
    }


    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "Changelog[" + filepath + ", " + changeSets.size() + " changeset(s)]";
    }
}
