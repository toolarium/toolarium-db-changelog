/*
 * ChangelogParserUtil.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog.impl.parser.util;

import java.util.List;
import java.util.Map;


/**
 * Utility for safe type casting of parsed changelog data structures.
 * Centralizes unchecked casts with instanceof guards in a single place.
 *
 * @author patrick
 */
public final class ChangelogParserUtil {

    /**
     * Private class, the only instance of the singleton which will be created by accessing the holder class.
     *
     * @author patrick
     */
    private static final class HOLDER {
        static final ChangelogParserUtil INSTANCE = new ChangelogParserUtil();
    }


    /**
     * Private constructor
     */
    private ChangelogParserUtil() {
    }


    /**
     * Get the instance.
     *
     * @return the instance
     */
    public static ChangelogParserUtil getInstance() {
        return HOLDER.INSTANCE;
    }


    /**
     * Get a value as a List. Returns null if the value is null or not a List.
     *
     * @param <T> the list element type
     * @param value the value
     * @return the value as a List, or null
     */
    @SuppressWarnings("unchecked")
    public <T> List<T> listOf(Object value) {
        if (value instanceof List) {
            return (List<T>) value;
        }
        return null;
    }


    /**
     * Get a value as a Map. Returns null if the value is null or not a Map.
     *
     * @param value the value
     * @return the value as a Map, or null
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> mapOf(Object value) {
        if (value instanceof Map) {
            return (Map<String, Object>) value;
        }
        return null;
    }
}
