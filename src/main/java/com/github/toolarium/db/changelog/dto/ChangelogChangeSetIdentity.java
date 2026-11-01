/*
 * ChangelogChangeSetIdentity.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog.dto;


import java.util.Objects;

/**
 * Default implementation of a changeset identity.
 *
 * @author patrick
 */
public class ChangelogChangeSetIdentity implements IChangelogChangeSetIdentity {
    private final String id;
    private final String author;
    private final String filepath;

    /**
     * Constructor for ChangelogChangeSetIdentity
     *
     * @param id the changeset id
     * @param author the author
     * @param filepath the file path
     */
    public ChangelogChangeSetIdentity(String id, String author, String filepath) {
        this.id = id;
        this.author = author;
        this.filepath = filepath;
    }

    /**
     * @see com.github.toolarium.db.changelog.dto.IChangelogChangeSetIdentity#getId()
     */
    @Override
    public String getId() {
        return id;
    }

    /**
     * @see com.github.toolarium.db.changelog.dto.IChangelogChangeSetIdentity#getAuthor()
     */
    @Override
    public String getAuthor() {
        return author;
    }

    /**
     * @see com.github.toolarium.db.changelog.dto.IChangelogChangeSetIdentity#getFilepath()
     */
    @Override
    public String getFilepath() {
        return filepath;
    }

    /**
     * @see java.lang.Object#equals(Object)
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChangelogChangeSetIdentity that = (ChangelogChangeSetIdentity) o;
        return Objects.equals(id, that.id) && Objects.equals(author, that.author) && Objects.equals(filepath, that.filepath);
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return Objects.hash(id, author, filepath);
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return id + "::" + author + "::" + filepath;
    }
}
