/*
 * LiquibaseYamlChangelogParser.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog.impl.parser.liquibase;

import com.github.toolarium.db.changelog.exception.ChangelogParseException;
import com.github.toolarium.db.changelog.impl.parser.IChangelogParser;
import com.github.toolarium.db.changelog.impl.parser.util.ChangelogParserUtil;
import com.github.toolarium.db.changelog.impl.resolver.IChangelogParameterResolver;
import com.github.toolarium.db.changelog.model.Changelog;
import com.github.toolarium.db.changelog.model.ChangelogChangeSet;
import com.github.toolarium.db.changelog.model.ChangelogChangeSetIdentity;
import com.github.toolarium.db.changelog.model.IChangelog;
import com.github.toolarium.db.changelog.model.IChangelogChangeSet;
import com.github.toolarium.db.changelog.model.IChangelogEntry;
import com.github.toolarium.db.changelog.model.sql.SqlChangelogEntry;
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
 * Parses external YAML changelog format (databaseChangeLog root key).
 *
 * @author patrick
 */
public class LiquibaseYamlChangelogParser implements IChangelogParser {
    private static final Logger LOG = LoggerFactory.getLogger(LiquibaseYamlChangelogParser.class);
    private static final String KEY_SQL = "sql";


    /**
     * @see com.github.toolarium.db.changelog.impl.parser.IChangelogParser#parse(InputStream, String, IChangelogParameterResolver)
     */
    @Override
    public IChangelog parse(InputStream input, String physicalPath, IChangelogParameterResolver parameterResolver) throws ChangelogParseException {
        try {
            Yaml yaml = new Yaml(new SafeConstructor(new LoaderOptions()));
            Map<String, Object> root = yaml.load(input);

            if (root == null || !root.containsKey("databaseChangeLog")) {
                throw new ChangelogParseException("Not a valid external YAML changelog [" + physicalPath + "].");
            }

            List<Map<String, Object>> changeLogList = ChangelogParserUtil.getInstance().listOf(root.get("databaseChangeLog"));
            if (changeLogList == null) {
                return new Changelog(physicalPath, Collections.emptyList());
            }

            List<IChangelogChangeSet> changeSets = new ArrayList<>();
            for (Map<String, Object> entry : changeLogList) {
                if (entry.containsKey("changeSet")) {
                    Map<String, Object> csMap = ChangelogParserUtil.getInstance().mapOf(entry.get("changeSet"));
                    if (csMap != null) {
                        changeSets.add(parseChangeSet(csMap, physicalPath, parameterResolver));
                    }
                }
            }

            if (LOG.isDebugEnabled()) {
                LOG.debug("Parsed " + changeSets.size() + " changeset(s) from external YAML [" + physicalPath + "].");
            }

            return new Changelog(physicalPath, changeSets);
        } catch (ChangelogParseException e) {
            throw e;
        } catch (Exception e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Failed to parse external YAML changelog: " + e.getMessage(), e);
            }
            throw new ChangelogParseException("Failed to parse external YAML changelog [" + physicalPath + "].", e);
        }
    }


    /**
     * @see com.github.toolarium.db.changelog.impl.parser.IChangelogParser#supports(String)
     */
    @Override
    public boolean supports(String fileExtension) {
        return ".yaml".equals(fileExtension) || ".yml".equals(fileExtension);
    }


    /**
     * Parse a single changeset from an external YAML map.
     *
     * @param csMap the changeset map
     * @param physicalPath the file path
     * @param parameterResolver the parameter resolver
     * @return the parsed changeset
     */
    private IChangelogChangeSet parseChangeSet(Map<String, Object> csMap, String physicalPath,
                                               IChangelogParameterResolver parameterResolver) {
        String id = "unknown";
        if (csMap.get("id") != null) {
            id = csMap.get("id").toString();
        }
        String author = System.getProperty("user.name", "default");
        if (csMap.get("author") != null) {
            author = csMap.get("author").toString();
        }

        ChangelogChangeSetIdentity identity = new ChangelogChangeSetIdentity(id, author, physicalPath);
        List<IChangelogEntry> entries = new ArrayList<>();

        List<Map<String, Object>> changes = ChangelogParserUtil.getInstance().listOf(csMap.get("changes"));
        if (changes != null) {
            for (Map<String, Object> change : changes) {
                extractSqlEntry(change, parameterResolver, entries);
            }
        }

        List<IChangelogEntry> rollbackEntries = Collections.emptyList();
        List<Map<String, Object>> rollbacks = ChangelogParserUtil.getInstance().listOf(csMap.get("rollback"));
        if (rollbacks != null) {
            rollbackEntries = new ArrayList<>();
            for (Map<String, Object> rb : rollbacks) {
                extractSqlEntry(rb, parameterResolver, rollbackEntries);
            }
        }

        return new ChangelogChangeSet(identity, entries, rollbackEntries, null,
                false, false, true, true, Collections.emptyList(), Collections.emptyList(), null);
    }


    /**
     * Extract a SQL entry from a change map and add it to the list.
     *
     * @param change the change map
     * @param parameterResolver the parameter resolver
     * @param entries the list to add to
     */
    private void extractSqlEntry(Map<String, Object> change, IChangelogParameterResolver parameterResolver,
                                 List<IChangelogEntry> entries) {
        if (!change.containsKey(KEY_SQL)) {
            return;
        }
        Map<String, Object> sqlMap = ChangelogParserUtil.getInstance().mapOf(change.get(KEY_SQL));
        if (sqlMap != null && sqlMap.containsKey(KEY_SQL)) {
            entries.add(new SqlChangelogEntry(parameterResolver.resolve(sqlMap.get(KEY_SQL).toString())));
        }
    }
}
