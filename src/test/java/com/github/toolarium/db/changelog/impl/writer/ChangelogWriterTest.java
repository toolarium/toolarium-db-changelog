/*
 * ChangelogWriterTest.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog.impl.writer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.toolarium.db.changelog.impl.parser.dbchangelog.JsonChangelogParser;
import com.github.toolarium.db.changelog.impl.parser.dbchangelog.YamlChangelogParser;
import com.github.toolarium.db.changelog.impl.resolver.impl.ChangelogParameterResolverImpl;
import com.github.toolarium.db.changelog.impl.writer.impl.JsonChangelogWriterImpl;
import com.github.toolarium.db.changelog.impl.writer.impl.XmlChangelogWriterImpl;
import com.github.toolarium.db.changelog.impl.writer.impl.YamlChangelogWriterImpl;
import com.github.toolarium.db.changelog.model.Changelog;
import com.github.toolarium.db.changelog.model.ChangelogChangeSet;
import com.github.toolarium.db.changelog.model.ChangelogChangeSetIdentity;
import com.github.toolarium.db.changelog.model.IChangelog;
import com.github.toolarium.db.changelog.model.IChangelogChangeSet;
import com.github.toolarium.db.changelog.model.IChangelogEntry;
import com.github.toolarium.db.changelog.model.column.AddColumnChangelogEntry;
import com.github.toolarium.db.changelog.model.column.ChangelogColumnDefinition;
import com.github.toolarium.db.changelog.model.sql.SqlChangelogEntry;
import com.github.toolarium.db.changelog.model.table.CreateTableChangelogEntry;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;


/**
 * Round-trip tests for changelog writers: write then parse back.
 *
 * @author patrick
 */
public class ChangelogWriterTest {
    private static final String CS_CREATE_TABLE = "create-table";
    private static final String TEST_AUTHOR = "testauthor";
    private static final String TEST_FILE = "test.db.yaml";

    /**
     * Test YAML round-trip with SQL entries.
     *
     * @throws Exception on error
     */
    @Test
    void testYamlRoundTripSql() throws Exception {
        IChangelog original = createSqlChangelog();
        YamlChangelogWriterImpl writer = new YamlChangelogWriterImpl();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        writer.write(original, out);
        String yaml = out.toString(StandardCharsets.UTF_8.name());

        assertNotNull(yaml);
        assertTrue(yaml.contains("toolarium-db-changelog/1.0"));
        assertTrue(yaml.contains(CS_CREATE_TABLE));

        // Parse back
        YamlChangelogParser parser = new YamlChangelogParser();
        IChangelog parsed = parser.parse(
                new ByteArrayInputStream(yaml.getBytes(StandardCharsets.UTF_8)),
                "round-trip.db.yaml",
                new ChangelogParameterResolverImpl());

        assertEquals(2, parsed.getChangelogSets().size());
        assertEquals(CS_CREATE_TABLE, parsed.getChangelogSets().get(0).getIdentity().getId());
        assertEquals("insert-data", parsed.getChangelogSets().get(1).getIdentity().getId());
    }


    /**
     * Test YAML round-trip with DDL entries.
     *
     * @throws Exception on error
     */
    @Test
    void testYamlRoundTripDdl() throws Exception {
        IChangelog original = createDdlChangelog();
        YamlChangelogWriterImpl writer = new YamlChangelogWriterImpl();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        writer.write(original, out);
        String yaml = out.toString(StandardCharsets.UTF_8.name());

        assertTrue(yaml.contains("createTable"));
        assertTrue(yaml.contains("tableName: orders"));
        assertTrue(yaml.contains("addColumn"));

        // Parse back
        YamlChangelogParser parser = new YamlChangelogParser();
        IChangelog parsed = parser.parse(
                new ByteArrayInputStream(yaml.getBytes(StandardCharsets.UTF_8)),
                "round-trip-ddl.db.yaml",
                new ChangelogParameterResolverImpl());

        assertEquals(2, parsed.getChangelogSets().size());

        // Verify the createTable was preserved
        IChangelogChangeSet firstCs = parsed.getChangelogSets().get(0);
        assertEquals("ddl-1", firstCs.getIdentity().getId());
        IChangelogEntry firstEntry = firstCs.getEntries().get(0);
        assertTrue(firstEntry instanceof CreateTableChangelogEntry);
        CreateTableChangelogEntry createTable = (CreateTableChangelogEntry) firstEntry;
        assertEquals("orders", createTable.getTableName());
        assertEquals(2, createTable.getColumns().size());
    }


