/*
 * ChangelogParseException.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog.exception;


/**
 * Exception thrown when changelog parsing fails.
 *
 * @author patrick
 */
public class ChangelogParseException extends ChangelogException {
    private static final long serialVersionUID = 1L;


    /**
     * Constructor for ChangelogParseException
     *
     * @param message the message
     */
    public ChangelogParseException(String message) {
        super(message);
    }


    /**
     * Constructor for ChangelogParseException
     *
     * @param message the message
     * @param cause the cause
     */
    public ChangelogParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
