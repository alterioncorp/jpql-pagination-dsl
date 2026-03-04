# jpql-pagination-dsl

[![codecov](https://codecov.io/gh/alterioncorp/jpql-pagination-dsl/graph/badge.svg)](https://codecov.io/gh/alterioncorp/jpql-pagination-dsl)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)

A small Java library that lets you define a [QueryDSL](https://openfeign.github.io/querydsl/) query once as a lambda and reuse it to fetch a page, get a count, or stream the full result set — without repeating predicates.

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
    <version>1.0.1</version>
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

Define your builder once:

```java
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
        offset, limit, QPerson.person.name.asc());
```

### Multi-field sort

```java
List<Person> results = queryTemplate.find(
        byDepartment,
        QPerson.person.name.asc(), QPerson.person.id.asc());
```

### Streaming the full dataset (batch processing)

`apply` streams results and clears the persistence context after each entity, keeping memory usage flat for large result sets:

```java
queryTemplate.apply(byDepartment, entity -> process(entity));
```

## API summary

| Method | Description |
|--------|-------------|
| `find(builder, sort...)` | Fetch all matching results, with optional sorting |
| `find(builder, offset, limit, sort...)` | Fetch a single page, with optional sorting |
| `count(builder)` | Count matching results |
| `apply(builder, consumer, sort...)` | Stream results, with optional sorting |
| `apply(builder, consumer, offset, limit, sort...)` | Stream a single page, with optional sorting |

## Framework integration

`QueryTemplateImpl` accepts an `EntityManager` via its constructor, so it works with any framework.

### CDI (Quarkus, Jakarta EE)

```java
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@ApplicationScoped
public class QueryTemplateProducer {

    @PersistenceContext
    EntityManager entityManager;

    @Produces
    @ApplicationScoped
    public QueryTemplate queryTemplate() {
        return new QueryTemplateImpl(entityManager);
    }
}
```

Then inject normally:

```java
@Inject
QueryTemplate queryTemplate;
```

### Spring

```java
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QueryTemplateConfig {

    @PersistenceContext
    EntityManager entityManager;

    @Bean
    public QueryTemplate queryTemplate() {
        return new QueryTemplateImpl(entityManager);
    }
}
```

Then inject normally:

```java
@Autowired
QueryTemplate queryTemplate;
```
