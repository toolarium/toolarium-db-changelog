[![License](https://img.shields.io/github/license/toolarium/toolarium-db-changelog)](https://github.com/toolarium/toolarium-db-changelog/blob/master/LICENSE)
[
[![Maven Central](https://img.shields.io/maven-central/v/com.github.toolarium/toolarium-db-changelog/0.8.0)](https://search.maven.org/artifact/com.github.toolarium/toolarium-db-changelog/0.8.0/jar)
[![javadoc](https://javadoc.io/badge2/com.github.toolarium/toolarium-db-changelog/javadoc.svg)](https://javadoc.io/doc/com.github.toolarium/toolarium-db-changelog)

# toolarium-db-changelog

An open-source Java library for automated database schema migrations and rollbacks, committed to remaining open source. A modern alternative to Liquibase and Flyway with no commercial restrictions: rollback, dry-run, preconditions, and all advanced features are free and built-in, forever. Provides its own clean changelog format (`.db.yaml`, `.db.xml`, `.db.json`) and can import existing Liquibase and Flyway changelogs for seamless migration. Distributed locking is handled by [toolarium-leader-election](https://github.com/toolarium/toolarium-leader-election).


## Features

- **Automated migrations**: apply, rollback, tag, sync, validate, and dry-run changesets
- **Structured DDL changes**: createTable, dropTable, addColumn, dropColumn, renameTable, renameColumn, modifyColumn, createIndex, dropIndex with auto-rollback
- **Constraints**: addPrimaryKey, dropPrimaryKey, addForeignKey, dropForeignKey, addUniqueConstraint, dropUniqueConstraint, addNotNull, dropNotNull
- **Sequences and views**: createSequence, dropSequence, createView, dropView
- **Preconditions**: dbms, tableExists, columnExists, tableIsEmpty, rowCount, changeLogPropertyDefined, sqlCheck, and/or/not combinators with HALT/WARN/CONTINUE/MARK_RAN
- **Own changelog format**: clean `.db.yaml`, `.db.xml`, and `.db.json` with property substitution
- **Property resolution**: `${name}` from changelog properties, system properties, and environment variables via toolarium-common `PropertyExpander`
- **External format import**: parse Liquibase (XML, YAML, JSON, SQL) and Flyway (`V*__*.sql`, `R__*.sql`) changelogs
- **Format migration**: convert Liquibase and Flyway changelogs to `.db.yaml`/`.db.xml`/`.db.json` via `IChangelogMigrator`
- **Pre-execution snapshots**: `DB_CHANGELOG_SNAPSHOT` table for capturing table metadata before changes
- **Distributed locking**: via [toolarium-leader-election](https://github.com/toolarium/toolarium-leader-election) (self-healing, no stale locks)
- **SHA-256 checksums**: stable algorithm, no version prefix
- **DML change types**: insert, update, delete, tagDatabase, setTableRemarks, setColumnRemarks, loadData, loadUpdateData
- **Custom changes**: user-provided migration logic via `ICustomChangelogEntry`
- **Context and label filtering**: conditional changeset execution based on active contexts and labels
- **Sync operation**: mark all pending changesets as executed without running (baselining)
- **Rollback automation**: auto-rollback for DDL, Level 1 SQL rollback generator for raw SQL, dry-run via `generateRollbackSql()`
- **Resilient rollback**: continues on partial failure, reports all errors
- **Configurable policies**: snapshot policy, rollback policy, object quoting strategy
- **Changelog writing**: serialize changelog models to `.db.yaml`, `.db.xml`, and `.db.json` via `IChangelogWriter` with round-trip support
- **Pluggable resource resolver**: unified resolution across classpath, JARs, and file system via `IChangelogResourceResolver`
- **Security hardened**: SQL identifier validation, XXE protection, safe YAML deserialization, statement timeouts
- **14 tested databases**: H2, HSQLDB, Derby, SQLite (embedded), PostgreSQL, MySQL, MariaDB, Oracle, SQL Server, CockroachDB, DB2, Firebird, TiDB, YugabyteDB
- **Additional databases**: any JDBC-compliant database via generic ANSI SQL (not tested: Snowflake, SAP HANA, Sybase, Informix)


## Adding the library

**Gradle**
```gradle
implementation 'com.github.toolarium:toolarium-db-changelog:0.8.0'
```

**Maven**
```xml
<dependency>
    <groupId>com.github.toolarium</groupId>
    <artifactId>toolarium-db-changelog</artifactId>
    <version>0.8.0</version>
</dependency>
```

The library brings `slf4j-api`, `snakeyaml`, and `jackson-databind` as transitive dependencies. You must provide an SLF4J binding (e.g., Logback) at runtime.


## Quick start

```java
// Minimal usage
IChangelogRunner runner = ChangelogRunnerBuilder.builder()
    .dataSource(dataSource)
    .changelog("classpath:db/changelog/changelog-master.db.yaml")
    .build();

runner.update();
```

```java
// With context/label filtering and distributed locking
IChangelogRunner runner = ChangelogRunnerBuilder.builder()
    .dataSource(dataSource)
    .changelog("classpath:db/changelog/changelog-master.db.yaml")
    .contexts(List.of("production", "shared"))
    .labels(List.of("v1.0"))
    .leaderElection(true)
    .build();

runner.update();
```


**Example changelog (YAML)** (`changelog-master.db.yaml`):
```yaml
format: toolarium-db-changelog/1.0
properties:
  prefix: app_

changesets:
  - id: create-users
    changes:
      - createTable:
          tableName: ${prefix}users
          columns:
            - name: id
              type: BIGINT
              autoIncrement: true
              constraints: { primaryKey: true, nullable: false }
            - name: name
              type: VARCHAR(100)
              constraints: { nullable: false }
            - name: email
              type: VARCHAR(255)
              constraints: { unique: true }

  - id: add-phone
    changes:
      - addColumn:
          tableName: ${prefix}users
          columns:
            - name: phone
              type: VARCHAR(20)

  - id: create-email-index
    changes:
      - createIndex:
          tableName: ${prefix}users
          indexName: idx_users_email
          columns: [email]

  - id: raw-sql-example
    changes:
      - sql: "INSERT INTO ${prefix}users (name, email) VALUES ('Admin', 'admin@example.com')"
    rollback:
      - sql: "DELETE FROM ${prefix}users WHERE email = 'admin@example.com'"
```

**Example changelog (XML)** (`changelog-master.db.xml`):
```xml
<?xml version="1.0" encoding="UTF-8"?>
<dbChangelog format="toolarium-db-changelog/1.0">
    <properties>
        <property name="prefix" value="app_"/>
    </properties>

    <changesets>
        <changeset id="create-users">
            <changes>
                <sql>CREATE TABLE ${prefix}users (id INT PRIMARY KEY, name VARCHAR(100))</sql>
            </changes>
            <rollback>
                <sql>DROP TABLE ${prefix}users</sql>
            </rollback>
        </changeset>

        <changeset id="add-email">
            <changes>
                <sql>ALTER TABLE ${prefix}users ADD COLUMN email VARCHAR(255)</sql>
            </changes>
        </changeset>
    </changesets>
</dbChangelog>
```


## API

```java
runner.update();                         // apply all pending changesets
runner.update(5);                        // apply up to 5 changesets
runner.rollback(3);                      // rollback last 3 changesets
runner.rollback("v1.0");                 // rollback to tag
runner.rollback(Instant.parse("..."));   // rollback to point in time
runner.generateRollbackSql(3);           // dry-run: rollback SQL without executing
runner.tag("v1.0");                      // tag current state
runner.sync();                           // mark all pending as executed (baselining)
runner.status();                         // list pending and applied changesets
runner.history();                        // list applied changesets
runner.validate();                       // check for checksum mismatches
runner.clearChecksums();                 // reset checksums
```


## Changeset attributes

Each changeset supports the following attributes:

| Attribute | Default | Description |
|---|---|---|
| `id` | (required) | Unique identifier for the changeset |
| `author` | OS username | Author of the changeset |
| `filePath` | resource location | Overrides the resource location in the changeset identity (see below) |
| `comment` | null | Descriptive comment |
| `runAlways` | false | Re-execute on every run |
| `runOnChange` | false | Re-execute when checksum changes |
| `failOnError` | true | Halt on execution error |
| `runInTransaction` | true | Wrap in a database transaction |
| `context` | (none) | Context filter (comma-separated in XML, list in YAML/JSON) |
| `labels` | (none) | Label filter |

### filePath override

A changeset is identified by the triple `(id, author, filePath)`. By default, `filePath` is the resource location where the changeset was loaded from (classpath or file system path). All paths are normalized automatically.

If you reorganize your changelog files after changesets have been applied in production, the resource location changes and the runner would treat them as new changesets. The `filePath` attribute overrides the resource location in the identity to keep it stable.

**YAML:**
```yaml
changesets:
  - id: create-users
    filePath: db/changelogs/001-initial.db.yaml
    changes:
      - createTable:
          tableName: users
          columns:
            - name: id
              type: INT
```

**XML:**
```xml
<changeset id="create-users" filePath="db/changelogs/001-initial.db.xml">
    <changes>
        <sql>CREATE TABLE users (id INT PRIMARY KEY)</sql>
    </changes>
</changeset>
```

**JSON:**
```json
{
  "id": "create-users",
  "filePath": "db/changelogs/001-initial.db.json",
  "changes": [{"sql": "CREATE TABLE users (id INT PRIMARY KEY)"}]
}
```


## Resource resolution

Changelog files are resolved via `IChangelogResourceResolver` in this order:

1. Classpath scanning via [toolarium-common](https://github.com/toolarium/toolarium-common) ClassPathUtil (finds resources in JARs, classpath directories)
2. Thread context classloader and class classloader
3. File system (unless `classpath:` prefix is used)

All paths are normalized automatically. The `classpath:` prefix restricts resolution to classpath only.

```java
// Custom resource resolver
IChangelogRunner runner = ChangelogRunnerBuilder.builder()
    .dataSource(dataSource)
    .changelog("db/changelog.db.yaml")
    .resourceResolver(customResolver)
    .build();
```


## Supported changelog formats

| Format | Extension | Description |
|---|---|---|
| DB-Changelog YAML | `.db.yaml` / `.db.yml` | Recommended format |
| DB-Changelog XML | `.db.xml` | XML format |
| DB-Changelog JSON | `.db.json` | JSON format |
| External SQL | `.sql` | Formatted SQL with `--changeset` directives |


## Writing changelogs programmatically

```java
// Build a changelog model
Changelog changelog = new Changelog("output.db.yaml", List.of(
    new ChangelogChangeSet(
        new ChangelogChangeSetIdentity("create-users", "admin", "output.db.yaml"),
        List.of(new CreateTableChangelogEntry("users", List.of(
            new ChangelogColumnDefinition("id", "INT"),
            new ChangelogColumnDefinition("name", "VARCHAR(100)")
        )))
    )
));

// Write to YAML
IChangelogWriter writer = ChangelogWriterFactory.getInstance().getYamlWriter();
try (OutputStream out = new FileOutputStream("output.db.yaml")) {
    writer.write(changelog, out);
}

// Write to JSON or XML
ChangelogWriterFactory.getInstance().getWriter(".db.json").write(changelog, outputStream);
ChangelogWriterFactory.getInstance().getWriter(".db.xml").write(changelog, outputStream);
```


## Related Projects

| Project | Description |
|---|---|
| [toolarium-db-changelog-spring-boot-starter](https://github.com/toolarium/toolarium-db-changelog-spring-boot-starter) | Spring Boot auto-configuration and starter for toolarium-db-changelog |
| [toolarium-db-changelog-gradle-plugin](https://github.com/toolarium/toolarium-db-changelog-gradle-plugin) | Gradle plugin for running migrations, generating changelogs, and validating schemas |
| [toolarium-db-changelog-cli](https://github.com/toolarium/toolarium-db-changelog-cli) | Command-line interface for running migrations, rollbacks, and format conversion |

All related projects are released in v0.8.0.


## Built With

* [cb](https://github.com/toolarium/common-build) - The toolarium common build


## Versioning

We use [SemVer](http://semver.org/) for versioning. For the versions available, see the [tags on this repository](https://github.com/toolarium/toolarium-db-changelog/tags).


## License

This project is licensed under the MPL-2.0 License, see the [LICENSE](LICENSE) file for details.

| Action | Allowed | Required | Not Allowed |
|---|---|---|---|
| Commercial use | Yes | | |
| Private use | Yes | | |
| Modify source | Yes | | |
| Distribute | Yes | | |
| Mix with proprietary code | Yes | | |
| Share modified MPL files | | Yes | |
| Keep license notices | | Yes | |
| Remove copyright notices | | | Yes |
| Hold authors liable | | | Yes |
| Use authors' trademarks | | | Yes |
