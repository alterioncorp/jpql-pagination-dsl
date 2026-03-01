package com.alexlitovsky.jpql.pagination;

import java.util.List;
import java.util.function.Consumer;

import com.querydsl.core.types.OrderSpecifier;

/**
 * Executes a QueryDSL query defined by a {@link JPQLQueryBuilder} lambda in one of three modes:
 * paginated fetch ({@link #find}), count ({@link #count}), or streaming consumer ({@link #apply}).
 *
 * <p>The same builder lambda can be passed to any of these methods, allowing callers to reuse
 * a single query definition — including its {@code where} predicates — across all three modes
 * without duplication.
 *
 * <p>The standard usage pattern for a paginated endpoint is:
 * <pre>{@code
 * JPQLQueryBuilder<Person> query = qf -> qf.select(QPerson.person)
 *         .from(QPerson.person)
 *         .where(QPerson.person.department.eq("Engineering"));
 *
 * long total = queryTemplate.count(Person.class, query);
 * List<Person> page = queryTemplate.find(Person.class, query, QPerson.person.name.asc(), offset, limit);
 * }</pre>
 */
public interface QueryTemplate {

	/**
	 * Returns all entities matching the query, in the order determined by the query itself.
	 *
	 * @param clazz        the entity class
	 * @param queryBuilder the query definition
	 * @return matching entities
	 */
	<T> List<T> find(Class<T> clazz, JPQLQueryBuilder<T> queryBuilder);

	/**
	 * Returns all entities matching the query, ordered by the given specifier.
	 *
	 * @param clazz        the entity class
	 * @param queryBuilder the query definition
	 * @param sort         the sort order
	 * @return matching entities in the specified order
	 */
	<T> List<T> find(Class<T> clazz, JPQLQueryBuilder<T> queryBuilder, OrderSpecifier<?> sort);

	/**
	 * Returns all entities matching the query, ordered by the given specifiers.
	 *
	 * @param clazz        the entity class
	 * @param queryBuilder the query definition
	 * @param sort         the sort orders, applied in array order
	 * @return matching entities in the specified order
	 */
	<T> List<T> find(Class<T> clazz, JPQLQueryBuilder<T> queryBuilder, OrderSpecifier<?>[] sort);

	/**
	 * Returns one page of entities matching the query.
	 *
	 * @param clazz        the entity class
	 * @param queryBuilder the query definition
	 * @param sort         the sort order
	 * @param offset       zero-based index of the first result to return
	 * @param limit        maximum number of results to return
	 * @return one page of matching entities in the specified order
	 */
	<T> List<T> find(Class<T> clazz, JPQLQueryBuilder<T> queryBuilder, OrderSpecifier<?> sort, long offset, long limit);

	/**
	 * Returns one page of entities matching the query.
	 *
	 * @param clazz        the entity class
	 * @param queryBuilder the query definition
	 * @param sort         the sort orders, applied in array order
	 * @param offset       zero-based index of the first result to return
	 * @param limit        maximum number of results to return
	 * @return one page of matching entities in the specified order
	 */
	<T> List<T> find(Class<T> clazz, JPQLQueryBuilder<T> queryBuilder, OrderSpecifier<?>[] sort, long offset, long limit);

	/**
	 * Returns the number of entities matching the query.
	 *
	 * @param clazz        the entity class
	 * @param queryBuilder the query definition
	 * @return the count of matching entities
	 */
	<T> long count(Class<T> clazz, JPQLQueryBuilder<T> queryBuilder);

	/**
	 * Passes each entity matching the query to the given consumer, in the order determined by
	 * the query itself. The persistence context is cleared after each entity to bound memory
	 * usage when processing large result sets.
	 *
	 * @param clazz        the entity class
	 * @param queryBuilder the query definition
	 * @param consumer     called once per matching entity
	 */
	<T> void apply(Class<T> clazz, JPQLQueryBuilder<T> queryBuilder, Consumer<T> consumer);

	/**
	 * Passes each entity matching the query to the given consumer, in the specified order.
	 * The persistence context is cleared after each entity to bound memory usage when
	 * processing large result sets.
	 *
	 * @param clazz        the entity class
	 * @param queryBuilder the query definition
	 * @param sort         the sort order
	 * @param consumer     called once per matching entity
	 */
	<T> void apply(Class<T> clazz, JPQLQueryBuilder<T> queryBuilder, OrderSpecifier<?> sort, Consumer<T> consumer);

	/**
	 * Passes each entity matching the query to the given consumer, in the specified order.
	 * The persistence context is cleared after each entity to bound memory usage when
	 * processing large result sets.
	 *
	 * @param clazz        the entity class
	 * @param queryBuilder the query definition
	 * @param sort         the sort orders, applied in array order
	 * @param consumer     called once per matching entity
	 */
	<T> void apply(Class<T> clazz, JPQLQueryBuilder<T> queryBuilder, OrderSpecifier<?>[] sort, Consumer<T> consumer);

	/**
	 * Passes a page of entities matching the query to the given consumer, in the specified order.
	 * The persistence context is cleared after each entity to bound memory usage when
	 * processing large result sets.
	 *
	 * @param clazz        the entity class
	 * @param queryBuilder the query definition
	 * @param sort         the sort order
	 * @param offset       zero-based index of the first result to process
	 * @param limit        maximum number of results to process
	 * @param consumer     called once per matching entity
	 */
	<T> void apply(Class<T> clazz, JPQLQueryBuilder<T> queryBuilder, OrderSpecifier<?> sort, long offset, long limit, Consumer<T> consumer);

	/**
	 * Passes a page of entities matching the query to the given consumer, in the specified order.
	 * The persistence context is cleared after each entity to bound memory usage when
	 * processing large result sets.
	 *
	 * @param clazz        the entity class
	 * @param queryBuilder the query definition
	 * @param sort         the sort orders, applied in array order
	 * @param offset       zero-based index of the first result to process
	 * @param limit        maximum number of results to process
	 * @param consumer     called once per matching entity
	 */
	<T> void apply(Class<T> clazz, JPQLQueryBuilder<T> queryBuilder, OrderSpecifier<?>[] sort, long offset, long limit, Consumer<T> consumer);

}
