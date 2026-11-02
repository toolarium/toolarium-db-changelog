/*
 * ChangelogObjectQuotingStrategy.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog.config;


/**
 * Controls whether SQL identifiers (table names, column names) are quoted in generated SQL.
 *
 * @author patrick
 */
public enum ChangelogObjectQuotingStrategy {

    /** Do not quote identifiers (default). Identifiers are used as-is. */
    NONE,

    /** Quote all identifiers with double quotes (ANSI standard). */
    QUOTE_ALL
}
