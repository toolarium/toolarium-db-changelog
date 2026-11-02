/*
 * ChangelogWriterFactoryTest.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog.impl.writer;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.toolarium.db.changelog.impl.writer.impl.JsonChangelogWriterImpl;
import com.github.toolarium.db.changelog.impl.writer.impl.XmlChangelogWriterImpl;
import com.github.toolarium.db.changelog.impl.writer.impl.YamlChangelogWriterImpl;
import org.junit.jupiter.api.Test;


/**
 * Tests for ChangelogWriterFactory.
 *
 * @author patrick
 */
public class ChangelogWriterFactoryTest {

    /**
     * Test singleton instance.
     */
    @Test
    void testSingleton() {
        ChangelogWriterFactory instance1 = ChangelogWriterFactory.getInstance();
        ChangelogWriterFactory instance2 = ChangelogWriterFactory.getInstance();
        assertNotNull(instance1);
        assertSame(instance1, instance2);
    }


    /**
     * Test getWriter returns correct writer for YAML extensions.
     */
    @Test
    void testGetYamlWriter() {
        IChangelogWriter writer = ChangelogWriterFactory.getInstance().getWriter(".db.yaml");
        assertNotNull(writer);
        assertTrue(writer instanceof YamlChangelogWriterImpl);

        IChangelogWriter ymlWriter = ChangelogWriterFactory.getInstance().getWriter(".db.yml");
        assertNotNull(ymlWriter);
        assertTrue(ymlWriter instanceof YamlChangelogWriterImpl);
    }


    /**
     * Test getWriter returns correct writer for XML extension.
     */
    @Test
    void testGetXmlWriter() {
        IChangelogWriter writer = ChangelogWriterFactory.getInstance().getWriter(".db.xml");
        assertNotNull(writer);
        assertTrue(writer instanceof XmlChangelogWriterImpl);
    }


    /**
     * Test getWriter returns correct writer for JSON extension.
     */
    @Test
    void testGetJsonWriter() {
        IChangelogWriter writer = ChangelogWriterFactory.getInstance().getWriter(".db.json");
        assertNotNull(writer);
        assertTrue(writer instanceof JsonChangelogWriterImpl);
    }


    /**
     * Test getWriter returns null for unsupported extension.
     */
    @Test
    void testGetWriterUnsupported() {
        assertNull(ChangelogWriterFactory.getInstance().getWriter(".sql"));
        assertNull(ChangelogWriterFactory.getInstance().getWriter(".txt"));
    }


    /**
     * Test direct getter methods.
     */
    @Test
    void testDirectGetters() {
        assertNotNull(ChangelogWriterFactory.getInstance().getYamlWriter());
        assertNotNull(ChangelogWriterFactory.getInstance().getXmlWriter());
        assertNotNull(ChangelogWriterFactory.getInstance().getJsonWriter());
    }
}
