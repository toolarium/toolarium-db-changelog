/*
 * IChangelogColumnDefinition.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog.dto.column;


/**
 * Defines a column for createTable or addColumn change types.
 *
 * @author patrick
 */
public interface IChangelogColumnDefinition {

    /**
     * Get the column name.
     *
     * @return the name
     */
    String getName();


    /**
     * Get the column type.
     *
     * @return the type
     */
    String getType();


    /**
     * Check if the column is auto-increment.
     *
     * @return true if auto-increment
     */
    boolean isAutoIncrement();


    /**
     * Check if the column is nullable.
     *
     * @return true if nullable (default true)
     */
    boolean isNullable();


    /**
     * Check if the column is a primary key.
     *
     * @return true if primary key
     */
    boolean isPrimaryKey();


    /**
     * Check if the column has a unique constraint.
     *
     * @return true if unique
     */
    boolean isUnique();


    /**
     * Get the default value.
     *
     * @return the default value, or null
     */
    String getDefaultValue();


    /**
     * Get the computed default value (e.g., CURRENT_TIMESTAMP).
     *
     * @return the computed default, or null
     */
    String getDefaultValueComputed();
}
