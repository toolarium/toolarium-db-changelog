/*
 * IChangelogPrecondition.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog.model.precondition;


/**
 * A precondition that must be evaluated before a changeset executes.
 *
 * @author patrick
 */
public interface IChangelogPrecondition {

    /**
     * Get a human-readable description of this precondition.
     *
     * @return the description
     */
    String getDescription();
}
