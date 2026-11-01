/*
 * ChangelogParameterResolverImpl.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog.impl.resolver.impl;


import com.github.toolarium.db.changelog.impl.resolver.IChangelogParameterResolver;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Default implementation of parameter resolution using a simple map-based lookup.
 *
 * @author patrick
 */
public class ChangelogParameterResolverImpl implements IChangelogParameterResolver {
    private static final String PLACEHOLDER_PREFIX = "${";
    private static final String PLACEHOLDER_SUFFIX = "}";
    private final Map<String, String> properties;


    /**
     * Constructor for ChangelogParameterResolverImpl
     */
    public ChangelogParameterResolverImpl() {
        this.properties = new LinkedHashMap<>();
    }


    /**
     * Constructor for ChangelogParameterResolverImpl
     *
     * @param properties the initial properties
     */
    public ChangelogParameterResolverImpl(Map<String, String> properties) {
        this.properties = new LinkedHashMap<>(properties);
    }


    /**
     * @see com.github.toolarium.db.changelog.impl.resolver.IChangelogParameterResolver#resolve(String)
     */
    @Override
    public String resolve(String value) {
        if (value == null || !value.contains(PLACEHOLDER_PREFIX)) {
            return value;
        }

        String result = value;
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            String placeholder = PLACEHOLDER_PREFIX + entry.getKey() + PLACEHOLDER_SUFFIX;
            result = result.replace(placeholder, entry.getValue());
        }

        return result;
    }


    /**
     * @see com.github.toolarium.db.changelog.impl.resolver.IChangelogParameterResolver#setProperty(String, String)
     */
    @Override
    public void setProperty(String name, String value) {
        properties.put(name, value);
    }


    /**
     * @see com.github.toolarium.db.changelog.impl.resolver.IChangelogParameterResolver#hasProperty(String)
     */
    @Override
    public boolean hasProperty(String name) {
        return properties.containsKey(name);
    }
}
