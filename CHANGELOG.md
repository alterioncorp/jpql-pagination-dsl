# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [1.0.1] - 2026-03-03

### Changed
- `QueryTemplateImpl` no longer carries CDI annotations (`@ApplicationScoped`, `@PersistenceContext`).
  It is now a plain class constructed with an `EntityManager`, making it usable in any framework
  (CDI, Spring, plain JPA). See the README for integration examples.

## [1.0.0] - 2026-03-03

### Added
- Initial release.
- `QueryTemplate` / `QueryTemplateImpl` — entry point modelled after `EntityManager`, with `find`,
  `count`, and `apply` methods that accept a `JPQLQueryBuilder` lambda for reusable query
  definitions across paginated, counted, and streaming execution modes.
- `JPQLQueryBuilder<T>` — functional interface that defines a QueryDSL query (select, from, join,
  where) without sort or pagination, allowing the same predicate logic to be reused across all
  three execution modes.
