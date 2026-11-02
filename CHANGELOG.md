# toolarium-db-changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [ 0.8.1 ] - 2026-11-02

## [ 0.8.0 ] - 2026-11-02
### Added
- Core changelog runner with update, rollback, tag, sync, validate, status, history, and clearChecksums.
- SHA 256 checksum generation for changesets.
- JDBC tracking table (`DB_CHANGELOG`) with auto-creation.
- Native changelog formats: `.db.yaml`, `.db.xml`, `.db.json` with property substitution and include resolution.
- Structured DDL: createTable, dropTable, addColumn, dropColumn, createIndex, dropIndex, renameTable, renameColumn, modifyColumn.
- Constraints: addPrimaryKey, dropPrimaryKey, addForeignKey, dropForeignKey, addUniqueConstraint, dropUniqueConstraint, addNotNull, dropNotNull.
- Sequences: createSequence, dropSequence. Views: createView, dropView.
- DML: insert, update, delete, tagDatabase, setTableRemarks, setColumnRemarks, loadData, loadUpdateData.
- Auto-rollback for structured DDL changes, Level 1 SQL rollback for raw SQL patterns.
- Rollback by count, tag, or date. Dry-run via `generateRollbackSql()`.
- Preconditions: dbms, tableExists, columnExists, indexExists, viewExists, sequenceExists, foreignKeyExists, primaryKeyExists, sqlCheck, changeSetExecuted, runningAs, tableIsEmpty, rowCount, changeLogPropertyDefined.
- Precondition combinators (and, or, not) with HALT, WARN, CONTINUE, MARK_RAN failure actions.
- Context and label filtering for conditional changeset execution.
- Distributed locking via `toolarium-leader-election` (DATABASE strategy, no-op fallback).
- Builder-based configuration via `ChangelogRunnerBuilder` with pluggable interfaces.
- Security: SQL identifier validation, safe YAML (SafeConstructor), XXE protection, statement timeouts, default value escaping, path normalization.
- `IChangelogResourceResolver` for unified resolution across classpath, JARs, and file system.
- `ICustomChangelogEntry` for user-provided migration logic.
- `IChangelogSnapshotManager` for pre-execution metadata snapshots (`DB_CHANGELOG_SNAPSHOT`).
- Liquibase changelog import (XML, YAML, JSON, SQL with `--changeset` directives).
- Flyway SQL import (`V*__*.sql` versioned, `R__*.sql` repeatable migrations).
- Format migration via `IChangelogMigrator` to convert Liquibase and Flyway changelogs to native format.
- Changelog writers (`IChangelogWriter`) for serializing models to YAML, XML, and JSON with round-trip support.
- Property resolution via toolarium-common `PropertyExpander` (changelog properties, system properties, environment variables).
- Database-portable DDL with automatic database detection and adaptation.
- Integration tested across 14 databases: H2, HSQLDB, Derby, SQLite, PostgreSQL, MySQL, MariaDB, Oracle, SQL Server, CockroachDB, DB2, Firebird, TiDB, YugabyteDB.
