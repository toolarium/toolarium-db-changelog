/*
 * XmlChangelogWriterImpl.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog.impl.writer.impl;

import com.github.toolarium.db.changelog.config.ChangelogConfiguration;
import com.github.toolarium.db.changelog.exception.ChangelogException;
import com.github.toolarium.db.changelog.impl.writer.IChangelogWriter;
import com.github.toolarium.db.changelog.impl.writer.util.ChangelogWriterUtil;
import com.github.toolarium.db.changelog.model.IChangelog;
import com.github.toolarium.db.changelog.model.IChangelogChangeSet;
import java.io.OutputStream;
import java.io.Writer;
import java.util.List;
import java.util.Map;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Writes a changelog model to the DB-Changelog XML format (.db.xml).
 * Uses JDK built-in StAX (no external dependency).
 *
 * @author patrick
 */
public class XmlChangelogWriterImpl implements IChangelogWriter {
    private static final Logger LOG = LoggerFactory.getLogger(XmlChangelogWriterImpl.class);
    private static final XMLOutputFactory XML_OUTPUT_FACTORY = XMLOutputFactory.newInstance();
    private static final String NL = "\n";
    private static final String INDENT2 = "  ";
    private static final String INDENT4 = "    ";
    private static final String INDENT6 = "\n      ";


