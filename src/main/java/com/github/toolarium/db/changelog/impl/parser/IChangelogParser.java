/*
 * IChangelogParser.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog.impl.parser;


import com.github.toolarium.db.changelog.dto.IChangelog;
import com.github.toolarium.db.changelog.exception.ChangelogParseException;
import com.github.toolarium.db.changelog.impl.resolver.IChangelogParameterResolver;
import java.io.InputStream;

/**
 * Parses a changelog from an input stream into the internal model.
 *
 * @author patrick
 */
public interface IChangelogParser {

    /**
     * Parse a changelog from an input stream.
     *
     * @param input the input stream
     * @param physicalPath the physical file path (used for identity and relative includes)
     * @param parameterResolver the parameter resolver for property substitution
     * @return the parsed changelog
     * @throws ChangelogParseException if parsing fails
     */
    IChangelog parse(InputStream input, String physicalPath, IChangelogParameterResolver parameterResolver) throws ChangelogParseException;


    /**
     * Check if this parser supports the given file extension.
     *
     * @param fileExtension the file extension (e.g., ".db.yaml", ".xml")
     * @return true if this parser can handle the extension
     */
    boolean supports(String fileExtension);
}
