/*
 * ChangelogWriterFactory.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog.impl.writer;

import com.github.toolarium.db.changelog.config.ChangelogConfiguration;
import com.github.toolarium.db.changelog.impl.writer.impl.JsonChangelogWriterImpl;
import com.github.toolarium.db.changelog.impl.writer.impl.XmlChangelogWriterImpl;
import com.github.toolarium.db.changelog.impl.writer.impl.YamlChangelogWriterImpl;


/**
 * Singleton factory for changelog writers.
 *
 * @author patrick
 */
public final class ChangelogWriterFactory {

    /**
     * Private class, the only instance of the singleton which will be created by accessing the holder class.
     *
     * @author patrick
     */
    private static final class HOLDER {
        static final ChangelogWriterFactory INSTANCE = new ChangelogWriterFactory();
    }


    /**
     * Private constructor
     */
    private ChangelogWriterFactory() {
    }


    /**
     * Get the instance.
     *
     * @return the instance
     */
    public static ChangelogWriterFactory getInstance() {
        return HOLDER.INSTANCE;
    }


    /**
     * Get a writer for the given file extension.
     *
     * @param fileExtension the file extension (e.g., ".db.yaml", ".db.xml", ".db.json")
     * @return the writer, or null if no writer supports the extension
     */
    public IChangelogWriter getWriter(String fileExtension) {
        if (ChangelogConfiguration.FILE_EXTENSION_DB_YAML.equals(fileExtension) || ChangelogConfiguration.FILE_EXTENSION_DB_YML.equals(fileExtension)) {
            return getYamlWriter();
        }
        if (ChangelogConfiguration.FILE_EXTENSION_DB_XML.equals(fileExtension)) {
            return getXmlWriter();
        }
        if (ChangelogConfiguration.FILE_EXTENSION_DB_JSON.equals(fileExtension)) {
            return getJsonWriter();
        }
        return null;
    }


    /**
     * Get the YAML changelog writer.
     *
     * @return the YAML writer
     */
    public IChangelogWriter getYamlWriter() {
        return new YamlChangelogWriterImpl();
    }


    /**
     * Get the XML changelog writer.
     *
     * @return the XML writer
     */
    public IChangelogWriter getXmlWriter() {
        return new XmlChangelogWriterImpl();
    }


    /**
     * Get the JSON changelog writer.
     *
     * @return the JSON writer
     */
    public IChangelogWriter getJsonWriter() {
        return new JsonChangelogWriterImpl();
    }
}
