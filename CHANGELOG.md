# toolarium-db-changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [ 0.0.1 ] - 2026-11-01
### Added
- Core changelog runner with update, tag, sync, history, and clearChecksums operations.
- SHA 256 based checksum generation for changesets.
- JDBC tracking table (`DB_CHANGELOG`) with auto-creation.
- Changelog parsing for DB-Changelog YAML (`.db.yaml`), XML (`.db.xml`), and JSON (`.db.json`) formats.
- External SQL changelog format import (`.sql` with `--changeset` directives).
- Property substitution (`${property}`) in changelogs.
- Include/includeAll resolution for composing changelogs from multiple files.
- Parser factory with automatic format detection by file extension.
- Structured DDL change types: createTable, dropTable, addColumn, dropColumn, createIndex, dropIndex.
- Auto-rollback generation for structured DDL changes.
- Generic ANSI SQL generator for DDL change types.
- Support for runAlways, runOnChange, and failOnError changeset attributes.
- No-op lock implementation (leader-election integration planned).
- Builder-based configuration via `ChangelogRunnerBuilder`.
