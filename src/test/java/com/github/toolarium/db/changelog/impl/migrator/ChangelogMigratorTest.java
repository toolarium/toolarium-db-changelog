/*
 * ChangelogMigratorTest.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog.impl.migrator;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.toolarium.db.changelog.config.ChangelogConfiguration;
import com.github.toolarium.db.changelog.exception.ChangelogException;
import com.github.toolarium.db.changelog.impl.migrator.impl.FlywayChangelogMigratorImpl;
import com.github.toolarium.db.changelog.impl.migrator.impl.LiquibaseChangelogMigratorImpl;
import com.github.toolarium.db.changelog.model.IChangelog;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;


/**
 * Tests for changelog migrators and factory.
 *
 * @author patrick
 */
public class ChangelogMigratorTest {

    /**
     * Test factory singleton.
     */
    @Test
    void testFactorySingleton() {
        ChangelogMigratorFactory instance1 = ChangelogMigratorFactory.getInstance();
        ChangelogMigratorFactory instance2 = ChangelogMigratorFactory.getInstance();
        assertNotNull(instance1);
        assertSame(instance1, instance2);
    }


    /**
     * Test factory creates Liquibase migrator.
     */
    @Test
    void testFactoryLiquibaseMigrator() {
        IChangelogMigrator migrator = ChangelogMigratorFactory.getInstance()
                .getLiquibaseMigrator(new ByteArrayInputStream(new byte[0]), "test.yaml", ".db.yaml");
        assertNotNull(migrator);
        assertTrue(migrator instanceof LiquibaseChangelogMigratorImpl);
    }


    /**
     * Test factory creates Flyway migrator.
     *
     * @param tempDir the temporary directory
     */
    @Test
    void testFactoryFlywayMigrator(@TempDir Path tempDir) {
        IChangelogMigrator migrator = ChangelogMigratorFactory.getInstance()
                .getFlywayMigrator(tempDir.toFile(), ".db.yaml");
        assertNotNull(migrator);
        assertTrue(migrator instanceof FlywayChangelogMigratorImpl);
    }


    /**
     * Test Liquibase migrator: parse and migrate a formatted SQL changelog.
     *
     * @throws Exception on error
     */
    @Test
    void testLiquibaseSqlMigration() throws Exception {
        String liquibaseSql = "--liquibase formatted sql\n\n"
                + "--changeset john:create-users\n"
                + "CREATE TABLE users (id INT PRIMARY KEY, name VARCHAR(100));\n\n"
                + "--rollback DROP TABLE users;\n\n"
                + "--changeset john:add-email\n"
                + "ALTER TABLE users ADD COLUMN email VARCHAR(255);\n";

        IChangelogMigrator migrator = ChangelogMigratorFactory.getInstance()
                .getLiquibaseMigrator(
                        new ByteArrayInputStream(liquibaseSql.getBytes(StandardCharsets.UTF_8)),
                        "changelog.sql",
                        ChangelogConfiguration.FILE_EXTENSION_DB_YAML);

        // Test parse
        IChangelog changelog = migrator.parse();
        assertNotNull(changelog);

        // Re-create migrator (input stream consumed)
        migrator = ChangelogMigratorFactory.getInstance()
                .getLiquibaseMigrator(
                        new ByteArrayInputStream(liquibaseSql.getBytes(StandardCharsets.UTF_8)),
                        "changelog.sql",
                        ChangelogConfiguration.FILE_EXTENSION_DB_YAML);

        // Test migrate to OutputStream
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        migrator.migrate(out);
        String yaml = out.toString(StandardCharsets.UTF_8.name());

        assertNotNull(yaml);
        assertTrue(yaml.contains("toolarium-db-changelog/1.0"));
        assertTrue(yaml.contains("create-users"));
        assertTrue(yaml.contains("add-email"));
    }


    /**
     * Test Liquibase migrator with Writer output.
     *
     * @throws Exception on error
     */
    @Test
    void testLiquibaseMigrationToWriter() throws Exception {
        String liquibaseSql = "--liquibase formatted sql\n\n"
                + "--changeset admin:init\n"
                + "CREATE TABLE config (key VARCHAR(100) PRIMARY KEY, value VARCHAR(500));\n";

        IChangelogMigrator migrator = ChangelogMigratorFactory.getInstance()
                .getLiquibaseMigrator(
                        new ByteArrayInputStream(liquibaseSql.getBytes(StandardCharsets.UTF_8)),
                        "init.sql",
                        ChangelogConfiguration.FILE_EXTENSION_DB_JSON);

        StringWriter sw = new StringWriter();
        migrator.migrate(sw);
        String json = sw.toString();

        assertTrue(json.contains("\"format\""));
        assertTrue(json.contains("init"));
    }


