/*
 * YamlChangelogParser.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog.impl.parser.dbchangelog;

import com.github.toolarium.common.util.EnumUtil;
import com.github.toolarium.db.changelog.exception.ChangelogParseException;
import com.github.toolarium.db.changelog.impl.parser.ChangelogParserFactory;
import com.github.toolarium.db.changelog.impl.parser.IChangelogParser;
import com.github.toolarium.db.changelog.impl.parser.util.ChangelogEntryParserUtil;
import com.github.toolarium.db.changelog.impl.parser.util.ChangelogParserUtil;
import com.github.toolarium.db.changelog.impl.resolver.IChangelogParameterResolver;
import com.github.toolarium.db.changelog.model.Changelog;
import com.github.toolarium.db.changelog.model.ChangelogChangeSet;
import com.github.toolarium.db.changelog.model.ChangelogChangeSetIdentity;
import com.github.toolarium.db.changelog.model.IChangelog;
import com.github.toolarium.db.changelog.model.IChangelogChangeSet;
import com.github.toolarium.db.changelog.model.IChangelogEntry;
import com.github.toolarium.db.changelog.model.precondition.ChangelogPreconditionContainer;
import com.github.toolarium.db.changelog.model.precondition.ChangelogPreconditionFailureAction;
import com.github.toolarium.db.changelog.model.precondition.DbmsPrecondition;
import com.github.toolarium.db.changelog.model.precondition.IChangelogPrecondition;
import com.github.toolarium.db.changelog.model.precondition.TableExistsPrecondition;
import com.github.toolarium.db.changelog.model.sql.SqlChangelogEntry;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;


/**
 * Parses the native DB-Changelog YAML format (.db.yaml).
 *
 * @author patrick
 */
public class YamlChangelogParser implements IChangelogParser {
    private static final Logger LOG = LoggerFactory.getLogger(YamlChangelogParser.class);
    private static final String FORMAT_PREFIX = "toolarium-db-changelog/";
    private static final String BRACKET_DOT = "].";


    /**
     * @see com.github.toolarium.db.changelog.impl.parser.IChangelogParser#parse(InputStream, String, IChangelogParameterResolver)
     */
    @Override
    public IChangelog parse(InputStream input, String physicalPath, IChangelogParameterResolver parameterResolver) throws ChangelogParseException {
        try {
            Yaml yaml = new Yaml(new SafeConstructor(new LoaderOptions()));
            Map<String, Object> root = yaml.load(input);

            if (root == null) {
                throw new ChangelogParseException("Empty changelog file [" + physicalPath + BRACKET_DOT);
            }

            validateFormat(root, physicalPath);
            loadProperties(root, parameterResolver);

            List<IChangelogChangeSet> changeSets = new ArrayList<>();

            // Process includes
            List<Map<String, Object>> includeList = ChangelogParserUtil.getInstance().listOf(root.get("include"));
            if (includeList != null) {
                for (Map<String, Object> includeMap : includeList) {
                    String includePath = (String) includeMap.get("path");
                    if (includePath != null) {
                        changeSets.addAll(resolveInclude(includePath, physicalPath, parameterResolver));
                    }
                }
            }

            List<Map<String, Object>> changesetList = ChangelogParserUtil.getInstance().listOf(root.get("changesets"));
            if (changesetList != null) {
                for (Map<String, Object> changesetMap : changesetList) {
                    changeSets.add(parseChangeSet(changesetMap, physicalPath, parameterResolver));
                }
            }

            if (LOG.isDebugEnabled()) {
                LOG.debug("Parsed " + changeSets.size() + " changeset(s) from [" + physicalPath + BRACKET_DOT);
            }

            return new Changelog(physicalPath, changeSets);
        } catch (ChangelogParseException e) {
            throw e;
        } catch (Exception e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Failed to parse changelog [" + e.getMessage(), e);
            }
            throw new ChangelogParseException("Failed to parse changelog [" + physicalPath + BRACKET_DOT, e);
        }
    }


    /**
     * @see com.github.toolarium.db.changelog.impl.parser.IChangelogParser#supports(String)
     */
    @Override
    public boolean supports(String fileExtension) {
        return ".db.yaml".equals(fileExtension) || ".db.yml".equals(fileExtension);
    }


    /**
     * Resolve an included changelog file.
     *
     * @param includePath the relative path to the included file
     * @param parentPath the parent changelog path
     * @param parameterResolver the parameter resolver
     * @return the changesets from the included file
     * @throws ChangelogParseException if the included file cannot be parsed
     */
    private List<IChangelogChangeSet> resolveInclude(String includePath, String parentPath,
                                                     IChangelogParameterResolver parameterResolver) throws ChangelogParseException {
        String basePath = parentPath;
        if (basePath.startsWith("classpath:")) {
            basePath = basePath.substring("classpath:".length());
        }
        String resolvedPath = resolveRelativePath(basePath, includePath);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Including changelog [" + resolvedPath + "] from [" + parentPath + BRACKET_DOT);
        }

        InputStream input = Thread.currentThread().getContextClassLoader().getResourceAsStream(resolvedPath);
        if (input == null) {
            try {
                input = new FileInputStream(new File(resolvedPath));
            } catch (Exception e) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Included changelog not found [" + e.getMessage(), e);
                }
                throw new ChangelogParseException("Included changelog not found [" + resolvedPath + BRACKET_DOT);
            }
        }

