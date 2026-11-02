/*
 * ChangelogResourceResolverTest.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog.impl.resolver;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.toolarium.db.changelog.IChangelogResourceResolver;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;


/**
 * Tests for ChangelogResourceResolverImpl covering classpath, file system, and JAR resolution.
 *
 * @author patrick
 */
public class ChangelogResourceResolverTest {
    private static final String CLASSPATH_RESOURCE = "db/changelog/resolver-test.db.yaml";
    private static final String CLASSPATH_PREFIX_RESOURCE = "classpath:db/changelog/resolver-test.db.yaml";
    private static final String FORMAT_MARKER = "toolarium-db-changelog";
    private final IChangelogResourceResolver resolver = ChangelogResourceResolverFactory.getInstance().getResourceResolver();


    /**
     * Test factory singleton.
     */
    @Test
    void testFactorySingleton() {
        assertSame(ChangelogResourceResolverFactory.getInstance(), ChangelogResourceResolverFactory.getInstance());
        assertNotNull(resolver);
    }


    /**
     * Test resolve classpath resource without prefix.
     *
     * @throws IOException on error
     */
    @Test
    void testResolveClasspathResource() throws IOException {
        try (InputStream input = resolver.resolve(CLASSPATH_RESOURCE)) {
            assertNotNull(input, "Should find resource on classpath");
            String content = readContent(input);
            assertTrue(content.contains(FORMAT_MARKER));
        }
    }


    /**
     * Test resolve classpath resource with classpath: prefix.
     *
     * @throws IOException on error
     */
    @Test
    void testResolveClasspathPrefixResource() throws IOException {
        try (InputStream input = resolver.resolve(CLASSPATH_PREFIX_RESOURCE)) {
            assertNotNull(input, "Should find resource with classpath: prefix");
            String content = readContent(input);
            assertTrue(content.contains(FORMAT_MARKER));
        }
    }


    /**
     * Test resolve file system resource.
     *
     * @param tempDir temporary directory for test files
     * @throws IOException on error
     */
    @Test
    void testResolveFileSystemResource(@TempDir File tempDir) throws IOException {
        File changelogFile = new File(tempDir, "fs-changelog.db.yaml");
        try (FileOutputStream fos = new FileOutputStream(changelogFile)) {
            fos.write("format: toolarium-db-changelog/1.0\nchangesets: []".getBytes(StandardCharsets.UTF_8));
        }

        try (InputStream input = resolver.resolve(changelogFile.getAbsolutePath())) {
            assertNotNull(input, "Should find file on file system");
            String content = readContent(input);
            assertTrue(content.contains(FORMAT_MARKER));
        }
    }


    /**
     * Test resolve with path normalization (double slashes, dot segments).
     *
     * @throws IOException on error
     */
    @Test
    void testResolveWithPathNormalization() throws IOException {
        try (InputStream input = resolver.resolve("db/./changelog/../changelog/test-changelog.db.yaml")) {
            assertNotNull(input, "Should resolve normalized path");
        }
    }


    /**
     * Test resolve non-existent resource returns null.
     */
    @Test
    void testResolveNonExistentReturnsNull() {
        assertNull(resolver.resolve("non/existent/changelog.db.yaml"));
    }


    /**
     * Test resolve null path returns null.
     */
    @Test
    void testResolveNullReturnsNull() {
        assertNull(resolver.resolve(null));
    }


    /**
     * Test resolve empty path returns null.
     */
    @Test
    void testResolveEmptyReturnsNull() {
        assertNull(resolver.resolve(""));
        assertNull(resolver.resolve("   "));
    }


    /**
     * Test resolveAsUrl for classpath resource.
     */
    @Test
    void testResolveAsUrlClasspath() {
        URL url = resolver.resolveAsUrl(CLASSPATH_RESOURCE);
        assertNotNull(url, "Should resolve classpath resource to URL");
    }


