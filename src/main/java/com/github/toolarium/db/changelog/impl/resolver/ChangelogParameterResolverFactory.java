/*
 * ChangelogParameterResolverFactory.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog.impl.resolver;


import com.github.toolarium.db.changelog.impl.resolver.impl.ChangelogParameterResolverImpl;


/**
 * Singleton factory for creating changelog parameter resolver instances.
 *
 * @author patrick
 */
public final class ChangelogParameterResolverFactory {

    /**
     * Private class, the only instance of the singleton which will be created by accessing the holder class.
     *
     * @author patrick
     */
    private static final class HOLDER {
        static final ChangelogParameterResolverFactory INSTANCE = new ChangelogParameterResolverFactory();
    }


    /**
     * Private constructor
     */
    private ChangelogParameterResolverFactory() {
    }


    /**
     * Get the instance.
     *
     * @return the instance
     */
    public static ChangelogParameterResolverFactory getInstance() {
        return HOLDER.INSTANCE;
    }


    /**
     * Get a new parameter resolver.
     *
     * @return the parameter resolver instance
     */
    public IChangelogParameterResolver getParameterResolver() {
        return new ChangelogParameterResolverImpl();
    }
}
