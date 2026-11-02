/*
 * ChangelogSnapshotPolicy.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.db.changelog.config;


/**
 * Controls when pre-execution snapshots are captured for rollback support.
 *
 * @author patrick
 */
public enum ChangelogSnapshotPolicy {

    /** Automatically snapshot DDL changes only. */
    AUTO,

    /** Snapshot only DDL changes (createTable, addColumn, etc.). */
    DDL,

    /** Snapshot all changes including DML. */
    ALL,

    /** Disable snapshots entirely. */
    NONE
}
