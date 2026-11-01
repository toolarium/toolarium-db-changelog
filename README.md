[![License: MPL-2.0](https://img.shields.io/badge/License-MPL%202.0-brightgreen.svg)](https://github.com/toolarium/toolarium-db-changelog/blob/master/LICENSE)
[![Maven Central](https://img.shields.io/maven-central/v/com.github.toolarium/toolarium-db-changelog/0.0.1)](https://search.maven.org/artifact/com.github.toolarium/toolarium-db-changelog/0.0.1/jar)
[![javadoc](https://javadoc.io/badge2/com.github.toolarium/toolarium-db-changelog/javadoc.svg)](https://javadoc.io/doc/com.github.toolarium/toolarium-db-changelog)

# toolarium-db-changelog

A Java library for automated database schema migrations and rollbacks. It provides its own changelog format (`.db.yaml`, `.db.xml`, `.db.json`) and can import external changelog formats for migration compatibility. Distributed locking is handled by [toolarium-leader-election](https://github.com/toolarium/toolarium-leader-election).


## Features

- **Automated migrations**: apply, rollback, tag, sync, validate, and dry-run changesets
- **Structured DDL changes**: createTable, dropTable, addColumn, dropColumn, renameTable, renameColumn, modifyColumn, createIndex, dropIndex with auto-rollback
- **Constraints**: addPrimaryKey, dropPrimaryKey, addForeignKey, dropForeignKey, addUniqueConstraint, dropUniqueConstraint, addNotNull, dropNotNull
- **Sequences and views**: createSequence, dropSequence, createView, dropView
- **Own changelog format**: clean `.db.yaml`, `.db.xml`, and `.db.json` with property substitution
- **External format import**: parse and migrate from external changelog formats (XML, YAML, JSON, SQL)
- **Distributed locking**: via `toolarium-leader-election` (self-healing, no stale locks)
- **SHA-256 checksums**: stable algorithm, no version prefix
- **Rollback automation**: auto-rollback for DDL, Level 1 SQL rollback generator for raw SQL, pre-execution snapshots, validation enforcement
- **All major databases**: PostgreSQL, MySQL, MariaDB, Oracle, SQL Server, H2, DB2, and more


## Adding the library

**Gradle**
```gradle
implementation 'com.github.toolarium:toolarium-db-changelog:0.0.1'
```

**Maven**
```xml
<dependency>
    <groupId>com.github.toolarium</groupId>
    <artifactId>toolarium-db-changelog</artifactId>
    <version>0.0.1</version>
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
runner.tag("v1.0");                      // tag current state
runner.sync();                           // mark all pending as executed
runner.history();                        // list applied changesets
runner.clearChecksums();                 // reset checksums
```


## Supported changelog formats

| Format | Extension | Description |
|---|---|---|
| DB-Changelog YAML | `.db.yaml` / `.db.yml` | Recommended native format |
| DB-Changelog XML | `.db.xml` | Native XML format |
| DB-Changelog JSON | `.db.json` | Native JSON format |
| External SQL | `.sql` | Formatted SQL with `--changeset` directives |


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
