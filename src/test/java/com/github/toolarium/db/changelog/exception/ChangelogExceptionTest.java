/*
 * ChangelogExceptionTest.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog.exception;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;


/**
 * Tests for exception classes.
 *
 * @author patrick
 */
public class ChangelogExceptionTest {

    /**
     * Test ChangelogException with message.
     */
    @Test
    void testChangelogException() {
        ChangelogException ex = new ChangelogException("test error");
        assertEquals("test error", ex.getMessage());
        assertNull(ex.getCause());
    }


    /**
     * Test ChangelogException with message and cause.
     */
    @Test
    void testChangelogExceptionWithCause() {
        RuntimeException cause = new RuntimeException("root cause");
        ChangelogException ex = new ChangelogException("wrapped", cause);
        assertEquals("wrapped", ex.getMessage());
        assertNotNull(ex.getCause());
        assertEquals("root cause", ex.getCause().getMessage());
    }


    /**
     * Test ChangelogParseException.
     */
    @Test
    void testChangelogParseException() {
        ChangelogParseException ex = new ChangelogParseException("parse error");
        assertEquals("parse error", ex.getMessage());
        assertNull(ex.getCause());
    }


    /**
     * Test ChangelogParseException with cause.
     */
    @Test
    void testChangelogParseExceptionWithCause() {
        Exception cause = new Exception("bad format");
        ChangelogParseException ex = new ChangelogParseException("parse failed", cause);
        assertNotNull(ex.getCause());
    }


    /**
     * Test ChangelogChangeSetExecutionException.
     */
    @Test
    void testChangelogChangeSetExecutionException() {
        ChangelogChangeSetExecutionException ex = new ChangelogChangeSetExecutionException("exec error");
        assertEquals("exec error", ex.getMessage());
    }


    /**
     * Test ChangelogChangeSetExecutionException with cause.
     */
    @Test
    void testChangelogChangeSetExecutionExceptionWithCause() {
        Exception cause = new Exception("sql error");
        ChangelogChangeSetExecutionException ex = new ChangelogChangeSetExecutionException("failed", cause);
        assertNotNull(ex.getCause());
    }
}