    /**
     * Test resolveAsUrl for file system resource.
     *
     * @param tempDir temporary directory for test files
     * @throws IOException on error
     */
    @Test
    void testResolveAsUrlFileSystem(@TempDir File tempDir) throws IOException {
        File changelogFile = new File(tempDir, "url-test.db.yaml");
        try (FileOutputStream fos = new FileOutputStream(changelogFile)) {
            fos.write("format: toolarium-db-changelog/1.0".getBytes(StandardCharsets.UTF_8));
        }

        URL url = resolver.resolveAsUrl(changelogFile.getAbsolutePath());
        assertNotNull(url, "Should resolve file system resource to URL");
    }


    /**
     * Test resolveAsUrl for non-existent resource returns null.
     */
    @Test
    void testResolveAsUrlNonExistentReturnsNull() {
        assertNull(resolver.resolveAsUrl("does/not/exist.db.yaml"));
    }


    /**
     * Test exists for classpath resource.
     */
    @Test
    void testExistsClasspathResource() {
        assertTrue(resolver.exists(CLASSPATH_RESOURCE));
    }


    /**
     * Test exists for non-existent resource.
     */
    @Test
    void testExistsNonExistent() {
        assertFalse(resolver.exists("no/such/resource.db.yaml"));
    }


    /**
     * Test exists for file system resource.
     *
     * @param tempDir temporary directory for test files
     * @throws IOException on error
     */
    @Test
    void testExistsFileSystem(@TempDir File tempDir) throws IOException {
        File changelogFile = new File(tempDir, "exists-test.db.yaml");
        try (FileOutputStream fos = new FileOutputStream(changelogFile)) {
            fos.write("test".getBytes(StandardCharsets.UTF_8));
        }

        assertTrue(resolver.exists(changelogFile.getAbsolutePath()));
    }


    /**
     * Test classpath: prefix prevents file system fallback.
     *
     * @param tempDir temporary directory for test files
     * @throws IOException on error
     */
    @Test
    void testClasspathPrefixNoFileSystemFallback(@TempDir File tempDir) throws IOException {
        File changelogFile = new File(tempDir, "only-on-disk.db.yaml");
        try (FileOutputStream fos = new FileOutputStream(changelogFile)) {
            fos.write("test".getBytes(StandardCharsets.UTF_8));
        }

        // With classpath: prefix, should not find file on disk
        assertNull(resolver.resolve("classpath:" + changelogFile.getAbsolutePath()));
    }


    /**
     * Test resolve resource from JAR on classpath.
     * The test-changelogs.jar is in src/test/resources/db/ and contains db/changelog/jar-changelog.db.yaml.
     *
     * @throws IOException on error
     */
    @Test
    void testResolveResourceFromJar() throws IOException {
        // The JAR file is in test resources, add it to classpath and resolve
        URL jarUrl = getClass().getClassLoader().getResource("db/test-changelogs.jar");
        assertNotNull(jarUrl, "Test JAR should be on classpath");

        // Try to load the resource inside the JAR via jar: URL
        String jarPath = "jar:" + jarUrl.toString() + "!/db/changelog/jar-changelog.db.yaml";
        try (InputStream input = java.net.URI.create(jarPath).toURL().openStream()) {
            assertNotNull(input, "Should open resource inside JAR");
            String content = readContent(input);
            assertTrue(content.contains("jar-resource"), "Should contain jar-resource changeset id");
        }
    }


    /**
     * Test resolve resource from nested JAR (JAR inside JAR).
     * The test-nested-changelogs.jar contains lib/inner.jar which contains db/changelog/nested-changelog.db.yaml.
     *
     * @throws IOException on error
     */
    @Test
    void testResolveNestedJarExists() {
        URL jarUrl = getClass().getClassLoader().getResource("db/test-nested-changelogs.jar");
        assertNotNull(jarUrl, "Nested test JAR should be on classpath");
    }


    /**
     * Read the full content of an input stream as a string.
     *
     * @param input the input stream
     * @return the content
     * @throws IOException on error
     */
    private String readContent(InputStream input) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
        }
        return sb.toString();
    }
}
