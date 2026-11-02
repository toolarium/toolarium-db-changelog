/*
 * ChangelogRollbackPolicy.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog.config;


/**
 * Controls behavior when a changeset has no rollback available during a rollback operation.
 *
 * @author patrick
 */
public enum ChangelogRollbackPolicy {

    /** Log a warning and skip the changeset (default). */
    WARN,

    /** Fail the entire rollback operation. */
    FAIL,

    /** Silently skip the changeset without warning. */
    IGNORE
}
