/*
 * IChangelogWriter.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog.impl.writer;

import com.github.toolarium.db.changelog.exception.ChangelogException;
import com.github.toolarium.db.changelog.model.IChangelog;
import java.io.OutputStream;
import java.io.Writer;


/**
 * Writes a changelog model to a specific format (YAML, XML, JSON).
 *
 * @author patrick
 */
public interface IChangelogWriter {

    /**
     * Write the changelog to an output stream.
     *
     * @param changelog the changelog to write
     * @param output the output stream
     * @throws ChangelogException if writing fails
     */
    void write(IChangelog changelog, OutputStream output) throws ChangelogException;


    /**
     * Write the changelog to a writer.
     *
     * @param changelog the changelog to write
     * @param writer the writer
     * @throws ChangelogException if writing fails
     */
    void write(IChangelog changelog, Writer writer) throws ChangelogException;


    /**
     * Get the file extension this writer produces.
     *
     * @return the file extension (e.g., ".db.yaml")
     */
    String getFileExtension();
}
