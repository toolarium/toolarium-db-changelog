/*
 * ChangelogResourceResolverFactory.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog.impl.resolver;

import com.github.toolarium.db.changelog.IChangelogResourceResolver;
import com.github.toolarium.db.changelog.impl.resolver.impl.ChangelogResourceResolverImpl;


/**
 * Singleton factory for creating changelog resource resolver instances.
 *
 * @author patrick
 */
public final class ChangelogResourceResolverFactory {

    /**
     * Private class, the only instance of the singleton which will be created by accessing the holder class.
     *
     * @author patrick
     */
    private static final class HOLDER {
        static final ChangelogResourceResolverFactory INSTANCE = new ChangelogResourceResolverFactory();
    }


    /**
     * Private constructor
     */
    private ChangelogResourceResolverFactory() {
    }


    /**
     * Get the instance.
     *
     * @return the instance
     */
    public static ChangelogResourceResolverFactory getInstance() {
        return HOLDER.INSTANCE;
    }


    /**
     * Get the changelog resource resolver.
     *
     * @return the resource resolver
     */
    public IChangelogResourceResolver getResourceResolver() {
        return ChangelogResourceResolverImpl.getInstance();
    }
}
