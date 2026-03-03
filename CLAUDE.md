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

This is a small Java library (package `io.github.alterioncorp.jpql.pagination`, groupId `io.github.alterioncorp`) that provides a CDI-injectable wrapper around JPA/Hibernate for executing QueryDSL fluent queries with sorting and pagination.

### Key types

**`JPQLQueryBuilder<T>`** — top-level functional interface: `JPQLQuery<T> createQuery(JPQLQueryFactory)`
- Callers define the select, from, join, and where clauses; sorting and pagination are left to the executor
- The same lambda can be passed to `find`, `count`, or `apply` without modification

**`QueryTemplate`** — executor interface

`find` overloads:
- `find(queryBuilder)` — all results, order from query
- `find(queryBuilder, sort)` — all results, single `OrderSpecifier`
- `find(queryBuilder, sort[])` — all results, multiple `OrderSpecifier`s
- `find(queryBuilder, sort, offset, limit)` — paginated, single sort
- `find(queryBuilder, sort[], offset, limit)` — paginated, multiple sorts

`count`:
- `count(queryBuilder)` — returns `long`

`apply` overloads (stream results, clear context after each entity):
- `apply(queryBuilder, consumer)` — order from query
- `apply(queryBuilder, sort, consumer)` — single sort
- `apply(queryBuilder, sort[], consumer)` — multiple sorts
- `apply(queryBuilder, sort, offset, limit, consumer)` — paginated, single sort
- `apply(queryBuilder, sort[], offset, limit, consumer)` — paginated, multiple sorts

**`QueryTemplateImpl`** — `@ApplicationScoped` CDI implementation with `@PersistenceContext(unitName = "default")`, also exposes a public constructor accepting `EntityManager` for direct use in tests

### `apply` pattern
These methods stream results and call `entityManager.clear()` after each entity is processed — intended for bulk/batch processing where holding all entities in memory at once would be costly.

### Test setup
Tests use Apache Derby (in-memory) via `JpaTestBase`, which creates/destroys the DB per test class. The `unit-test` persistence unit is defined in `src/test/resources/META-INF/persistence.xml`. QueryDSL Q-types for test entities are generated at build time into `target/generated-test-sources/java` by the `maven-compiler-plugin` (execution id `default-testCompile`) using `querydsl-apt:jakarta` as an annotation processor path, with the `querydsl.packageSuffix=.path` option — so Q-types land in `*.path` sub-packages (e.g., `io.github.alterioncorp.jpql.pagination.entities.path.QPerson`). The `build-helper-maven-plugin` registers that directory as a test source root.

When test entity classes change, run `mvn clean test` (not just `mvn test`) to force Q-type regeneration.

### Key test class
- `QueryTemplateImplTest` — covers fetch, sorting (single and multi-field), pagination, count, and `apply` variants

### Key dependencies
- `querydsl-jpa` / `querydsl-core` (`io.github.openfeign.querydsl` fork, v7.x) — compile-scope
- Hibernate 7.x and Jakarta Persistence 3.2 — provided scope
- Jakarta CDI 4 / Inject 2 / Annotation 2 — provided scope
