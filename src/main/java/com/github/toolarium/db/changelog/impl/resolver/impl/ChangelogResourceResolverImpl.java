/*
 * ChangelogResourceResolverImpl.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog.impl.resolver.impl;

import com.github.toolarium.common.util.ClassPathUtil;
import com.github.toolarium.common.util.FileUtil;
import com.github.toolarium.db.changelog.IChangelogResourceResolver;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Resolves changelog resources from classpath, file system, or archives.
 * Uses toolarium-common ClassPathUtil for classpath scanning and FileUtil for path normalization.
 *
 * @author patrick
 */
public final class ChangelogResourceResolverImpl implements IChangelogResourceResolver {
    private static final String CLASSPATH_PREFIX = "classpath:";
    private static final Logger LOG = LoggerFactory.getLogger(ChangelogResourceResolverImpl.class);


    /**
     * Private class, the only instance of the singleton which will be created by accessing the holder class.
     *
     * @author patrick
     */
    private static final class HOLDER {
        static final ChangelogResourceResolverImpl INSTANCE = new ChangelogResourceResolverImpl();
    }


    /**
     * Private constructor
     */
    private ChangelogResourceResolverImpl() {
    }


    /**
     * Get the instance.
     *
     * @return the instance
     */
    public static ChangelogResourceResolverImpl getInstance() {
        return HOLDER.INSTANCE;
    }


    /**
     * @see com.github.toolarium.db.changelog.IChangelogResourceResolver#resolve(String)
     */
    @Override
    public InputStream resolve(String path) {
        if (path == null || path.trim().isEmpty()) {
            return null;
        }

        String normalizedPath = normalizePath(path);

        // Try ClassPathUtil (searches classpath including JARs)
        InputStream result = resolveFromClassPathUtil(normalizedPath);
        if (result != null) {
            return result;
        }

        // Try classloaders directly
        result = resolveFromClassLoaders(normalizedPath);
        if (result != null) {
            return result;
        }

        // Try file system (unless classpath-only)
        if (!path.startsWith(CLASSPATH_PREFIX)) {
            result = resolveFromFileSystem(normalizedPath);
            if (result != null) {
                return result;
            }
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Resource not found [" + normalizedPath + "].");
        }
        return null;
    }


    /**
     * @see com.github.toolarium.db.changelog.IChangelogResourceResolver#resolveAsUrl(String)
     */
    @Override
    public URL resolveAsUrl(String path) {
        if (path == null || path.trim().isEmpty()) {
            return null;
        }

        String normalizedPath = normalizePath(path);

        // Try ClassPathUtil
        List<URL> urls = ClassPathUtil.getInstance().searchFileByRegExpAsURLList(
                Pattern.quote(normalizedPath));
        if (urls != null && !urls.isEmpty()) {
            return urls.get(0);
        }

        // Try classloaders
        URL url = Thread.currentThread().getContextClassLoader().getResource(normalizedPath);
        if (url != null) {
            return url;
        }
        url = getClass().getResource(normalizedPath);
        if (url != null) {
            return url;
        }

        // Try file system
        if (!path.startsWith(CLASSPATH_PREFIX)) {
            File file = new File(normalizedPath);
            if (file.exists()) {
                try {
                    return file.toURI().toURL();
                } catch (IOException e) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Failed to convert file to URL [" + normalizedPath + "]: " + e.getMessage(), e);
                    }
                }
            }
        }

        return null;
    }


    /**
     * @see com.github.toolarium.db.changelog.IChangelogResourceResolver#exists(String)
     */
    @Override
    public boolean exists(String path) {
        return resolveAsUrl(path) != null;
    }


    /**
     * Normalize the path by removing the classpath prefix and simplifying.
     *
     * @param path the raw path
     * @return the normalized path
     */
    private String normalizePath(String path) {
        String result = path;
        if (result.startsWith(CLASSPATH_PREFIX)) {
            result = result.substring(CLASSPATH_PREFIX.length());
        }
        return FileUtil.getInstance().simplifyPath(result);
    }


    /**
     * Try to resolve from ClassPathUtil which scans classpath entries including JARs.
     *
     * @param normalizedPath the normalized path
     * @return the input stream, or null
     */
    private InputStream resolveFromClassPathUtil(String normalizedPath) {
        List<URL> urls = ClassPathUtil.getInstance().searchFileByRegExpAsURLList(
                Pattern.quote(normalizedPath));
        if (urls != null && !urls.isEmpty()) {
            try {
                return urls.get(0).openStream();
            } catch (IOException e) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Failed to open classpath resource [" + normalizedPath + "]: " + e.getMessage(), e);
                }
            }
        }
        return null;
    }


    /**
     * Try to resolve from thread context classloader and class classloader.
     *
     * @param normalizedPath the normalized path
     * @return the input stream, or null
     */
    private InputStream resolveFromClassLoaders(String normalizedPath) {
        InputStream input = Thread.currentThread().getContextClassLoader().getResourceAsStream(normalizedPath);
        if (input != null) {
            return input;
        }
        return getClass().getResourceAsStream(normalizedPath);
    }


    /**
     * Try to resolve from the file system.
     *
     * @param normalizedPath the normalized path
     * @return the input stream, or null
     */
    private InputStream resolveFromFileSystem(String normalizedPath) {
        try {
            return new FileInputStream(new File(normalizedPath));
        } catch (FileNotFoundException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("File not found [" + normalizedPath + "]: " + e.getMessage(), e);
            }
            return null;
        }
    }
}
