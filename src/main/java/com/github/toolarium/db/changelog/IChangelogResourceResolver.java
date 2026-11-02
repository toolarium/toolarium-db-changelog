/*
 * IChangelogResourceResolver.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog;

import java.io.InputStream;
import java.net.URL;


/**
 * Resolves changelog resources from classpath, file system, or archives.
 *
 * @author patrick
 */
public interface IChangelogResourceResolver {

    /**
     * Resolve a changelog resource to an input stream.
     * Searches classpath (including JARs) and file system.
     * The classpath: prefix forces classpath-only resolution.
     *
     * @param path the resource path (e.g., "db/changelog.db.yaml" or "classpath:db/changelog.db.yaml")
     * @return the input stream, or null if the resource was not found
     */
    InputStream resolve(String path);


    /**
     * Resolve a changelog resource to a URL.
     * Searches classpath (including JARs) and file system.
     * The classpath: prefix forces classpath-only resolution.
     *
     * @param path the resource path
     * @return the URL, or null if the resource was not found
     */
    URL resolveAsUrl(String path);


    /**
     * Check if a changelog resource exists.
     *
     * @param path the resource path
     * @return true if the resource exists
     */
    boolean exists(String path);
}
