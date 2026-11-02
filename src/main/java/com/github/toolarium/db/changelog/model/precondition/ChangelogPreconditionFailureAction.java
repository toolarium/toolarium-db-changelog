/*
 * ChangelogPreconditionFailureAction.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog.model.precondition;


/**
 * Action to take when a precondition fails.
 *
 * @author patrick
 */
public enum ChangelogPreconditionFailureAction {

    /** Stop execution with error (default). */
    HALT,

    /** Log warning and skip the changeset. */
    WARN,

    /** Silently skip the changeset. */
    CONTINUE,

    /** Skip execution but record as executed in tracking table. */
    MARK_RAN
}
