/*
 * LiquibaseExternalParserTest.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog.impl.parser.liquibase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.github.toolarium.db.changelog.impl.resolver.impl.ChangelogParameterResolverImpl;
import com.github.toolarium.db.changelog.model.IChangelog;
import com.github.toolarium.db.changelog.model.IChangelogChangeSet;
import java.io.InputStream;
import org.junit.jupiter.api.Test;


/**
 * Tests for external format parsers (YAML, XML, JSON).
 *
 * @author patrick
 */
public class LiquibaseExternalParserTest {
    private static final String TEST_AUTHOR = "testauthor";
    private static final String CHANGESET_ID = "ext-create-table";


    /**
     * Test parsing external YAML format.
     *
     * @throws Exception on error
     */
    @Test
    void testParseExternalYaml() throws Exception {
        LiquibaseYamlChangelogParser parser = new LiquibaseYamlChangelogParser();
        InputStream input = getClass().getClassLoader().getResourceAsStream("db/changelog/external-test.yaml");
        assertNotNull(input);

        IChangelog changelog = parser.parse(input, "external-test.yaml", new ChangelogParameterResolverImpl());
        assertNotNull(changelog);
        assertEquals(2, changelog.getChangelogSets().size());

        IChangelogChangeSet first = changelog.getChangelogSets().get(0);
        assertEquals(CHANGESET_ID, first.getIdentity().getId());
        assertEquals(TEST_AUTHOR, first.getIdentity().getAuthor());
        assertFalse(first.getEntries().isEmpty());
        assertFalse(first.getRollbackEntries().isEmpty());
    }


    /**
     * Test parsing external XML format.
     *
     * @throws Exception on error
     */
    @Test
    void testParseExternalXml() throws Exception {
        LiquibaseXmlChangelogParser parser = new LiquibaseXmlChangelogParser();
        InputStream input = getClass().getClassLoader().getResourceAsStream("db/changelog/external-test.xml");
        assertNotNull(input);

        IChangelog changelog = parser.parse(input, "external-test.xml", new ChangelogParameterResolverImpl());
        assertNotNull(changelog);
        assertEquals(2, changelog.getChangelogSets().size());

        IChangelogChangeSet first = changelog.getChangelogSets().get(0);
        assertEquals(CHANGESET_ID, first.getIdentity().getId());
        assertEquals(TEST_AUTHOR, first.getIdentity().getAuthor());
        assertFalse(first.getEntries().isEmpty());
        assertFalse(first.getRollbackEntries().isEmpty());
    }


    /**
     * Test parsing external JSON format.
     *
     * @throws Exception on error
     */
    @Test
    void testParseExternalJson() throws Exception {
        LiquibaseJsonChangelogParser parser = new LiquibaseJsonChangelogParser();
        InputStream input = getClass().getClassLoader().getResourceAsStream("db/changelog/external-test.json");
        assertNotNull(input);

        IChangelog changelog = parser.parse(input, "external-test.json", new ChangelogParameterResolverImpl());
        assertNotNull(changelog);
        assertEquals(2, changelog.getChangelogSets().size());

        IChangelogChangeSet first = changelog.getChangelogSets().get(0);
        assertEquals(CHANGESET_ID, first.getIdentity().getId());
        assertEquals(TEST_AUTHOR, first.getIdentity().getAuthor());
        assertFalse(first.getEntries().isEmpty());
        assertFalse(first.getRollbackEntries().isEmpty());
    }


    /**
     * Test supports method for external parsers.
     */
    @Test
    void testSupports() {
        LiquibaseYamlChangelogParser yamlParser = new LiquibaseYamlChangelogParser();
        assertEquals(true, yamlParser.supports(".yaml"));
        assertEquals(true, yamlParser.supports(".yml"));
        assertEquals(false, yamlParser.supports(".db.yaml"));

        LiquibaseXmlChangelogParser xmlParser = new LiquibaseXmlChangelogParser();
        assertEquals(true, xmlParser.supports(".xml"));
        assertEquals(false, xmlParser.supports(".db.xml"));

        LiquibaseJsonChangelogParser jsonParser = new LiquibaseJsonChangelogParser();
        assertEquals(true, jsonParser.supports(".json"));
        assertEquals(false, jsonParser.supports(".db.json"));
    }
}
