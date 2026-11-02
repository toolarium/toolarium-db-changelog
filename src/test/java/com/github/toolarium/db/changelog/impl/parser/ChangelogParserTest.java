/*
 * ChangelogParserTest.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog.impl.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.toolarium.db.changelog.ChangelogRunnerBuilder;
import com.github.toolarium.db.changelog.IChangelogRunner;
import com.github.toolarium.db.changelog.exception.ChangelogParseException;
import com.github.toolarium.db.changelog.impl.resolver.impl.ChangelogParameterResolverImpl;
import com.github.toolarium.db.changelog.model.ChangelogExecutionType;
import com.github.toolarium.db.changelog.model.IChangelog;
import com.github.toolarium.db.changelog.model.IChangelogChangeSet;
import com.github.toolarium.db.changelog.model.IChangelogTrackingEntry;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


/**
 * Tests for changelog parsing.
 *
 * @author patrick
 */
public class ChangelogParserTest {
    private static final String TEST_CHANGELOG_PATH = "db/changelog/test-changelog.db.yaml";
    private JdbcDataSource dataSource;


    /**
     * Set up a fresh H2 in-memory database for each test.
     */
    @BeforeEach
    void setUp() {
        dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:parser_test_" + System.nanoTime() + ";DB_CLOSE_DELAY=-1");
        dataSource.setUser("sa");
        dataSource.setPassword("");
    }


    /**
     * Test parsing a YAML changelog from classpath.
     *
     * @throws Exception on error
     */
    @Test
    void testParseYamlChangelog() throws Exception {
        ChangelogParserFactory factory = ChangelogParserFactory.getInstance();
        IChangelogParser parser = factory.getParser(TEST_CHANGELOG_PATH);
        assertNotNull(parser);

        InputStream input = getClass().getClassLoader().getResourceAsStream(TEST_CHANGELOG_PATH);
        assertNotNull(input, "Test changelog should be on classpath");

        IChangelog changelog = parser.parse(input, TEST_CHANGELOG_PATH, new ChangelogParameterResolverImpl());
        assertNotNull(changelog);
        assertEquals(3, changelog.getChangelogSets().size());

        IChangelogChangeSet first = changelog.getChangelogSets().get(0);
        assertEquals("create-users-table", first.getIdentity().getId());
        assertEquals("testauthor", first.getIdentity().getAuthor());
        assertEquals("Create the users table", first.getComment());
        assertFalse(first.getContexts().isEmpty());
        assertTrue(first.getContexts().contains("dev"));
        assertFalse(first.getRollbackEntries().isEmpty());
    }


    /**
     * Test that property substitution works in parsed changelogs.
     *
     * @throws Exception on error
     */
    @Test
    void testPropertySubstitution() throws Exception {
        ChangelogParserFactory factory = ChangelogParserFactory.getInstance();
        IChangelogParser parser = factory.getParser(TEST_CHANGELOG_PATH);

        InputStream input = getClass().getClassLoader().getResourceAsStream(TEST_CHANGELOG_PATH);
        IChangelog changelog = parser.parse(input, TEST_CHANGELOG_PATH, new ChangelogParameterResolverImpl());

        // The SQL should have ${table.prefix} resolved to "test_"
        IChangelogChangeSet first = changelog.getChangelogSets().get(0);
        String description = first.getEntries().get(0).getDescription();
        assertTrue(description.contains("test_users"), "Property should be resolved: " + description);
        assertFalse(description.contains("${table.prefix}"), "Placeholder should be resolved: " + description);
    }


    /**
     * Test that default author is used when not specified.
     *
     * @throws Exception on error
     */
    @Test
    void testDefaultAuthor() throws Exception {
        ChangelogParserFactory factory = ChangelogParserFactory.getInstance();
        IChangelogParser parser = factory.getParser(TEST_CHANGELOG_PATH);

        InputStream input = getClass().getClassLoader().getResourceAsStream(TEST_CHANGELOG_PATH);
        IChangelog changelog = parser.parse(input, TEST_CHANGELOG_PATH, new ChangelogParameterResolverImpl());

        // Second changeset has no author specified, should default to OS username
        IChangelogChangeSet second = changelog.getChangelogSets().get(1);
        assertNotNull(second.getIdentity().getAuthor());
        assertFalse(second.getIdentity().getAuthor().isEmpty());
    }


    /**
     * Test format validation rejects missing format key.
     *
     * @throws Exception on error
     */
    @Test
    void testRejectsMissingFormat() throws Exception {
        String yaml = "changesets:\n  - id: test\n    changes:\n      - sql: \"SELECT 1\"\n";
        InputStream input = new ByteArrayInputStream(yaml.getBytes(StandardCharsets.UTF_8));

        ChangelogParserFactory factory = ChangelogParserFactory.getInstance();
        IChangelogParser parser = factory.getParser("test.db.yaml");

        assertThrows(ChangelogParseException.class, () ->
                parser.parse(input, "test.db.yaml", new ChangelogParameterResolverImpl()));
    }


    /**
     * Test format validation rejects wrong format.
     *
     * @throws Exception on error
     */
    @Test
    void testRejectsWrongFormat() throws Exception {
        String yaml = "format: wrong-format/1.0\nchangesets:\n  - id: test\n    changes:\n      - sql: \"SELECT 1\"\n";
        InputStream input = new ByteArrayInputStream(yaml.getBytes(StandardCharsets.UTF_8));

        ChangelogParserFactory factory = ChangelogParserFactory.getInstance();
        IChangelogParser parser = factory.getParser("test.db.yaml");

        assertThrows(ChangelogParseException.class, () ->
                parser.parse(input, "test.db.yaml", new ChangelogParameterResolverImpl()));
    }


