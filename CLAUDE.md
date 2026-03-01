# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Purpose

This library provides type-safe, composable JPQL queries via QueryDSL with built-in pagination and sorting support. Callers use the `JPQLQueryBuilder<T>` functional interface to construct queries using QueryDSL's `JPAQueryFactory` API, then pass the builder to `QueryTemplate` which handles execution, ordering, and pagination transparently.

## Build & Test Commands

```bash
# Build the project
mvn clean install

# Run all tests
mvn test

# Run a single test class
mvn test -Dtest=QueryTemplateImplTest

# Run a single test method
mvn test -Dtest=QueryTemplateImplTest#testFind

# Package without running tests
mvn package -DskipTests
```

## Architecture Overview

This is a small Java library (package `com.alexlitovsky.jpql.pagination`, groupId `com.alexlitovsky`) that provides a CDI-injectable wrapper around JPA/Hibernate for executing QueryDSL fluent queries with sorting and pagination.

### Key types

**`JPQLQueryBuilder<T>`** — top-level functional interface: `JPQLQuery<T> createQuery(JPQLQueryFactory)`
- Callers define the select, from, join, and where clauses; sorting and pagination are left to the executor
- The same lambda can be passed to `find`, `count`, or `apply` without modification

**`QueryTemplate`** — executor interface
- `find` — fetch a list, with optional sort and pagination
- `count` — count matching results
- `apply` — stream the full result set through a `Consumer`, clearing the persistence context after each entity

**`QueryTemplateImpl`** — `@ApplicationScoped` CDI implementation with `@PersistenceContext(unitName = "default")`, also exposes a package-private constructor accepting `EntityManager` for direct use in tests

### `apply` pattern
These methods stream results and call `entityManager.clear()` after each entity is processed — intended for bulk/batch processing where holding all entities in memory at once would be costly.

### Test setup
Tests use Apache Derby (in-memory) via `JpaTestBase`, which creates/destroys the DB per test class. The `unit-test` persistence unit is defined in `src/test/resources/META-INF/persistence.xml`. QueryDSL Q-types for test entities are generated at build time into `target/generated-test-sources/java` by the `apt-maven-plugin`.

### Key test class
- `QueryTemplateImplTest` — covers fetch, sorting (single and multi-field), pagination, count, and `apply` variants

### Key dependencies
- `querydsl-jpa` / `querydsl-core` (`io.github.openfeign.querydsl` fork, v6.x) — compile-scope
- Hibernate 6.x and Jakarta Persistence 3.1 — provided scope
- Jakarta CDI 4 / Inject 2 / Annotation 2 — provided scope
