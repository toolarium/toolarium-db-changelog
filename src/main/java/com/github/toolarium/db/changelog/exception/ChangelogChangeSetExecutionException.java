/*
 * ChangelogChangeSetExecutionException.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog.exception;

/**
 * Exception thrown when a changeset execution fails.
 *
 * @author patrick
 */
public class ChangelogChangeSetExecutionException extends ChangelogException {
    private static final long serialVersionUID = 1L;

    /**
     * Constructor for ChangelogChangeSetExecutionException
     *
     * @param message the message
     */
    public ChangelogChangeSetExecutionException(String message) {
        super(message);
    }

    /**
     * Constructor for ChangelogChangeSetExecutionException
     *
     * @param message the message
     * @param cause the cause
     */
    public ChangelogChangeSetExecutionException(String message, Throwable cause) {
        super(message, cause);
    }
}