    /**
     * Test parser factory selects correct parser by extension.
     *
     * @throws Exception on error
     */
    @Test
    void testParserFactoryExtensionDetection() throws Exception {
        ChangelogParserFactory factory = ChangelogParserFactory.getInstance();

        assertNotNull(factory.getParser("changelog.db.yaml"));
        assertNotNull(factory.getParser("changelog.db.yml"));
        assertNotNull(factory.getParser("changelog.db.xml"));
        assertNotNull(factory.getParser("changelog.db.json"));
        assertNotNull(factory.getParser("changelog.sql"));
        assertNotNull(factory.getParser("path/to/changelog.db.yaml"));

        assertThrows(ChangelogParseException.class, () -> factory.getParser("changelog.unknown"));
    }


    /**
     * Test parser factory is singleton.
     *
     * @throws Exception on error
     */
    @Test
    void testParserFactorySingleton() throws Exception {
        assertSame(ChangelogParserFactory.getInstance(), ChangelogParserFactory.getInstance());
    }


    /**
     * Test end-to-end: parse changelog from classpath and execute against H2.
     *
     * @throws Exception on error
     */
    @Test
    void testParseAndExecute() throws Exception {
        IChangelogRunner runner = ChangelogRunnerBuilder.builder()
                .dataSource(dataSource)
                .changelog("classpath:" + TEST_CHANGELOG_PATH)
                .build();

        runner.update();

        // Verify the table was created with the resolved property (test_users)
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT name FROM test_users WHERE id = 1")) {
            assertTrue(rs.next(), "Should have one row");
            assertEquals("Admin", rs.getString("name"));
        }

        // Verify all 3 changesets recorded
        List<IChangelogTrackingEntry> history = runner.history();
        assertEquals(3, history.size());
        assertEquals("create-users-table", history.get(0).getId());
        assertEquals("insert-admin", history.get(1).getId());
        assertEquals("add-phone-column", history.get(2).getId());
        assertEquals(ChangelogExecutionType.EXECUTED, history.get(0).getExecutionType());
    }


    /**
     * Test end-to-end: parse XML changelog and execute against H2.
     *
     * @throws Exception on error
     */
    @Test
    void testParseXmlAndExecute() throws Exception {
        IChangelogRunner runner = ChangelogRunnerBuilder.builder()
                .dataSource(dataSource)
                .changelog("classpath:db/changelog/test-changelog.db.xml")
                .build();

        runner.update();

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT name FROM xmltest_items WHERE id = 1")) {
            assertTrue(rs.next(), "Should have one row");
            assertEquals("TestItem", rs.getString("name"));
        }

        List<IChangelogTrackingEntry> history = runner.history();
        assertEquals(2, history.size());
        assertEquals("create-table", history.get(0).getId());
        assertEquals("insert-item", history.get(1).getId());
    }


    /**
     * Test end-to-end: parse JSON changelog and execute against H2.
     *
     * @throws Exception on error
     */
    @Test
    void testParseJsonAndExecute() throws Exception {
        IChangelogRunner runner = ChangelogRunnerBuilder.builder()
                .dataSource(dataSource)
                .changelog("classpath:db/changelog/test-changelog.db.json")
                .build();

        runner.update();

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT name FROM jsontest_products WHERE id = 1")) {
            assertTrue(rs.next(), "Should have one row");
            assertEquals("TestProduct", rs.getString("name"));
        }

        List<IChangelogTrackingEntry> history = runner.history();
        assertEquals(2, history.size());
        assertEquals("create-table", history.get(0).getId());
    }


    /**
     * Test end-to-end: parse SQL formatted changelog and execute against H2.
     *
     * @throws Exception on error
     */
    @Test
    void testParseSqlAndExecute() throws Exception {
        IChangelogRunner runner = ChangelogRunnerBuilder.builder()
                .dataSource(dataSource)
                .changelog("classpath:db/changelog/liquibase/test-changelog.sql")
                .build();

        runner.update();

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT product FROM orders WHERE id = 1")) {
            assertTrue(rs.next(), "Should have one row");
            assertEquals("Widget", rs.getString("product"));
        }

        List<IChangelogTrackingEntry> history = runner.history();
        assertEquals(2, history.size());
        assertEquals("create-orders", history.get(0).getId());
        assertEquals("dev", history.get(0).getAuthor());
    }


    /**
     * Test include resolution: master changelog includes another file.
     *
     * @throws Exception on error
     */
    @Test
    void testIncludeResolution() throws Exception {
        IChangelogRunner runner = ChangelogRunnerBuilder.builder()
                .dataSource(dataSource)
                .changelog("classpath:db/changelog/master-with-include.db.yaml")
                .build();

        runner.update();

        List<IChangelogTrackingEntry> history = runner.history();
        assertEquals(2, history.size());
        assertEquals("included-changeset", history.get(0).getId());
        assertEquals("master-changeset", history.get(1).getId());
    }


    /**
     * Test parameter resolver.
     */
    @Test
    void testParameterResolver() {
        ChangelogParameterResolverImpl resolver = new ChangelogParameterResolverImpl();
        resolver.setProperty("name", "world");
        resolver.setProperty("prefix", "app_");

        assertEquals("hello world", resolver.resolve("hello ${name}"));
        assertEquals("app_users", resolver.resolve("${prefix}users"));
        assertEquals("no placeholders", resolver.resolve("no placeholders"));
        assertEquals(null, resolver.resolve(null));
        assertTrue(resolver.hasProperty("name"));
        assertFalse(resolver.hasProperty("missing"));
    }
}