    /**
     * Test YAML writer with Writer instead of OutputStream.
     *
     * @throws Exception on error
     */
    @Test
    void testYamlWriterWithWriter() throws Exception {
        IChangelog original = createSqlChangelog();
        YamlChangelogWriterImpl writer = new YamlChangelogWriterImpl();

        StringWriter sw = new StringWriter();
        writer.write(original, sw);
        String yaml = sw.toString();

        assertNotNull(yaml);
        assertTrue(yaml.contains("toolarium-db-changelog/1.0"));
    }


    /**
     * Test YAML writer preserves changeset attributes.
     *
     * @throws Exception on error
     */
    @Test
    void testYamlRoundTripAttributes() throws Exception {
        IChangelog original = createChangelogWithAttributes();
        YamlChangelogWriterImpl writer = new YamlChangelogWriterImpl();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        writer.write(original, out);
        String yaml = out.toString(StandardCharsets.UTF_8.name());

        assertTrue(yaml.contains("runAlways: true"));
        assertTrue(yaml.contains("comment: Test comment"));

        // Parse back
        YamlChangelogParser parser = new YamlChangelogParser();
        IChangelog parsed = parser.parse(
                new ByteArrayInputStream(yaml.getBytes(StandardCharsets.UTF_8)),
                "attrs.db.yaml",
                new ChangelogParameterResolverImpl());

        IChangelogChangeSet cs = parsed.getChangelogSets().get(0);
        assertTrue(cs.isRunAlways());
        assertTrue(cs.isRunOnChange());
        assertFalse(cs.isFailOnError());
        assertEquals("Test comment", cs.getComment());
    }


    /**
     * Test YAML writer preserves rollback entries.
     *
     * @throws Exception on error
     */
    @Test
    void testYamlRoundTripRollback() throws Exception {
        ChangelogChangeSetIdentity identity = new ChangelogChangeSetIdentity("rb-1", "author", TEST_FILE);
        List<IChangelogEntry> entries = Collections.singletonList(new SqlChangelogEntry("CREATE TABLE rb_test (id INT)"));
        List<IChangelogEntry> rollback = Collections.singletonList(new SqlChangelogEntry("DROP TABLE rb_test"));
        ChangelogChangeSet cs = new ChangelogChangeSet(identity, entries, rollback, null,
                false, false, true, true, Collections.emptyList(), Collections.emptyList(), null);
        IChangelog original = new Changelog(TEST_FILE, Collections.singletonList(cs));

        YamlChangelogWriterImpl writer = new YamlChangelogWriterImpl();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        writer.write(original, out);
        String yaml = out.toString(StandardCharsets.UTF_8.name());

        assertTrue(yaml.contains("rollback"));
        assertTrue(yaml.contains("DROP TABLE"));

        // Parse back
        YamlChangelogParser parser = new YamlChangelogParser();
        IChangelog parsed = parser.parse(
                new ByteArrayInputStream(yaml.getBytes(StandardCharsets.UTF_8)),
                "rollback.db.yaml",
                new ChangelogParameterResolverImpl());

        assertFalse(parsed.getChangelogSets().get(0).getRollbackEntries().isEmpty());
    }


