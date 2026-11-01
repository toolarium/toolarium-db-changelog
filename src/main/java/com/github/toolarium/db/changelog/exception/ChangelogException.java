/*
 * ChangelogException.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog.exception;

/**
 * Base exception for all changelog operations.
 *
 * @author patrick
 */
public class ChangelogException extends Exception {
    private static final long serialVersionUID = 1L;

    /**
     * Constructor for ChangelogException
     *
     * @param message the message
     */
    public ChangelogException(String message) {
        super(message);
    }

    /**
     * Constructor for ChangelogException
     *
     * @param message the message
     * @param cause the cause
     */
    public ChangelogException(String message, Throwable cause) {
        super(message, cause);
    }
}
