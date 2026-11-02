# toolarium-db-changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [ 0.0.1 ] - 2026-11-02
### Added
- Core changelog runner with update, tag, sync, history, and clearChecksums operations.
- SHA 256 based checksum generation for changesets.
- JDBC tracking table (`DB_CHANGELOG`) with auto-creation.
- Changelog parsing for DB-Changelog YAML (`.db.yaml`), XML (`.db.xml`), and JSON (`.db.json`) formats.
- External SQL changelog format import (`.sql` with `--changeset` directives).
- Property substitution (`${property}`) in changelogs.
- Include/includeAll resolution for composing changelogs from multiple files.
- Parser factory with automatic format detection by file extension.
- Structured DDL change types: createTable, dropTable, addColumn, dropColumn, createIndex, dropIndex, renameTable, renameColumn, modifyColumn.
- Constraint change types: addPrimaryKey, dropPrimaryKey, addForeignKey, dropForeignKey, addUniqueConstraint, dropUniqueConstraint, addNotNull, dropNotNull.
- Sequence change types: createSequence, dropSequence.
- View change types: createView, dropView.
- Auto-rollback generation for structured DDL changes.
- Generic ANSI SQL generator for all DDL change types.
- Level 1 SQL rollback generator: auto-generates rollback for common raw SQL DDL patterns.
- Rollback engine: rollback by count, tag, or date.
- Rollback priority: manual rollback entries, auto-rollback, Level 1 SQL rollback.
- Dry-run rollback via `generateRollbackSql()`.
- Preconditions: dbms, tableExists, columnExists, indexExists, viewExists, sequenceExists, foreignKeyExists, primaryKeyExists, sqlCheck, changeSetExecuted, runningAs.
- Precondition logical combinators: and, or, not.
- Precondition failure actions: HALT, WARN, CONTINUE, MARK_RAN.
- Preconditions integrated into execution engine with configurable failure actions.
- Precondition parsing from YAML changelogs.
- DML change types: insert, update, delete, tagDatabase.
- `IChangelogContext` interface for runtime execution context (active contexts and labels).
- Context and label filtering for conditional changeset execution.
- Sync operation: mark all pending changesets as executed without running.
- Support for runAlways, runOnChange, and failOnError changeset attributes.
- Distributed locking via `toolarium-leader-election` with DATABASE strategy.
- No-op lock fallback for single-instance deployments.
- Builder-based configuration via `ChangelogRunnerBuilder`.
- Status operation: list pending and applied changesets.
