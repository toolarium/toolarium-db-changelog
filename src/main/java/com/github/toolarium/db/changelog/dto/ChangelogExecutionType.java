/*
 * ChangelogExecutionType.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog.dto;


/**
 * Execution type recorded in the tracking table.
 *
 * @author patrick
 */
public enum ChangelogExecutionType {

    /** Normal successful execution. */
    EXECUTED,

    /** Re-executed due to runAlways or runOnChange. */
    RERAN,

    /** Rolled back. */
    ROLLED_BACK,

    /** Skipped due to precondition. */
    SKIPPED,

    /** Marked as ran without actual execution (sync). */
    MARK_RAN
}
