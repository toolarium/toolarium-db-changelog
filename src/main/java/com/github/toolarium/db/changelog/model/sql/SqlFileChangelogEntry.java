/*
 * SqlFileChangelogEntry.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog.model.sql;


/**
 * Executes SQL loaded from an external file. Extends SqlChangelogEntry
 * with file-specific attributes (path, encoding, relative resolution).
 *
 * @author patrick
 */
public class SqlFileChangelogEntry extends SqlChangelogEntry {
    private final String path;
    private final String encoding;
    private final boolean relativeToChangelogFile;


    /**
     * Constructor for SqlFileChangelogEntry
     *
     * @param path the path to the SQL file
     */
    public SqlFileChangelogEntry(String path) {
        this(path, true, ";", false, "UTF-8", true, null);
    }


    /**
     * Constructor for SqlFileChangelogEntry
     *
     * @param path the path to the SQL file
     * @param splitStatements whether to split on delimiter
     * @param endDelimiter the statement delimiter
     * @param stripComments whether to strip SQL comments
     * @param encoding the file encoding
     * @param relativeToChangelogFile whether path is relative to changelog
     * @param dbms the database type restriction, or null for all
     */
    public SqlFileChangelogEntry(String path, boolean splitStatements, String endDelimiter,
                                 boolean stripComments, String encoding,
                                 boolean relativeToChangelogFile, String dbms) {
        super(null, splitStatements, endDelimiter, stripComments, dbms);
        this.path = path;
        this.encoding = encoding;
        this.relativeToChangelogFile = relativeToChangelogFile;
    }


    /**
     * Get the path to the SQL file.
     *
     * @return the path
     */
    public String getPath() {
        return path;
    }


    /**
     * Get the file encoding.
     *
     * @return the encoding
     */
    public String getEncoding() {
        return encoding;
    }


    /**
     * Check if the path is relative to the changelog file.
     *
     * @return true if relative to changelog
     */
    public boolean isRelativeToChangelogFile() {
        return relativeToChangelogFile;
    }


    /**
     * @see com.github.toolarium.db.changelog.model.sql.SqlChangelogEntry#getDescription()
     */
    @Override
    public String getDescription() {
        return "sqlFile: " + path;
    }


    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return java.util.Objects.hash(path);
    }


    /**
     * @see java.lang.Object#equals(Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        return java.util.Objects.equals(path, ((SqlFileChangelogEntry) obj).path);
    }


    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return getDescription();
    }
}
