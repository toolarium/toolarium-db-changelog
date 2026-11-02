/*
 * ChangelogParserUtilTest.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog.impl.parser.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;


/**
 * Tests for ChangelogParserUtil.
 *
 * @author patrick
 */
public class ChangelogParserUtilTest {

    /**
     * Test singleton instance.
     */
    @Test
    void testSingleton() {
        assertSame(ChangelogParserUtil.getInstance(), ChangelogParserUtil.getInstance());
    }


    /**
     * Test listOf with a List value.
     */
    @Test
    void testListOfWithList() {
        List<String> input = Arrays.asList("a", "b", "c");
        List<String> result = ChangelogParserUtil.getInstance().listOf(input);
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("a", result.get(0));
    }


    /**
     * Test listOf with null.
     */
    @Test
    void testListOfWithNull() {
        List<String> result = ChangelogParserUtil.getInstance().listOf(null);
        assertNull(result);
    }


    /**
     * Test listOf with non-List value.
     */
    @Test
    void testListOfWithNonList() {
        List<String> result = ChangelogParserUtil.getInstance().listOf("not a list");
        assertNull(result);
    }


    /**
     * Test listOf with empty list.
     */
    @Test
    void testListOfWithEmptyList() {
        List<Object> result = ChangelogParserUtil.getInstance().listOf(Collections.emptyList());
        assertNotNull(result);
        assertEquals(0, result.size());
    }


    /**
     * Test mapOf with a Map value.
     */
    @Test
    void testMapOfWithMap() {
        Map<String, Object> input = new HashMap<>();
        input.put("key", "value");
        Map<String, Object> result = ChangelogParserUtil.getInstance().mapOf(input);
        assertNotNull(result);
        assertEquals("value", result.get("key"));
    }


    /**
     * Test mapOf with null.
     */
    @Test
    void testMapOfWithNull() {
        Map<String, Object> result = ChangelogParserUtil.getInstance().mapOf(null);
        assertNull(result);
    }


    /**
     * Test mapOf with non-Map value.
     */
    @Test
    void testMapOfWithNonMap() {
        Map<String, Object> result = ChangelogParserUtil.getInstance().mapOf("not a map");
        assertNull(result);
    }


    /**
     * Test mapOf with empty map.
     */
    @Test
    void testMapOfWithEmptyMap() {
        Map<String, Object> result = ChangelogParserUtil.getInstance().mapOf(Collections.emptyMap());
        assertNotNull(result);
        assertEquals(0, result.size());
    }


    /**
     * Test mapOf with integer value returns null.
     */
    @Test
    void testMapOfWithInteger() {
        Map<String, Object> result = ChangelogParserUtil.getInstance().mapOf(42);
        assertNull(result);
    }


    /**
     * Test listOf with integer value returns null.
     */
    @Test
    void testListOfWithInteger() {
        List<Object> result = ChangelogParserUtil.getInstance().listOf(42);
        assertNull(result);
    }
}
