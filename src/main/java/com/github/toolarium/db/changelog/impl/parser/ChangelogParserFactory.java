/*
 * ChangelogParserFactory.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog.impl.parser;

import com.github.toolarium.db.changelog.exception.ChangelogParseException;
import com.github.toolarium.db.changelog.impl.parser.dbchangelog.JsonChangelogParser;
import com.github.toolarium.db.changelog.impl.parser.dbchangelog.XmlChangelogParser;
import com.github.toolarium.db.changelog.impl.parser.dbchangelog.YamlChangelogParser;
import com.github.toolarium.db.changelog.impl.parser.liquibase.LiquibaseJsonChangelogParser;
import com.github.toolarium.db.changelog.impl.parser.liquibase.LiquibaseSqlChangelogParser;
import com.github.toolarium.db.changelog.impl.parser.liquibase.LiquibaseXmlChangelogParser;
import com.github.toolarium.db.changelog.impl.parser.liquibase.LiquibaseYamlChangelogParser;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Singleton factory that selects the appropriate parser based on file extension.
 * Parsers are registered in priority order: DB-Changelog format first, then external formats.
 *
 * @author patrick
 */
public final class ChangelogParserFactory {
    private static final Logger LOG = LoggerFactory.getLogger(ChangelogParserFactory.class);


    /**
     * Private class, the only instance of the singleton which will be created by accessing the holder class.
     *
     * @author patrick
     */
    private static final class HOLDER {
        static final ChangelogParserFactory INSTANCE = new ChangelogParserFactory();
    }

    private final List<IChangelogParser> parsers;


    /**
     * Private constructor
     */
    private ChangelogParserFactory() {
        this.parsers = new ArrayList<>();
        registerDefaultParsers();
    }


    /**
     * Get the instance.
     *
     * @return the instance
     */
    public static ChangelogParserFactory getInstance() {
        return HOLDER.INSTANCE;
    }


    /**
     * Get the parser for the given file path.
     *
     * @param filePath the file path
     * @return the matching parser
     * @throws ChangelogParseException if no parser supports the file
     */
    public IChangelogParser getParser(String filePath) throws ChangelogParseException {
        String extension = getExtension(filePath);

        for (IChangelogParser parser : parsers) {
            if (parser.supports(extension)) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Selected parser [" + parser.getClass().getSimpleName() + "] for [" + filePath + "].");
                }
                return parser;
            }
        }

        throw new ChangelogParseException("No parser found for file [" + filePath + "] with extension [" + extension + "].");
    }


    /**
     * Register an additional parser.
     *
     * @param parser the parser to register
     */
    public void registerParser(IChangelogParser parser) {
        parsers.add(parser);
    }


    /**
     * Register the default parsers.
     */
    private void registerDefaultParsers() {
        // DB-Changelog format parsers
        parsers.add(new YamlChangelogParser());
        parsers.add(new XmlChangelogParser());
        parsers.add(new JsonChangelogParser());

        // External format parsers
        parsers.add(new LiquibaseXmlChangelogParser());
        parsers.add(new LiquibaseYamlChangelogParser());
        parsers.add(new LiquibaseJsonChangelogParser());
        parsers.add(new LiquibaseSqlChangelogParser());
    }


    /**
     * Extract the file extension from a path. Handles compound extensions like ".db.yaml".
     *
     * @param filePath the file path
     * @return the extension (e.g., ".db.yaml", ".xml")
     */
    private String getExtension(String filePath) {
        if (filePath == null) {
            return "";
        }

        String name = filePath;
        int lastSlash = Math.max(name.lastIndexOf('/'), name.lastIndexOf('\\'));
        if (lastSlash >= 0) {
            name = name.substring(lastSlash + 1);
        }

        // Check for compound extension ".db.yaml" / ".db.yml" / ".db.xml" / ".db.json"
        if (name.contains(".db.")) {
            int dbIndex = name.indexOf(".db.");
            return name.substring(dbIndex);
        }

        int dotIndex = name.lastIndexOf('.');
        if (dotIndex >= 0) {
            return name.substring(dotIndex);
        }

        return "";
    }
}