    /**
     * @see com.github.toolarium.db.changelog.impl.writer.IChangelogWriter#write(IChangelog, OutputStream)
     */
    @Override
    public void write(IChangelog changelog, OutputStream output) throws ChangelogException {
        try {
            XMLStreamWriter xml = XML_OUTPUT_FACTORY.createXMLStreamWriter(output, "UTF-8");
            writeChangelog(changelog, xml);
        } catch (ChangelogException e) {
            throw e;
        } catch (Exception e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Failed to write XML changelog: " + e.getMessage(), e);
            }
            throw new ChangelogException("Failed to write XML changelog.", e);
        }
    }


    /**
     * @see com.github.toolarium.db.changelog.impl.writer.IChangelogWriter#write(IChangelog, Writer)
     */
    @Override
    public void write(IChangelog changelog, Writer writer) throws ChangelogException {
        try {
            XMLStreamWriter xml = XML_OUTPUT_FACTORY.createXMLStreamWriter(writer);
            writeChangelog(changelog, xml);
        } catch (ChangelogException e) {
            throw e;
        } catch (Exception e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Failed to write XML changelog: " + e.getMessage(), e);
            }
            throw new ChangelogException("Failed to write XML changelog.", e);
        }
    }


    /**
     * @see com.github.toolarium.db.changelog.impl.writer.IChangelogWriter#getFileExtension()
     */
    @Override
    public String getFileExtension() {
        return ChangelogConfiguration.FILE_EXTENSION_DB_XML;
    }


    /**
     * Write the changelog to the XML stream writer.
     *
     * @param changelog the changelog
     * @param xml the XML stream writer
     * @throws Exception if writing fails
     */
    private void writeChangelog(IChangelog changelog, XMLStreamWriter xml) throws Exception {
        xml.writeStartDocument("UTF-8", "1.0");
        xml.writeCharacters(NL);
        xml.writeStartElement("dbChangelog");
        xml.writeAttribute("format", ChangelogConfiguration.FORMAT_VERSION);
        xml.writeCharacters(NL);

        for (IChangelogChangeSet changeSet : changelog.getChangelogSets()) {
            writeChangeSet(xml, changeSet);
        }

        xml.writeEndElement();
        xml.writeCharacters(NL);
        xml.writeEndDocument();
        xml.flush();
        xml.close();

        if (LOG.isDebugEnabled()) {
            LOG.debug("Wrote " + changelog.getChangelogSets().size() + " changeset(s) to XML.");
        }
    }


    /**
     * Write a single changeset element.
     *
     * @param xml the XML stream writer
     * @param changeSet the changeset
     * @throws Exception if writing fails
     */
    private void writeChangeSet(XMLStreamWriter xml, IChangelogChangeSet changeSet) throws Exception {
        Map<String, Object> map = ChangelogWriterUtil.getInstance().toChangeSetMap(changeSet);

        xml.writeCharacters(INDENT2);
        xml.writeStartElement("changeSet");
        xml.writeAttribute("id", (String) map.get("id"));
        xml.writeAttribute("author", (String) map.get("author"));

        writeOptionalAttribute(xml, map, "comment");
        writeOptionalAttribute(xml, map, "runAlways");
        writeOptionalAttribute(xml, map, "runOnChange");
        writeOptionalAttribute(xml, map, "failOnError");
        writeOptionalAttribute(xml, map, "runInTransaction");
        writeOptionalAttribute(xml, map, "filePath");

        if (map.containsKey("context")) {
            xml.writeAttribute("context", joinList(map.get("context")));
        }
        if (map.containsKey("labels")) {
            xml.writeAttribute("labels", joinList(map.get("labels")));
        }

        xml.writeCharacters(NL);

        writeEntries(xml, map, "changes");
        writeEntries(xml, map, "rollback");

        xml.writeCharacters(INDENT2);
        xml.writeEndElement();
        xml.writeCharacters(NL);
    }


    /**
     * Write entries (changes or rollback) as child elements.
     *
     * @param xml the XML stream writer
     * @param map the changeset map
     * @param key the key ("changes" or "rollback")
     * @throws Exception if writing fails
     */
    @SuppressWarnings("unchecked")
    private void writeEntries(XMLStreamWriter xml, Map<String, Object> map, String key) throws Exception {
        Object entriesObj = map.get(key);
        if (entriesObj == null) {
            return;
        }

        List<Object> entries = (List<Object>) entriesObj;
        for (Object entryObj : entries) {
            Map<String, Object> entryMap = (Map<String, Object>) entryObj;
            for (Map.Entry<String, Object> entry : entryMap.entrySet()) {
                xml.writeCharacters(INDENT4);
                writeEntryElement(xml, entry.getKey(), entry.getValue());
                xml.writeCharacters(NL);
            }
        }
    }


    /**
     * Write a single entry element.
     *
     * @param xml the XML stream writer
     * @param elementName the element name
     * @param value the value (String or Map)
     * @throws Exception if writing fails
     */
    @SuppressWarnings("unchecked")
    private void writeEntryElement(XMLStreamWriter xml, String elementName, Object value) throws Exception {
        if (value instanceof String) {
            xml.writeStartElement(elementName);
            xml.writeCharacters((String) value);
            xml.writeEndElement();
        } else if (value instanceof Map) {
            xml.writeStartElement(elementName);
            Map<String, Object> attrs = (Map<String, Object>) value;
            for (Map.Entry<String, Object> attr : attrs.entrySet()) {
                if (attr.getValue() instanceof List) {
                    writeListAsChildElements(xml, attr.getKey(), (List<Object>) attr.getValue());
                } else if (attr.getValue() instanceof Map) {
                    xml.writeCharacters(INDENT6);
                    writeEntryElement(xml, attr.getKey(), attr.getValue());
                } else {
                    xml.writeAttribute(attr.getKey(), String.valueOf(attr.getValue()));
                }
            }
            xml.writeEndElement();
        }
    }


    /**
     * Write a list as child elements.
     *
     * @param xml the XML stream writer
     * @param elementName the element name for each item
     * @param items the list items
     * @throws Exception if writing fails
     */
    @SuppressWarnings("unchecked")
    private void writeListAsChildElements(XMLStreamWriter xml, String elementName, List<Object> items) throws Exception {
        String singularName = singularize(elementName);
        for (Object item : items) {
            xml.writeCharacters(INDENT6);
            if (item instanceof Map) {
                writeEntryElement(xml, singularName, item);
            } else {
                xml.writeStartElement(singularName);
                xml.writeCharacters(String.valueOf(item));
                xml.writeEndElement();
            }
        }
    }


    /**
     * Write an optional attribute if present in the map.
     *
     * @param xml the XML stream writer
     * @param map the map
     * @param key the key
     * @throws Exception if writing fails
     */
    private void writeOptionalAttribute(XMLStreamWriter xml, Map<String, Object> map, String key) throws Exception {
        Object value = map.get(key);
        if (value != null) {
            xml.writeAttribute(key, value.toString());
        }
    }


    /**
     * Join a list value as a comma-separated string.
     *
     * @param value the value (List or String)
     * @return the joined string
     */
    @SuppressWarnings("unchecked")
    private String joinList(Object value) {
        if (value instanceof List) {
            return String.join(",", (List<String>) value);
        }
        return String.valueOf(value);
    }


    /**
     * Get singular form of an element name by removing trailing 's'.
     *
     * @param name the plural name
     * @return the singular name
     */
    private String singularize(String name) {
        if (name.endsWith("s")) {
            return name.substring(0, name.length() - 1);
        }
        return name;
    }
}
