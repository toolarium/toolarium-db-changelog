/*
 * YamlChangelogWriterImpl.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog.impl.writer.impl;

import com.github.toolarium.db.changelog.config.ChangelogConfiguration;
import com.github.toolarium.db.changelog.exception.ChangelogException;
import com.github.toolarium.db.changelog.impl.writer.IChangelogWriter;
import com.github.toolarium.db.changelog.impl.writer.util.ChangelogWriterUtil;
import com.github.toolarium.db.changelog.model.IChangelog;
import com.github.toolarium.db.changelog.model.IChangelogChangeSet;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;


/**
 * Writes a changelog model to the DB-Changelog YAML format (.db.yaml).
 *
 * @author patrick
 */
public class YamlChangelogWriterImpl implements IChangelogWriter {
    private static final Logger LOG = LoggerFactory.getLogger(YamlChangelogWriterImpl.class);


    /**
     * @see com.github.toolarium.db.changelog.impl.writer.IChangelogWriter#write(IChangelog, OutputStream)
     */
    @Override
    public void write(IChangelog changelog, OutputStream output) throws ChangelogException {
        write(changelog, new OutputStreamWriter(output, StandardCharsets.UTF_8));
    }


    /**
     * @see com.github.toolarium.db.changelog.impl.writer.IChangelogWriter#write(IChangelog, Writer)
     */
    @Override
    public void write(IChangelog changelog, Writer writer) throws ChangelogException {
        try {
            Map<String, Object> root = new LinkedHashMap<>();
            root.put("format", ChangelogConfiguration.FORMAT_VERSION);

            List<Map<String, Object>> changesets = new ArrayList<>();
            for (IChangelogChangeSet changeSet : changelog.getChangelogSets()) {
                changesets.add(ChangelogWriterUtil.getInstance().toChangeSetMap(changeSet));
            }
            root.put("changesets", changesets);

            DumperOptions options = new DumperOptions();
            options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
            options.setPrettyFlow(true);
            options.setDefaultScalarStyle(DumperOptions.ScalarStyle.PLAIN);
            options.setIndent(2);
            options.setIndicatorIndent(0);

            Yaml yaml = new Yaml(options);
            yaml.dump(root, writer);
            writer.flush();

            if (LOG.isDebugEnabled()) {
                LOG.debug("Wrote " + changesets.size() + " changeset(s) to YAML.");
            }
        } catch (Exception e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Failed to write YAML changelog: " + e.getMessage(), e);
            }
            throw new ChangelogException("Failed to write YAML changelog.", e);
        }
    }


    /**
     * @see com.github.toolarium.db.changelog.impl.writer.IChangelogWriter#getFileExtension()
     */
    @Override
    public String getFileExtension() {
        return ChangelogConfiguration.FILE_EXTENSION_DB_YAML;
    }
}
