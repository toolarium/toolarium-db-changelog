/*
 * JsonChangelogWriterImpl.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog.impl.writer.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.github.toolarium.db.changelog.config.ChangelogConfiguration;
import com.github.toolarium.db.changelog.exception.ChangelogException;
import com.github.toolarium.db.changelog.impl.writer.IChangelogWriter;
import com.github.toolarium.db.changelog.impl.writer.util.ChangelogWriterUtil;
import com.github.toolarium.db.changelog.model.IChangelog;
import com.github.toolarium.db.changelog.model.IChangelogChangeSet;
import java.io.OutputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Writes a changelog model to the DB-Changelog JSON format (.db.json).
 *
 * @author patrick
 */
public class JsonChangelogWriterImpl implements IChangelogWriter {
    private static final Logger LOG = LoggerFactory.getLogger(JsonChangelogWriterImpl.class);
    private static final ObjectMapper OBJECT_MAPPER = createObjectMapper();


    /**
     * @see com.github.toolarium.db.changelog.impl.writer.IChangelogWriter#write(IChangelog, OutputStream)
     */
    @Override
    public void write(IChangelog changelog, OutputStream output) throws ChangelogException {
        try {
            OBJECT_MAPPER.writeValue(output, buildRoot(changelog));

            if (LOG.isDebugEnabled()) {
                LOG.debug("Wrote " + changelog.getChangelogSets().size() + " changeset(s) to JSON.");
            }
        } catch (Exception e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Failed to write JSON changelog: " + e.getMessage(), e);
            }
            throw new ChangelogException("Failed to write JSON changelog.", e);
        }
    }


    /**
     * @see com.github.toolarium.db.changelog.impl.writer.IChangelogWriter#write(IChangelog, Writer)
     */
    @Override
    public void write(IChangelog changelog, Writer writer) throws ChangelogException {
        try {
            OBJECT_MAPPER.writeValue(writer, buildRoot(changelog));

            if (LOG.isDebugEnabled()) {
                LOG.debug("Wrote " + changelog.getChangelogSets().size() + " changeset(s) to JSON.");
            }
        } catch (Exception e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Failed to write JSON changelog: " + e.getMessage(), e);
            }
            throw new ChangelogException("Failed to write JSON changelog.", e);
        }
    }


    /**
     * @see com.github.toolarium.db.changelog.impl.writer.IChangelogWriter#getFileExtension()
     */
    @Override
    public String getFileExtension() {
        return ChangelogConfiguration.FILE_EXTENSION_DB_JSON;
    }


    /**
     * Build the root map structure for the JSON output.
     *
     * @param changelog the changelog
     * @return the root map
     */
    private Map<String, Object> buildRoot(IChangelog changelog) {
        Map<String, Object> root = new LinkedHashMap<>();
        root.put("format", ChangelogConfiguration.FORMAT_VERSION);

        List<Map<String, Object>> changesets = new ArrayList<>();
        for (IChangelogChangeSet changeSet : changelog.getChangelogSets()) {
            changesets.add(ChangelogWriterUtil.getInstance().toChangeSetMap(changeSet));
        }
        root.put("changesets", changesets);

        return root;
    }


    /**
     * Create a configured ObjectMapper for pretty-printing.
     *
     * @return the object mapper
     */
    private static ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        return mapper;
    }
}
