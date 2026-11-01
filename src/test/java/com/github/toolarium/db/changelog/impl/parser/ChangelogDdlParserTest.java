/*
 * ChangelogDdlParserTest.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog.impl.parser;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.toolarium.db.changelog.ChangelogRunnerBuilder;
import com.github.toolarium.db.changelog.IChangelogRunner;
import com.github.toolarium.db.changelog.dto.IChangelog;
import com.github.toolarium.db.changelog.dto.IChangelogChangeSet;
import com.github.toolarium.db.changelog.dto.IChangelogTrackingEntry;
import com.github.toolarium.db.changelog.dto.column.AddColumnChangelogEntry;
import com.github.toolarium.db.changelog.dto.index.CreateIndexChangelogEntry;
import com.github.toolarium.db.changelog.dto.table.CreateTableChangelogEntry;
import com.github.toolarium.db.changelog.impl.resolver.ChangelogParameterResolverFactory;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


/**
 * Tests for parsing structured DDL change types from YAML changelog.
 *
 * @author patrick
 */
public class ChangelogDdlParserTest {
    private static final String DDL_CHANGELOG = "db/changelog/test-ddl-changelog.db.yaml";
    private JdbcDataSource dataSource;


    /**
     * Set up.
     */
    @BeforeEach
    void setUp() {
        dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:ddlparser_" + System.nanoTime() + ";DB_CLOSE_DELAY=-1");
        dataSource.setUser("sa");
        dataSource.setPassword("");
    }


    /**
     * Test parsing structured DDL entries from YAML.
     *
     * @throws Exception on error
     */
    @Test
    void testParseDdlEntries() throws Exception {
        ChangelogParserFactory factory = new ChangelogParserFactory();
        IChangelogParser parser = factory.getParser(DDL_CHANGELOG);

        InputStream input = getClass().getClassLoader().getResourceAsStream(DDL_CHANGELOG);
        IChangelog changelog = parser.parse(input, DDL_CHANGELOG,
                ChangelogParameterResolverFactory.getInstance().getParameterResolver());

        assertEquals(3, changelog.getChangelogSets().size());

        IChangelogChangeSet first = changelog.getChangelogSets().get(0);
        assertEquals("create-customers", first.getIdentity().getId());
        assertTrue(first.getEntries().get(0) instanceof CreateTableChangelogEntry);

        CreateTableChangelogEntry createTable = (CreateTableChangelogEntry) first.getEntries().get(0);
        assertEquals("ddl_customers", createTable.getTableName());
        assertEquals(4, createTable.getColumns().size());

        IChangelogChangeSet second = changelog.getChangelogSets().get(1);
        assertTrue(second.getEntries().get(0) instanceof AddColumnChangelogEntry);

        IChangelogChangeSet third = changelog.getChangelogSets().get(2);
        assertTrue(third.getEntries().get(0) instanceof CreateIndexChangelogEntry);
    }


    /**
     * Test end-to-end: parse DDL YAML and execute against H2.
     *
     * @throws Exception on error
     */
    @Test
    void testParseDdlAndExecute() throws Exception {
        IChangelogRunner runner = ChangelogRunnerBuilder.builder()
                .dataSource(dataSource)
                .changelog("classpath:" + DDL_CHANGELOG)
                .build();

        runner.update();

        // Verify table created with resolved property
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("INSERT INTO ddl_customers (name, email) VALUES ('Alice', 'alice@test.com')");
            ResultSet rs = stmt.executeQuery("SELECT name, phone FROM ddl_customers WHERE email = 'alice@test.com'");
            assertTrue(rs.next());
            assertEquals("Alice", rs.getString("name"));
        }

        List<IChangelogTrackingEntry> history = runner.history();
        assertEquals(3, history.size());
        assertTrue(history.get(0).getDescription().contains("createTable"));
        assertTrue(history.get(1).getDescription().contains("addColumn"));
        assertTrue(history.get(2).getDescription().contains("createIndex"));
    }
}
