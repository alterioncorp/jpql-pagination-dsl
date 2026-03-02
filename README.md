# jpql-pagination-dsl

A small Java library that lets you define a QueryDSL query once as a lambda and reuse it to fetch a page, get a count, or stream the full result set — without repeating predicates.

## The problem

A single dataset filter often needs to serve three different execution modes:

1. **Count** — return the total number of matching records for a pagination header.
2. **Page** — return one sorted, offset-limited slice of those records for the current page.
3. **Stream** — iterate the entire matching dataset row-by-row to produce a report, clearing the persistence context after each entity to keep memory usage flat.

With plain JPA you must duplicate the predicate logic for each mode. `QueryTemplate` solves this by accepting a single builder lambda and applying sort, offset, limit, and streaming behaviour at execution time.

## Why QueryDSL

The `JPQLQueryBuilder<T>` lambda needs to express a complete query — entity, joins, and predicates — in a way that is type-safe and composable. JPA Criteria API can do this, but its verbosity works against readability at the call site: a simple filter requires `CriteriaBuilder`, `CriteriaQuery`, `Root`, and explicit `Predicate` construction.

QueryDSL provides the same type safety via generated Q-types but with a fluent, readable API that fits naturally into a single lambda expression.

## Requirements

- Java 21+
- Jakarta Persistence 3.2 (provided)
- Jakarta CDI 4 (provided)
- QueryDSL JPA (`io.github.openfeign.querydsl`, v6.x)

## Usage

`JPQLQueryBuilder<T>` is a top-level `@FunctionalInterface` that defines a query — its select, from, join, and where clauses — without any sort or pagination. Sorting and pagination are applied by `QueryTemplate` at execution time, which is what allows the same builder to serve all three modes.

Inject `QueryTemplate` and define your builder once:

```java
@Inject
QueryTemplate queryTemplate;

JPQLQueryBuilder<Person> byDepartment = queryFactory -> {
    QPerson p = QPerson.person;
    return queryFactory.select(p).from(p)
            .where(p.department.eq("Engineering"));
};
```

### Paginated fetch + count

```java
long total = queryTemplate.count(Person.class, byDepartment);

List<Person> page = queryTemplate.find(
        Person.class, byDepartment,
        QPerson.person.name.asc(), offset, limit);
```

### Multi-field sort

```java
List<Person> results = queryTemplate.find(
        Person.class, byDepartment,
        new OrderSpecifier[]{QPerson.person.name.asc(), QPerson.person.id.asc()});
```

### Streaming the full dataset (batch processing)

`apply` streams results and clears the persistence context after each entity, keeping memory usage flat for large result sets:

```java
queryTemplate.apply(Person.class, byDepartment, entity -> process(entity));
```

## API summary

| Method | Description |
|--------|-------------|
| `find(clazz, builder)` | Fetch all matching results |
| `find(clazz, builder, sort)` | Fetch with sorting |
| `find(clazz, builder, sort, offset, limit)` | Fetch a single page |
| `count(clazz, builder)` | Count matching results |
| `apply(clazz, builder, consumer)` | Stream results, clearing the persistence context after each entity |

All `find` and `apply` variants accept either a single `OrderSpecifier<?>` or an `OrderSpecifier<?>[]` for multi-field sorting.

## CDI setup

`QueryTemplateImpl` is an `@ApplicationScoped` bean with `@PersistenceContext(unitName = "default")`. Ensure a persistence unit named `default` is configured in your application.
