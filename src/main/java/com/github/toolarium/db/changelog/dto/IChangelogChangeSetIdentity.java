/*
 * IChangelogChangeSetIdentity.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog.dto;

/**
 * The composite identity of a changeset: id + author + filepath.
 *
 * @author patrick
 */
public interface IChangelogChangeSetIdentity {

    /**
     * Get the changeset id.
     *
     * @return the id
     */
    String getId();

    /**
     * Get the author.
     *
     * @return the author
     */
    String getAuthor();

    /**
     * Get the file path (or logical file path if set).
     *
     * @return the file path
     */
    String getFilepath();
}