        try {
            ChangelogParserFactory factory = ChangelogParserFactory.getInstance();
            IChangelogParser parser = factory.getParser(resolvedPath);
            IChangelog included = parser.parse(input, resolvedPath, parameterResolver);
            return included.getChangelogSets();
        } finally {
            try {
                input.close();
            } catch (Exception e) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Failed to close included changelog stream: " + e.getMessage(), e);
                }
            }
        }
    }


    /**
     * Resolve a path relative to the parent changelog path.
     *
     * @param parentPath the parent file path
     * @param relativePath the relative path
     * @return the resolved path
     */
    private String resolveRelativePath(String parentPath, String relativePath) {
        int lastSlash = Math.max(parentPath.lastIndexOf('/'), parentPath.lastIndexOf('\\'));
        if (lastSlash >= 0) {
            return parentPath.substring(0, lastSlash + 1) + relativePath;
        }
        return relativePath;
    }


    /**
     * Validate the format marker.
     *
     * @param root the root map
     * @param physicalPath the file path for error messages
     * @throws ChangelogParseException if the format is invalid
     */
    private void validateFormat(Map<String, Object> root, String physicalPath) throws ChangelogParseException {
        Object format = root.get("format");
        if (format == null) {
            throw new ChangelogParseException("Missing 'format' key in [" + physicalPath + BRACKET_DOT);
        }

        String formatStr = format.toString();
        if (!formatStr.startsWith(FORMAT_PREFIX)) {
            throw new ChangelogParseException("Unsupported format [" + formatStr + "] in [" + physicalPath + BRACKET_DOT);
        }
    }


    /**
     * Load properties from the changelog into the parameter resolver.
     *
     * @param root the root map
     * @param parameterResolver the parameter resolver
     */
    private void loadProperties(Map<String, Object> root, IChangelogParameterResolver parameterResolver) {
        Map<String, Object> properties = ChangelogParserUtil.getInstance().mapOf(root.get("properties"));
        if (properties != null) {
            for (Map.Entry<String, Object> entry : properties.entrySet()) {
                if (entry.getValue() != null) {
                    parameterResolver.setProperty(entry.getKey(), entry.getValue().toString());
                }
            }
        }
    }


    /**
     * Parse a single changeset from a map.
     *
     * @param changesetMap the changeset map
     * @param physicalPath the file path
     * @param parameterResolver the parameter resolver
     * @return the parsed changeset
     * @throws ChangelogParseException if parsing fails
     */
    private IChangelogChangeSet parseChangeSet(Map<String, Object> changesetMap, String physicalPath,
                                               IChangelogParameterResolver parameterResolver) throws ChangelogParseException {
        String id = getRequiredString(changesetMap, "id", physicalPath);
        String author = getString(changesetMap, "author", System.getProperty("user.name", "default"));
        String comment = getString(changesetMap, "comment", null);

        boolean runAlways = getBoolean(changesetMap, "runAlways", false);
        boolean runOnChange = getBoolean(changesetMap, "runOnChange", false);
        boolean failOnError = getBoolean(changesetMap, "failOnError", true);
        boolean runInTransaction = getBoolean(changesetMap, "runInTransaction", true);

        String filePath = getString(changesetMap, "filePath", null);

        List<String> contexts = getStringList(changesetMap, "context");
        List<String> labels = getStringList(changesetMap, "labels");
        ChangelogPreconditionContainer preconditions = parsePreconditions(changesetMap);

        String effectivePath = physicalPath;
        if (filePath != null) {
            effectivePath = filePath;
        }
        ChangelogChangeSetIdentity identity = new ChangelogChangeSetIdentity(id, author, effectivePath);

        List<IChangelogEntry> entries = parseEntries(changesetMap, "changes", parameterResolver, physicalPath);
        List<IChangelogEntry> rollbackEntries = parseEntries(changesetMap, "rollback", parameterResolver, physicalPath);

        if (entries.isEmpty()) {
            throw new ChangelogParseException("Changeset [" + id + "] in [" + physicalPath + "] has no changes.");
        }

        ChangelogChangeSet changeSet = new ChangelogChangeSet(identity, entries, rollbackEntries, comment,
                runAlways, runOnChange, failOnError, runInTransaction, contexts, labels, preconditions);
        if (filePath != null) {
            changeSet.setFilePath(filePath);
        }
        return changeSet;
    }


    /**
     * Parse preconditions from a changeset map.
     *
     * @param changesetMap the changeset map
     * @return the precondition container, or null if no preconditions
     */
    private ChangelogPreconditionContainer parsePreconditions(Map<String, Object> changesetMap) {
        Map<String, Object> precondMap = ChangelogParserUtil.getInstance().mapOf(changesetMap.get("preconditions"));
        if (precondMap == null) {
            return null;
        }

        String onFailStr = getString(precondMap, "onFail", "HALT");
        String onErrorStr = getString(precondMap, "onError", "HALT");
        ChangelogPreconditionFailureAction onFail = EnumUtil.getInstance().valueOf(ChangelogPreconditionFailureAction.class, onFailStr);
        if (onFail == null) {
            onFail = ChangelogPreconditionFailureAction.HALT;
        }
        ChangelogPreconditionFailureAction onError = EnumUtil.getInstance().valueOf(ChangelogPreconditionFailureAction.class, onErrorStr);
        if (onError == null) {
            onError = ChangelogPreconditionFailureAction.HALT;
        }

        List<IChangelogPrecondition> checks = new ArrayList<>();
        List<Map<String, Object>> checkList = ChangelogParserUtil.getInstance().listOf(precondMap.get("checks"));
        if (checkList != null) {
            for (Map<String, Object> checkMap : checkList) {
                if (checkMap.containsKey("dbms")) {
                    checks.add(new DbmsPrecondition(checkMap.get("dbms").toString()));
                }
                if (checkMap.containsKey("tableExists")) {
                    checks.add(new TableExistsPrecondition(checkMap.get("tableExists").toString()));
                }
            }
        }

        if (checks.isEmpty()) {
            return null;
        }

        return new ChangelogPreconditionContainer(onFail, onError, checks);
    }


    /**
     * Parse entries (changes or rollback) from a changeset map.
     *
     * @param changesetMap the changeset map
     * @param key the key ("changes" or "rollback")
     * @param parameterResolver the parameter resolver
     * @param physicalPath the file path
     * @return the list of entries
     * @throws ChangelogParseException if parsing fails
     */
    private List<IChangelogEntry> parseEntries(Map<String, Object> changesetMap, String key,
                                               IChangelogParameterResolver parameterResolver,
                                               String physicalPath) throws ChangelogParseException {
        List<Object> changeList = ChangelogParserUtil.getInstance().listOf(changesetMap.get(key));
        if (changeList == null) {
            return Collections.emptyList();
        }

        List<IChangelogEntry> entries = new ArrayList<>();
        for (Object changeObj : changeList) {
            if (changeObj instanceof Map) {
                entries.add(parseEntry(ChangelogParserUtil.getInstance().mapOf(changeObj), parameterResolver, physicalPath));
            } else if (changeObj instanceof String) {
                entries.add(new SqlChangelogEntry(parameterResolver.resolve((String) changeObj)));
            }
        }

        return entries;
    }


    /**
     * Parse a single changelog entry from a map.
     *
     * @param entryMap the entry map (e.g., {"sql": "..."}, {"createTable": {...}})
     * @param parameterResolver the parameter resolver
     * @param physicalPath the file path
     * @return the parsed entry
     * @throws ChangelogParseException if the entry type is unknown
     */
    private IChangelogEntry parseEntry(Map<String, Object> entryMap, IChangelogParameterResolver parameterResolver,
                                       String physicalPath) throws ChangelogParseException {
        return ChangelogEntryParserUtil.getInstance().parseEntry(entryMap, parameterResolver, physicalPath);
    }


    /**
     * Get a required string value from a map.
     *
     * @param map the map
     * @param key the key
     * @param physicalPath the file path for error messages
     * @return the string value
     * @throws ChangelogParseException if the key is missing
     */
    private String getRequiredString(Map<String, Object> map, String key, String physicalPath) throws ChangelogParseException {
        Object value = map.get(key);
        if (value == null) {
            throw new ChangelogParseException("Missing required attribute [" + key + "] in [" + physicalPath + BRACKET_DOT);
        }
        return value.toString();
    }


    /**
     * Get an optional string value from a map.
     *
     * @param map the map
     * @param key the key
     * @param defaultValue the default value
     * @return the string value or default
     */
    private String getString(Map<String, Object> map, String key, String defaultValue) {
        Object value = map.get(key);
        if (value == null) {
            return defaultValue;
        }
        return value.toString();
    }


    /**
     * Get an optional boolean value from a map.
     *
     * @param map the map
     * @param key the key
     * @param defaultValue the default value
     * @return the boolean value or default
     */
    private boolean getBoolean(Map<String, Object> map, String key, boolean defaultValue) {
        Object value = map.get(key);
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        return Boolean.parseBoolean(value.toString());
    }


    /**
     * Get an optional string list value from a map.
     *
     * @param map the map
     * @param key the key
     * @return the string list, or empty list if not present
     */
    private List<String> getStringList(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            return Collections.emptyList();
        }
        if (value instanceof List) {
            List<String> result = new ArrayList<>();
            List<Object> items = ChangelogParserUtil.getInstance().listOf(value);
            for (Object item : items) {
                result.add(item.toString());
            }
            return result;
        }
        return Collections.singletonList(value.toString());
    }
}
