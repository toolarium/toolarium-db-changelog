/*
 * ChangelogParameterResolverTest.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog.impl.resolver;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.toolarium.db.changelog.impl.resolver.impl.ChangelogParameterResolverImpl;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;


/**
 * Tests for ChangelogParameterResolverImpl.
 *
 * @author patrick
 */
public class ChangelogParameterResolverTest {

    /**
     * Test basic property resolution.
     */
    @Test
    void testResolve() {
        ChangelogParameterResolverImpl resolver = new ChangelogParameterResolverImpl();
        resolver.setProperty("name", "world");

        assertEquals("hello world", resolver.resolve("hello ${name}"));
    }


    /**
     * Test multiple properties.
     */
    @Test
    void testMultipleProperties() {
        ChangelogParameterResolverImpl resolver = new ChangelogParameterResolverImpl();
        resolver.setProperty("schema", "public");
        resolver.setProperty("prefix", "app_");

        assertEquals("public.app_users", resolver.resolve("${schema}.${prefix}users"));
    }


    /**
     * Test no placeholders.
     */
    @Test
    void testNoPlaceholders() {
        ChangelogParameterResolverImpl resolver = new ChangelogParameterResolverImpl();
        assertEquals("plain text", resolver.resolve("plain text"));
    }


    /**
     * Test null value.
     */
    @Test
    void testNullValue() {
        ChangelogParameterResolverImpl resolver = new ChangelogParameterResolverImpl();
        assertNull(resolver.resolve(null));
    }


    /**
     * Test unresolved placeholder stays.
     */
    @Test
    void testUnresolvedPlaceholder() {
        ChangelogParameterResolverImpl resolver = new ChangelogParameterResolverImpl();
        assertEquals("${missing}", resolver.resolve("${missing}"));
    }


    /**
     * Test hasProperty.
     */
    @Test
    void testHasProperty() {
        ChangelogParameterResolverImpl resolver = new ChangelogParameterResolverImpl();
        resolver.setProperty("key", "val");

        assertTrue(resolver.hasProperty("key"));
        assertFalse(resolver.hasProperty("other"));
    }


    /**
     * Test constructor with initial properties.
     */
    @Test
    void testConstructorWithProperties() {
        Map<String, String> props = new HashMap<>();
        props.put("db", "postgres");

        ChangelogParameterResolverImpl resolver = new ChangelogParameterResolverImpl(props);
        assertEquals("postgres", resolver.resolve("${db}"));
        assertTrue(resolver.hasProperty("db"));
    }
}