    /**
     * Test JSON round-trip.
     *
     * @throws Exception on error
     */
    @Test
    void testJsonRoundTrip() throws Exception {
        IChangelog original = createSqlChangelog();
        JsonChangelogWriterImpl writer = new JsonChangelogWriterImpl();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        writer.write(original, out);
        String json = out.toString(StandardCharsets.UTF_8.name());

        assertNotNull(json);
        assertTrue(json.contains("toolarium-db-changelog/1.0"));
        assertTrue(json.contains(CS_CREATE_TABLE));

        // Parse back
        JsonChangelogParser parser = new JsonChangelogParser();
        IChangelog parsed = parser.parse(
                new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8)),
                "round-trip.db.json",
                new ChangelogParameterResolverImpl());

        assertEquals(2, parsed.getChangelogSets().size());
        assertEquals(CS_CREATE_TABLE, parsed.getChangelogSets().get(0).getIdentity().getId());
    }


    /**
     * Test JSON writer with Writer.
     *
     * @throws Exception on error
     */
    @Test
    void testJsonWriterWithWriter() throws Exception {
        IChangelog original = createSqlChangelog();
        JsonChangelogWriterImpl writer = new JsonChangelogWriterImpl();

        StringWriter sw = new StringWriter();
        writer.write(original, sw);
        String json = sw.toString();

        assertNotNull(json);
        assertTrue(json.contains("\"format\""));
    }


    /**
     * Test XML writer produces valid output.
     *
     * @throws Exception on error
     */
    @Test
    void testXmlWriter() throws Exception {
        IChangelog original = createSqlChangelog();
        XmlChangelogWriterImpl writer = new XmlChangelogWriterImpl();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        writer.write(original, out);
        String xml = out.toString(StandardCharsets.UTF_8.name());

        assertNotNull(xml);
        assertTrue(xml.contains("dbChangelog"));
        assertTrue(xml.contains("toolarium-db-changelog/1.0"));
        assertTrue(xml.contains("changeSet"));
        assertTrue(xml.contains(CS_CREATE_TABLE));
    }


    /**
     * Test XML writer with Writer.
     *
     * @throws Exception on error
     */
    @Test
    void testXmlWriterWithWriter() throws Exception {
        IChangelog original = createSqlChangelog();
        XmlChangelogWriterImpl writer = new XmlChangelogWriterImpl();

        StringWriter sw = new StringWriter();
        writer.write(original, sw);
        String xml = sw.toString();

        assertNotNull(xml);
        assertTrue(xml.contains("dbChangelog"));
    }


    /**
     * Test file extension methods.
     */
    @Test
    void testFileExtensions() {
        assertEquals(".db.yaml", new YamlChangelogWriterImpl().getFileExtension());
        assertEquals(".db.xml", new XmlChangelogWriterImpl().getFileExtension());
        assertEquals(".db.json", new JsonChangelogWriterImpl().getFileExtension());
    }


    /**
     * Create a simple SQL-based changelog for testing.
     *
     * @return the changelog
     */
    private IChangelog createSqlChangelog() {
        ChangelogChangeSetIdentity id1 = new ChangelogChangeSetIdentity(CS_CREATE_TABLE, TEST_AUTHOR, TEST_FILE);
        ChangelogChangeSet cs1 = new ChangelogChangeSet(id1,
                Collections.singletonList(new SqlChangelogEntry("CREATE TABLE test_table (id INT NOT NULL PRIMARY KEY)")));

        ChangelogChangeSetIdentity id2 = new ChangelogChangeSetIdentity("insert-data", TEST_AUTHOR, TEST_FILE);
        ChangelogChangeSet cs2 = new ChangelogChangeSet(id2,
                Collections.singletonList(new SqlChangelogEntry("INSERT INTO test_table (id) VALUES (1)")));

        return new Changelog(TEST_FILE, Arrays.asList(cs1, cs2));
    }


    /**
     * Create a DDL-based changelog for testing.
     *
     * @return the changelog
     */
    private IChangelog createDdlChangelog() {
        ChangelogColumnDefinition idCol = new ChangelogColumnDefinition("id", "INT");
        idCol.setPrimaryKey(true).setNullable(false);
        ChangelogColumnDefinition nameCol = new ChangelogColumnDefinition("name", "VARCHAR(100)");

        ChangelogChangeSetIdentity id1 = new ChangelogChangeSetIdentity("ddl-1", TEST_AUTHOR, "ddl.db.yaml");
        ChangelogChangeSet cs1 = new ChangelogChangeSet(id1,
                Collections.singletonList(new CreateTableChangelogEntry("orders", Arrays.asList(idCol, nameCol))));

        ChangelogColumnDefinition emailCol = new ChangelogColumnDefinition("email", "VARCHAR(255)");
        ChangelogChangeSetIdentity id2 = new ChangelogChangeSetIdentity("ddl-2", TEST_AUTHOR, "ddl.db.yaml");
        ChangelogChangeSet cs2 = new ChangelogChangeSet(id2,
                Collections.singletonList(new AddColumnChangelogEntry("orders", Collections.singletonList(emailCol))));

        return new Changelog("ddl.db.yaml", Arrays.asList(cs1, cs2));
    }


    /**
     * Create a changelog with various attributes set.
     *
     * @return the changelog
     */
    private IChangelog createChangelogWithAttributes() {
        ChangelogChangeSetIdentity identity = new ChangelogChangeSetIdentity("attr-1", TEST_AUTHOR, "attrs.db.yaml");
        List<IChangelogEntry> entries = Collections.singletonList(new SqlChangelogEntry("SELECT 1"));
        ChangelogChangeSet cs = new ChangelogChangeSet(identity, entries, Collections.emptyList(), "Test comment",
                true, true, false, true,
                Arrays.asList("dev", "test"), Arrays.asList("v1.0"), null);

        return new Changelog("attrs.db.yaml", Collections.singletonList(cs));
    }
}
