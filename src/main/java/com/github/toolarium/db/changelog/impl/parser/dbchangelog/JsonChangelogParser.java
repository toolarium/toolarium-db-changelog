/*
 * JsonChangelogParser.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog.impl.parser.dbchangelog;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.toolarium.db.changelog.exception.ChangelogParseException;
import com.github.toolarium.db.changelog.impl.parser.IChangelogParser;
import com.github.toolarium.db.changelog.impl.parser.util.ChangelogEntryParserUtil;
import com.github.toolarium.db.changelog.impl.resolver.IChangelogParameterResolver;
import com.github.toolarium.db.changelog.model.Changelog;
import com.github.toolarium.db.changelog.model.ChangelogChangeSet;
import com.github.toolarium.db.changelog.model.ChangelogChangeSetIdentity;
import com.github.toolarium.db.changelog.model.IChangelog;
import com.github.toolarium.db.changelog.model.IChangelogChangeSet;
import com.github.toolarium.db.changelog.model.IChangelogEntry;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Parses the native DB-Changelog JSON format (.db.json) using Jackson.
 *
 * @author patrick
 */
public class JsonChangelogParser implements IChangelogParser {
    private static final Logger LOG = LoggerFactory.getLogger(JsonChangelogParser.class);
    private static final String FORMAT_PREFIX = "toolarium-db-changelog/";
    private final ObjectMapper objectMapper;


    /**
     * Constructor for JsonChangelogParser
     */
    public JsonChangelogParser() {
        this.objectMapper = new ObjectMapper();
    }


    /**
     * @see com.github.toolarium.db.changelog.impl.parser.IChangelogParser#parse(InputStream, String, IChangelogParameterResolver)
     */
    @Override
    public IChangelog parse(InputStream input, String physicalPath, IChangelogParameterResolver parameterResolver) throws ChangelogParseException {
        try {
            JsonNode root = objectMapper.readTree(input);
            if (root == null || root.isEmpty()) {
                throw new ChangelogParseException("Empty changelog file [" + physicalPath + "].");
            }

            validateFormat(root, physicalPath);
            loadProperties(root, parameterResolver);

            List<IChangelogChangeSet> changeSets = new ArrayList<>();
            JsonNode changesetsNode = root.get("changesets");
            if (changesetsNode != null && changesetsNode.isArray()) {
                for (JsonNode csNode : changesetsNode) {
                    changeSets.add(parseChangeSet(csNode, physicalPath, parameterResolver));
                }
            }

            if (LOG.isDebugEnabled()) {
                LOG.debug("Parsed " + changeSets.size() + " changeset(s) from [" + physicalPath + "].");
            }

            return new Changelog(physicalPath, changeSets);
        } catch (ChangelogParseException e) {
            throw e;
        } catch (Exception e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Failed to parse JSON changelog [" + e.getMessage(), e);
            }
            throw new ChangelogParseException("Failed to parse JSON changelog [" + physicalPath + "].", e);
        }
    }


    /**
     * @see com.github.toolarium.db.changelog.impl.parser.IChangelogParser#supports(String)
     */
    @Override
    public boolean supports(String fileExtension) {
        return ".db.json".equals(fileExtension);
    }


    /**
     * Validate the format field.
     *
     * @param root the root JSON node
     * @param physicalPath the file path for error messages
     * @throws ChangelogParseException if the format is invalid
     */
    private void validateFormat(JsonNode root, String physicalPath) throws ChangelogParseException {
        JsonNode formatNode = root.get("format");
        if (formatNode == null || !formatNode.asText().startsWith(FORMAT_PREFIX)) {
            throw new ChangelogParseException("Invalid or missing 'format' in [" + physicalPath + "].");
        }
    }