    /**
     * Test Flyway migrator with versioned and repeatable files.
     *
     * @param tempDir the temporary directory
     * @throws Exception on error
     */
    @Test
    void testFlywayMigration(@TempDir Path tempDir) throws Exception {
        // Create versioned migration files
        Files.writeString(tempDir.resolve("V1__Create_users.sql"),
                "CREATE TABLE users (id INT PRIMARY KEY, name VARCHAR(100));");
        Files.writeString(tempDir.resolve("V2__Add_email.sql"),
                "ALTER TABLE users ADD COLUMN email VARCHAR(255);");
        Files.writeString(tempDir.resolve("R__Active_users_view.sql"),
                "CREATE OR REPLACE VIEW active_users AS SELECT id, name FROM users;");

        // Also create a non-Flyway file that should be ignored
        Files.writeString(tempDir.resolve("readme.sql"), "-- Not a migration");

        IChangelogMigrator migrator = ChangelogMigratorFactory.getInstance()
                .getFlywayMigrator(tempDir.toFile(), ChangelogConfiguration.FILE_EXTENSION_DB_YAML);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        migrator.migrate(out);
        String yaml = out.toString(StandardCharsets.UTF_8.name());

        assertNotNull(yaml);
        assertTrue(yaml.contains("toolarium-db-changelog/1.0"));
        assertTrue(yaml.contains("CREATE TABLE users"));
        assertTrue(yaml.contains("ADD COLUMN email"));
        assertTrue(yaml.contains("active_users"));
    }


    /**
     * Test Flyway migrator parse returns model.
     *
     * @param tempDir the temporary directory
     * @throws Exception on error
     */
    @Test
    void testFlywayMigratorParse(@TempDir Path tempDir) throws Exception {
        Files.writeString(tempDir.resolve("V1.0__Init.sql"),
                "CREATE TABLE settings (key VARCHAR(100) PRIMARY KEY);");

        IChangelogMigrator migrator = ChangelogMigratorFactory.getInstance()
                .getFlywayMigrator(tempDir.toFile(), ChangelogConfiguration.FILE_EXTENSION_DB_YAML);

        IChangelog changelog = migrator.parse();
        assertNotNull(changelog);
        assertTrue(changelog.getChangelogSets().size() >= 1);
    }


    /**
     * Test Flyway migrator with empty directory throws exception.
     *
     * @param tempDir the temporary directory
     */
    @Test
    void testFlywayEmptyDirectoryThrows(@TempDir Path tempDir) {
        IChangelogMigrator migrator = ChangelogMigratorFactory.getInstance()
                .getFlywayMigrator(tempDir.toFile(), ChangelogConfiguration.FILE_EXTENSION_DB_YAML);

        assertThrows(ChangelogException.class, () -> {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            migrator.migrate(out);
        });
    }


    /**
     * Test Flyway migrator with non-existing directory throws exception.
     */
    @Test
    void testFlywayNonExistingDirectoryThrows() {
        IChangelogMigrator migrator = ChangelogMigratorFactory.getInstance()
                .getFlywayMigrator(new File("/non/existing/dir"), ChangelogConfiguration.FILE_EXTENSION_DB_YAML);

        assertThrows(ChangelogException.class, () -> {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            migrator.migrate(out);
        });
    }


    /**
     * Test Liquibase migrator with unsupported output format throws exception.
     */
    @Test
    void testUnsupportedOutputFormatThrows() {
        String sql = "--liquibase formatted sql\n--changeset x:y\nSELECT 1;\n";
        IChangelogMigrator migrator = ChangelogMigratorFactory.getInstance()
                .getLiquibaseMigrator(
                        new ByteArrayInputStream(sql.getBytes(StandardCharsets.UTF_8)),
                        "test.sql",
                        ".unsupported");

        assertThrows(ChangelogException.class, () -> {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            migrator.migrate(out);
        });
    }
}
