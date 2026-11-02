/*
 * ChangelogParameterResolverImpl.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog.impl.resolver.impl;

import com.github.toolarium.common.util.PropertyExpander;
import com.github.toolarium.common.util.PropertyExpanderContextBasedProperties;
import com.github.toolarium.db.changelog.impl.resolver.IChangelogParameterResolver;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Default implementation of parameter resolution using toolarium-common PropertyExpander.
 *
 * <p>Resolves <code>${name}</code> placeholders in this order:
 * <ol>
 *   <li>Changelog-defined properties (set via {@link #setProperty(String, String)} or YAML properties block)</li>
 *   <li>System properties</li>
 *   <li>Environment variables</li>
 * </ol>
 * Properties files can be loaded via {@link #loadProperties(InputStream)}.
 * This class is not thread-safe. Each parser or migrator creates its own instance.</p>
 *
 * @author patrick
 */
public class ChangelogParameterResolverImpl implements IChangelogParameterResolver {
    private static final Logger LOG = LoggerFactory.getLogger(ChangelogParameterResolverImpl.class);
    private final Properties properties;


    /**
     * Constructor for ChangelogParameterResolverImpl
     */
    public ChangelogParameterResolverImpl() {
        this.properties = new Properties();
    }


    /**
     * Constructor for ChangelogParameterResolverImpl
     *
     * @param properties the initial properties
     */
    public ChangelogParameterResolverImpl(Map<String, String> properties) {
        this.properties = new Properties();
        if (properties != null) {
            this.properties.putAll(properties);
        }
    }


    /**
     * @see com.github.toolarium.db.changelog.impl.resolver.IChangelogParameterResolver#resolve(String)
     */
    @Override
    public String resolve(String value) {
        if (value == null) {
            return null;
        }

        Properties previous = PropertyExpanderContextBasedProperties.get();
        try {
            Properties merged = new Properties();
            if (previous != null) {
                merged.putAll(previous);
            }
            merged.putAll(properties);
            PropertyExpanderContextBasedProperties.set(merged);

            return PropertyExpander.getInstance().expand(value);
        } finally {
            if (previous == null) {
                PropertyExpanderContextBasedProperties.unset();
            } else {
                PropertyExpanderContextBasedProperties.set(previous);
            }
        }
    }


    /**
     * @see com.github.toolarium.db.changelog.impl.resolver.IChangelogParameterResolver#setProperty(String, String)
     */
    @Override
    public void setProperty(String name, String value) {
        properties.setProperty(name, value);
    }


    /**
     * @see com.github.toolarium.db.changelog.impl.resolver.IChangelogParameterResolver#hasProperty(String)
     */
    @Override
    public boolean hasProperty(String name) {
        if (properties.containsKey(name)) {
            return true;
        }
        if (System.getProperty(name) != null) {
            return true;
        }
        if (System.getenv(name) != null) {
            return true;
        }
        return false;
    }


    /**
     * Load properties from an input stream (Java properties file format).
     * The caller is responsible for closing the input stream.
     *
     * @param input the input stream
     * @throws IllegalStateException if the properties cannot be loaded
     */
    public void loadProperties(InputStream input) {
        try {
            Properties props = new Properties();
            props.load(input);
            properties.putAll(props);

            if (LOG.isDebugEnabled()) {
                LOG.debug("Loaded " + props.size() + " property(s) from input stream.");
            }
        } catch (Exception e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Failed to load properties: " + e.getMessage(), e);
            }
            throw new IllegalStateException("Failed to load changelog properties.", e);
        }
    }
}