    /**
     * Load properties into the parameter resolver.
     *
     * @param root the root JSON node
     * @param parameterResolver the parameter resolver
     */
    private void loadProperties(JsonNode root, IChangelogParameterResolver parameterResolver) {
        JsonNode propsNode = root.get("properties");
        if (propsNode != null && propsNode.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> fields = propsNode.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                parameterResolver.setProperty(entry.getKey(), entry.getValue().asText());
            }
        }
    }


    /**
     * Parse a single changeset from a JSON node.
     *
     * @param csNode the changeset JSON node
     * @param physicalPath the file path
     * @param parameterResolver the parameter resolver
     * @return the parsed changeset
     * @throws ChangelogParseException if parsing fails
     */
    private IChangelogChangeSet parseChangeSet(JsonNode csNode, String physicalPath,
                                               IChangelogParameterResolver parameterResolver) throws ChangelogParseException {
        String id = getRequiredText(csNode, "id", physicalPath);
        String author = getText(csNode, "author", System.getProperty("user.name", "default"));
        String comment = getText(csNode, "comment", null);

        boolean runAlways = getBoolean(csNode, "runAlways", false);
        boolean runOnChange = getBoolean(csNode, "runOnChange", false);
        boolean failOnError = getBoolean(csNode, "failOnError", true);
        boolean runInTransaction = getBoolean(csNode, "runInTransaction", true);

        String filePath = getText(csNode, "filePath", null);

        List<String> contexts = getTextList(csNode, "context");
        List<String> labels = getTextList(csNode, "labels");

        String effectivePath = physicalPath;
        if (filePath != null) {
            effectivePath = filePath;
        }
        ChangelogChangeSetIdentity identity = new ChangelogChangeSetIdentity(id, author, effectivePath);

        List<IChangelogEntry> entries = parseEntries(csNode, "changes", parameterResolver, physicalPath);
        List<IChangelogEntry> rollbackEntries = parseEntries(csNode, "rollback", parameterResolver, physicalPath);

        if (entries.isEmpty()) {
            throw new ChangelogParseException("Changeset [" + id + "] in [" + physicalPath + "] has no changes.");
        }

        ChangelogChangeSet changeSet = new ChangelogChangeSet(identity, entries, rollbackEntries, comment,
                runAlways, runOnChange, failOnError, runInTransaction, contexts, labels, null);
        if (filePath != null) {
            changeSet.setFilePath(filePath);
        }
        return changeSet;
    }


    /**
     * Parse entries from a JSON array.
     *
     * @param csNode the changeset node
     * @param key the key ("changes" or "rollback")
     * @param parameterResolver the parameter resolver
     * @param physicalPath the file path
     * @return the list of entries
     * @throws ChangelogParseException if parsing fails
     */
    private List<IChangelogEntry> parseEntries(JsonNode csNode, String key,
                                               IChangelogParameterResolver parameterResolver,
                                               String physicalPath) throws ChangelogParseException {
        JsonNode arrayNode = csNode.get(key);
        if (arrayNode == null || !arrayNode.isArray()) {
            return Collections.emptyList();
        }

        List<IChangelogEntry> entries = new ArrayList<>();
        for (JsonNode entryNode : arrayNode) {
            Map<String, Object> entryMap = convertNodeToMap(entryNode);
            entries.add(ChangelogEntryParserUtil.getInstance().parseEntry(entryMap, parameterResolver, physicalPath));
        }

        return entries;
    }


    /**
     * Get a required text value.
     *
     * @param node the JSON node
     * @param key the key
     * @param physicalPath the file path for error messages
     * @return the text value
     * @throws ChangelogParseException if the key is missing
     */
    private String getRequiredText(JsonNode node, String key, String physicalPath) throws ChangelogParseException {
        JsonNode valueNode = node.get(key);
        if (valueNode == null) {
            throw new ChangelogParseException("Missing required attribute [" + key + "] in [" + physicalPath + "].");
        }
        return valueNode.asText();
    }


    /**
     * Get an optional text value.
     *
     * @param node the JSON node
     * @param key the key
     * @param defaultValue the default value
     * @return the text value or default
     */
    private String getText(JsonNode node, String key, String defaultValue) {
        JsonNode valueNode = node.get(key);
        if (valueNode == null) {
            return defaultValue;
        }
        return valueNode.asText();
    }


    /**
     * Get an optional boolean value.
     *
     * @param node the JSON node
     * @param key the key
     * @param defaultValue the default value
     * @return the boolean value or default
     */
    private boolean getBoolean(JsonNode node, String key, boolean defaultValue) {
        JsonNode valueNode = node.get(key);
        if (valueNode == null) {
            return defaultValue;
        }
        return valueNode.asBoolean(defaultValue);
    }


    /**
     * Get an optional text list from a JSON array.
     *
     * @param node the JSON node
     * @param key the key
     * @return the text list, or empty list
     */
    private List<String> getTextList(JsonNode node, String key) {
        JsonNode arrayNode = node.get(key);
        if (arrayNode == null) {
            return Collections.emptyList();
        }
        if (arrayNode.isArray()) {
            List<String> result = new ArrayList<>();
            for (JsonNode item : arrayNode) {
                result.add(item.asText());
            }
            return result;
        }
        return Collections.singletonList(arrayNode.asText());
    }


    /**
     * Convert a JSON node to a Map with unchecked suppression in a single place.
     *
     * @param node the JSON node
     * @return the map
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> convertNodeToMap(JsonNode node) {
        return objectMapper.convertValue(node, Map.class);
    }
}
