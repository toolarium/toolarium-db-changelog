/*
 * IChangelogParameterResolver.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog.impl.resolver;


/**
 * Resolves ${property} placeholders in changelog values.
 *
 * @author patrick
 */
public interface IChangelogParameterResolver {

    /**
     * Resolve all ${property} placeholders in the given value.
     *
     * @param value the value possibly containing placeholders
     * @return the resolved value
     */
    String resolve(String value);


    /**
     * Set a property value.
     *
     * @param name the property name
     * @param value the property value
     */
    void setProperty(String name, String value);


    /**
     * Check if a property is defined.
     *
     * @param name the property name
     * @return true if the property is defined
     */
    boolean hasProperty(String name);
}
