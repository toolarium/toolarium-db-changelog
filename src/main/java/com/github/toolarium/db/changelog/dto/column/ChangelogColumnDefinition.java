/*
 * ChangelogColumnDefinition.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog.dto.column;


import java.util.Objects;


/**
 * Defines a column for createTable or addColumn change types.
 *
 * @author patrick
 */
public class ChangelogColumnDefinition implements IChangelogColumnDefinition {
    private final String name;
    private final String type;
    private boolean autoIncrement;
    private boolean nullable;
    private boolean primaryKey;
    private boolean unique;
    private String defaultValue;
    private String defaultValueComputed;


    /**
     * Constructor for ChangelogColumnDefinition
     *
     * @param name the column name
     * @param type the column type (e.g., "bigint", "varchar(255)")
     */
    public ChangelogColumnDefinition(String name, String type) {
        this.name = name;
        this.type = type;
        this.nullable = true;
    }


    /**
     * Get the column name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }


    /**
     * Get the column type.
     *
     * @return the type
     */
    public String getType() {
        return type;
    }


    /**
     * Check if the column is auto-increment.
     *
     * @return true if auto-increment
     */
    public boolean isAutoIncrement() {
        return autoIncrement;
    }


    /**
     * Set auto-increment.
     *
     * @param autoIncrement true to enable auto-increment
     * @return this definition for chaining
     */
    public ChangelogColumnDefinition setAutoIncrement(boolean autoIncrement) {
        this.autoIncrement = autoIncrement;
        return this;
    }


    /**
     * Check if the column is nullable.
     *
     * @return true if nullable (default true)
     */
    public boolean isNullable() {
        return nullable;
    }


    /**
     * Set nullable.
     *
     * @param nullable true if nullable
     * @return this definition for chaining
     */
    public ChangelogColumnDefinition setNullable(boolean nullable) {
        this.nullable = nullable;
        return this;
    }


    /**
     * Check if the column is a primary key.
     *
     * @return true if primary key
     */
    public boolean isPrimaryKey() {
        return primaryKey;
    }


    /**
     * Set primary key.
     *
     * @param primaryKey true if primary key
     * @return this definition for chaining
     */
    public ChangelogColumnDefinition setPrimaryKey(boolean primaryKey) {
        this.primaryKey = primaryKey;
        return this;
    }


    /**
     * Check if the column has a unique constraint.
     *
     * @return true if unique
     */
    public boolean isUnique() {
        return unique;
    }


    /**
     * Set unique constraint.
     *
     * @param unique true if unique
     * @return this definition for chaining
     */
    public ChangelogColumnDefinition setUnique(boolean unique) {
        this.unique = unique;
        return this;
    }


    /**
     * Get the default value.
     *
     * @return the default value, or null
     */
    public String getDefaultValue() {
        return defaultValue;
    }


    /**
     * Set the default value.
     *
     * @param defaultValue the default value
     * @return this definition for chaining
     */
    public ChangelogColumnDefinition setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }


    /**
     * Get the computed default value (e.g., CURRENT_TIMESTAMP).
     *
     * @return the computed default, or null
     */
    public String getDefaultValueComputed() {
        return defaultValueComputed;
    }


    /**
     * Set the computed default value.
     *
     * @param defaultValueComputed the computed default
     * @return this definition for chaining
     */
    public ChangelogColumnDefinition setDefaultValueComputed(String defaultValueComputed) {
        this.defaultValueComputed = defaultValueComputed;
        return this;
    }


    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return Objects.hash(name, type);
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
        ChangelogColumnDefinition other = (ChangelogColumnDefinition) obj;
        return Objects.equals(name, other.name) && Objects.equals(type, other.type);
    }


    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return name + " " + type;
    }
}
