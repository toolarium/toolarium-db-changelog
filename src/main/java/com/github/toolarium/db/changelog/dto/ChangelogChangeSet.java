/*
 * ChangelogChangeSet.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog.dto;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Default implementation of a changeset.
 *
 * @author patrick
 */
public class ChangelogChangeSet implements IChangelogChangeSet {
    private final IChangelogChangeSetIdentity identity;
    private final List<IChangelogEntry> entries;
    private final List<IChangelogEntry> rollbackEntries;
    private final String comment;
    private final boolean runAlways;
    private final boolean runOnChange;
    private final boolean failOnError;
    private final boolean runInTransaction;
    private final List<String> contexts;
    private final List<String> labels;

    /**
     * Constructor for ChangelogChangeSet
     *
     * @param identity the changeset identity
     * @param entries the changelog entries
     */
    public ChangelogChangeSet(IChangelogChangeSetIdentity identity, List<IChangelogEntry> entries) {
        this(identity, entries, Collections.emptyList(), null, false, false, true, true, Collections.emptyList(), Collections.emptyList());
    }

    /**
     * Constructor for ChangelogChangeSet
     *
     * @param identity the changeset identity
     * @param entries the changelog entries
     * @param rollbackEntries the rollback entries
     * @param comment the comment
     * @param runAlways whether to run always
     * @param runOnChange whether to run on change
     * @param failOnError whether to fail on error
     * @param runInTransaction whether to run in transaction
     * @param contexts the contexts
     * @param labels the labels
     */
    public ChangelogChangeSet(IChangelogChangeSetIdentity identity, List<IChangelogEntry> entries,
                              List<IChangelogEntry> rollbackEntries, String comment,
                              boolean runAlways, boolean runOnChange,
                              boolean failOnError, boolean runInTransaction,
                              List<String> contexts, List<String> labels) {
        this.identity = identity;
        this.entries = Collections.unmodifiableList(new ArrayList<>(entries));
        this.rollbackEntries = Collections.unmodifiableList(new ArrayList<>(rollbackEntries));
        this.comment = comment;
        this.runAlways = runAlways;
        this.runOnChange = runOnChange;
        this.failOnError = failOnError;
        this.runInTransaction = runInTransaction;
        if (contexts != null) {
            this.contexts = Collections.unmodifiableList(new ArrayList<>(contexts));
        } else {
            this.contexts = Collections.emptyList();
        }
        if (labels != null) {
            this.labels = Collections.unmodifiableList(new ArrayList<>(labels));
        } else {
            this.labels = Collections.emptyList();
        }
    }

    /**
     * @see com.github.toolarium.db.changelog.dto.IChangelogChangeSet#getIdentity()
     */
    @Override
    public IChangelogChangeSetIdentity getIdentity() {
        return identity;
    }

    /**
     * @see com.github.toolarium.db.changelog.dto.IChangelogChangeSet#getEntries()
     */
    @Override
    public List<IChangelogEntry> getEntries() {
        return entries;
    }

    /**
     * @see com.github.toolarium.db.changelog.dto.IChangelogChangeSet#getRollbackEntries()
     */
    @Override
    public List<IChangelogEntry> getRollbackEntries() {
        return rollbackEntries;
    }

    /**
     * @see com.github.toolarium.db.changelog.dto.IChangelogChangeSet#getComment()
     */
    @Override
    public String getComment() {
        return comment;
    }

    /**
     * @see com.github.toolarium.db.changelog.dto.IChangelogChangeSet#isRunAlways()
     */
    @Override
    public boolean isRunAlways() {
        return runAlways;
    }

    /**
     * @see com.github.toolarium.db.changelog.dto.IChangelogChangeSet#isRunOnChange()
     */
    @Override
    public boolean isRunOnChange() {
        return runOnChange;
    }

    /**
     * @see com.github.toolarium.db.changelog.dto.IChangelogChangeSet#isFailOnError()
     */
    @Override
    public boolean isFailOnError() {
        return failOnError;
    }

    /**
     * @see com.github.toolarium.db.changelog.dto.IChangelogChangeSet#isRunInTransaction()
     */
    @Override
    public boolean isRunInTransaction() {
        return runInTransaction;
    }

    /**
     * @see com.github.toolarium.db.changelog.dto.IChangelogChangeSet#getContexts()
     */
    @Override
    public List<String> getContexts() {
        return contexts;
    }

    /**
     * @see com.github.toolarium.db.changelog.dto.IChangelogChangeSet#getLabels()
     */
    @Override
    public List<String> getLabels() {
        return labels;
    }


    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return java.util.Objects.hash(identity);
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
        return java.util.Objects.equals(identity, ((ChangelogChangeSet) obj).identity);
    }


    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "ChangelogChangeSet[" + identity + "]";
    }
}
