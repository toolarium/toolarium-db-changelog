/*
 * ChangelogContextImplTest.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.toolarium.db.changelog.IChangelogContext;
import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.Test;


/**
 * Tests for ChangelogContextImpl.
 *
 * @author patrick
 */
public class ChangelogContextImplTest {
    private static final String TEST_CONTEXT_DEV = "dev";
    private static final String TEST_LABEL_V1 = "v1.0";


    /**
     * Test default empty context.
     */
    @Test
    void testEmptyContext() {
        IChangelogContext context = new ChangelogContextImpl(null, null);
        assertNotNull(context.getActiveContexts());
        assertTrue(context.getActiveContexts().isEmpty());
        assertNotNull(context.getActiveLabels());
        assertTrue(context.getActiveLabels().isEmpty());
    }


    /**
     * Test context with values.
     */
    @Test
    void testContextWithValues() {
        IChangelogContext context = new ChangelogContextImpl(
                Arrays.asList(TEST_CONTEXT_DEV, "test"), Arrays.asList(TEST_LABEL_V1));
        assertEquals(2, context.getActiveContexts().size());
        assertEquals(TEST_CONTEXT_DEV, context.getActiveContexts().get(0));
        assertEquals("test", context.getActiveContexts().get(1));
        assertEquals(1, context.getActiveLabels().size());
        assertEquals(TEST_LABEL_V1, context.getActiveLabels().get(0));
    }


    /**
     * Test equals, hashCode, toString.
     */
    @Test
    void testEqualsHashCodeToString() {
        ChangelogContextImpl ctx1 = new ChangelogContextImpl(
                Arrays.asList(TEST_CONTEXT_DEV), Arrays.asList(TEST_LABEL_V1));
        ChangelogContextImpl ctx2 = new ChangelogContextImpl(
                Arrays.asList(TEST_CONTEXT_DEV), Arrays.asList(TEST_LABEL_V1));
        ChangelogContextImpl ctx3 = new ChangelogContextImpl(
                Arrays.asList("prod"), Collections.emptyList());

        assertEquals(ctx1, ctx2);
        assertEquals(ctx1.hashCode(), ctx2.hashCode());
        assertNotEquals(ctx1, ctx3);
        assertNotNull(ctx1.toString());
        assertTrue(ctx1.toString().contains(TEST_CONTEXT_DEV));
        assertTrue(ctx1.toString().contains(TEST_LABEL_V1));
    }


    /**
     * Test that lists are immutable.
     */
    @Test
    void testImmutability() {
        IChangelogContext context = new ChangelogContextImpl(
                Arrays.asList(TEST_CONTEXT_DEV), Arrays.asList(TEST_LABEL_V1));
        try {
            context.getActiveContexts().add("should-fail");
            assertTrue(false, "Should have thrown UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }
}
