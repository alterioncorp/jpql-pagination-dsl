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

## Integration

### Maven dependency

```xml
<dependency>
    <groupId>io.github.alterioncorp</groupId>
    <artifactId>jpql-pagination-dsl</artifactId>
    <version>1.0.0</version>
</dependency>
```

QueryDSL Q-types must be generated for your entities. Add the `maven-compiler-plugin` execution with `querydsl-apt` as an annotation processor:

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <executions>
        <execution>
            <id>querydsl-generate</id>
            <configuration>
                <annotationProcessorPaths>
                    <path>
                        <groupId>io.github.openfeign.querydsl</groupId>
                        <artifactId>querydsl-apt</artifactId>
                        <version>7.1</version>
                        <classifier>jakarta</classifier>
                    </path>
                </annotationProcessorPaths>
                <compilerArgs>
                    <arg>-Aquerydsl.packageSuffix=.path</arg>
                    <arg>-Aquerydsl.entityAccessors=true</arg>
                </compilerArgs>
                <generatedSourcesDirectory>${project.build.directory}/generated-sources/java</generatedSourcesDirectory>
            </configuration>
        </execution>
    </executions>
</plugin>
```

With `querydsl.packageSuffix=.path`, Q-types are generated in a `.path` sub-package of the entity's package. For example, an entity `com.example.entities.Person` gets a Q-type at `com.example.entities.path.QPerson`.

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
long total = queryTemplate.count(byDepartment);

List<Person> page = queryTemplate.find(
        byDepartment,
        QPerson.person.name.asc(), offset, limit);
```

### Multi-field sort

```java
List<Person> results = queryTemplate.find(
        byDepartment,
        new OrderSpecifier[]{QPerson.person.name.asc(), QPerson.person.id.asc()});
```

### Streaming the full dataset (batch processing)

`apply` streams results and clears the persistence context after each entity, keeping memory usage flat for large result sets:

```java
queryTemplate.apply(byDepartment, entity -> process(entity));
```

## API summary

| Method | Description |
|--------|-------------|
| `find(builder)` | Fetch all matching results |
| `find(builder, sort)` | Fetch with sorting |
| `find(builder, sort, offset, limit)` | Fetch a single page |
| `count(builder)` | Count matching results |
| `apply(builder, consumer)` | Stream results, clearing the persistence context after each entity |
| `apply(builder, sort, consumer)` | Stream with sorting |
| `apply(builder, sort, offset, limit, consumer)` | Stream a single page |

All `find` and `apply` variants accept either a single `OrderSpecifier<?>` or an `OrderSpecifier<?>[]` for multi-field sorting.

## CDI setup

`QueryTemplateImpl` is an `@ApplicationScoped` bean with `@PersistenceContext(unitName = "default")`. Ensure a persistence unit named `default` is configured in your application.
