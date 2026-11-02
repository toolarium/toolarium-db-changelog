/*
 * FlywaySqlChangelogParserTest.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog.impl.parser.flyway;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.toolarium.db.changelog.exception.ChangelogParseException;
import com.github.toolarium.db.changelog.impl.resolver.impl.ChangelogParameterResolverImpl;
import com.github.toolarium.db.changelog.model.IChangelog;
import com.github.toolarium.db.changelog.model.IChangelogChangeSet;
import com.github.toolarium.db.changelog.model.sql.SqlChangelogEntry;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;


/**
 * Tests for FlywaySqlChangelogParser.
 *
 * @author patrick
 */
public class FlywaySqlChangelogParserTest {

    /**
     * Test parsing a versioned migration file.
     *
     * @throws Exception on error
     */
    @Test
    void testVersionedMigration() throws Exception {
        String sql = "CREATE TABLE users (id INT PRIMARY KEY, name VARCHAR(100));\nINSERT INTO users (id, name) VALUES (1, 'Admin');";
        FlywaySqlChangelogParser parser = new FlywaySqlChangelogParser();

        IChangelog changelog = parser.parse(
                new ByteArrayInputStream(sql.getBytes(StandardCharsets.UTF_8)),
                "db/migration/V1.0__Create_users_table.sql",
                new ChangelogParameterResolverImpl());

        assertNotNull(changelog);
        assertEquals(1, changelog.getChangelogSets().size());

        IChangelogChangeSet cs = changelog.getChangelogSets().get(0);
        assertEquals("1.0", cs.getIdentity().getId());
        assertEquals("flyway", cs.getIdentity().getAuthor());
        assertEquals("Create users table", cs.getComment());
        assertFalse(cs.isRunOnChange());

        SqlChangelogEntry entry = (SqlChangelogEntry) cs.getEntries().get(0);
        assertTrue(entry.getSql().contains("CREATE TABLE users"));
    }


    /**
     * Test parsing a repeatable migration file.
     *
     * @throws Exception on error
     */
    @Test
    void testRepeatableMigration() throws Exception {
        String sql = "CREATE OR REPLACE VIEW active_users AS SELECT id, name FROM users WHERE active = true;";
        FlywaySqlChangelogParser parser = new FlywaySqlChangelogParser();

        IChangelog changelog = parser.parse(
                new ByteArrayInputStream(sql.getBytes(StandardCharsets.UTF_8)),
                "db/migration/R__Active_users_view.sql",
                new ChangelogParameterResolverImpl());

        assertNotNull(changelog);
        assertEquals(1, changelog.getChangelogSets().size());

        IChangelogChangeSet cs = changelog.getChangelogSets().get(0);
        assertEquals("R__Active_users_view", cs.getIdentity().getId());
        assertEquals("Active users view", cs.getComment());
        assertTrue(cs.isRunOnChange());
    }


    /**
     * Test parsing a versioned migration with multi-segment version.
     *
     * @throws Exception on error
     */
    @Test
    void testVersionedMigrationMultiSegmentVersion() throws Exception {
        String sql = "ALTER TABLE users ADD COLUMN email VARCHAR(255);";
        FlywaySqlChangelogParser parser = new FlywaySqlChangelogParser();

        IChangelog changelog = parser.parse(
                new ByteArrayInputStream(sql.getBytes(StandardCharsets.UTF_8)),
                "V2.1.3__Add_email_column.sql",
                new ChangelogParameterResolverImpl());

        IChangelogChangeSet cs = changelog.getChangelogSets().get(0);
        assertEquals("2.1.3", cs.getIdentity().getId());
        assertEquals("Add email column", cs.getComment());
    }


    /**
     * Test isFlywayMigration detection.
     */
    @Test
    void testIsFlywayMigration() {
        FlywaySqlChangelogParser parser = new FlywaySqlChangelogParser();

        assertTrue(parser.isFlywayMigration("V1__Init.sql"));
        assertTrue(parser.isFlywayMigration("V1.0__Create_table.sql"));
        assertTrue(parser.isFlywayMigration("V2.1.3__Add_column.sql"));
        assertTrue(parser.isFlywayMigration("R__Create_view.sql"));

        assertFalse(parser.isFlywayMigration("init.sql"));
        assertFalse(parser.isFlywayMigration("V1.sql"));
        assertFalse(parser.isFlywayMigration("R__.sql"));
        assertFalse(parser.isFlywayMigration(null));
        assertFalse(parser.isFlywayMigration("changelog.db.yaml"));
    }


    /**
     * Test that supports() returns false (Flyway parser is not auto-detected).
     */
    @Test
    void testSupportsReturnsFalse() {
        FlywaySqlChangelogParser parser = new FlywaySqlChangelogParser();
        assertFalse(parser.supports(".sql"));
        assertFalse(parser.supports(".db.yaml"));
    }


    /**
     * Test empty migration file throws exception.
     */
    @Test
    void testEmptyMigrationThrows() {
        FlywaySqlChangelogParser parser = new FlywaySqlChangelogParser();
        assertThrows(ChangelogParseException.class, () ->
                parser.parse(
                        new ByteArrayInputStream("".getBytes(StandardCharsets.UTF_8)),
                        "V1__Empty.sql",
                        new ChangelogParameterResolverImpl()));
    }


    /**
     * Test invalid file name throws exception.
     */
    @Test
    void testInvalidFileNameThrows() {
        FlywaySqlChangelogParser parser = new FlywaySqlChangelogParser();
        assertThrows(ChangelogParseException.class, () ->
                parser.parse(
                        new ByteArrayInputStream("SELECT 1;".getBytes(StandardCharsets.UTF_8)),
                        "not_flyway.sql",
                        new ChangelogParameterResolverImpl()));
    }


    /**
     * Test property substitution works.
     *
     * @throws Exception on error
     */
    @Test
    void testPropertySubstitution() throws Exception {
        String sql = "CREATE TABLE ${table.name} (id INT PRIMARY KEY);";
        FlywaySqlChangelogParser parser = new FlywaySqlChangelogParser();

        ChangelogParameterResolverImpl resolver = new ChangelogParameterResolverImpl();
        resolver.setProperty("table.name", "my_table");

        IChangelog changelog = parser.parse(
                new ByteArrayInputStream(sql.getBytes(StandardCharsets.UTF_8)),
                "V1__Create_table.sql",
                resolver);

        SqlChangelogEntry entry = (SqlChangelogEntry) changelog.getChangelogSets().get(0).getEntries().get(0);
        assertEquals("CREATE TABLE my_table (id INT PRIMARY KEY);", entry.getSql());
    }
}
