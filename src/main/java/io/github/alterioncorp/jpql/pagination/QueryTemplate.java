package io.github.alterioncorp.jpql.pagination;

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
 * long total = queryTemplate.count(query);
 * List<Person> page = queryTemplate.find(query, offset, limit, QPerson.person.name.asc());
 * }</pre>
 */
public interface QueryTemplate {

	/**
	 * Returns all entities matching the query, ordered by the given specifiers.
	 * If no specifiers are given, the order is determined by the query itself.
	 *
	 * @param <T>          the entity type
	 * @param queryBuilder the query definition
	 * @param sort         zero or more sort specifiers, applied in order
	 * @return matching entities
	 */
	<T> List<T> find(JPQLQueryBuilder<T> queryBuilder, OrderSpecifier<?>... sort);

	/**
	 * Returns one page of entities matching the query, ordered by the given specifiers.
	 * If no specifiers are given, the order is determined by the query itself.
	 *
	 * @param <T>          the entity type
	 * @param queryBuilder the query definition
	 * @param offset       zero-based index of the first result to return
	 * @param limit        maximum number of results to return
	 * @param sort         zero or more sort specifiers, applied in order
	 * @return one page of matching entities
	 */
	<T> List<T> find(JPQLQueryBuilder<T> queryBuilder, long offset, long limit, OrderSpecifier<?>... sort);

	/**
	 * Returns the number of entities matching the query.
	 *
	 * @param <T>          the entity type
	 * @param queryBuilder the query definition
	 * @return the count of matching entities
	 */
	<T> long count(JPQLQueryBuilder<T> queryBuilder);

	/**
	 * Passes each entity matching the query to the given consumer, ordered by the given specifiers.
	 * If no specifiers are given, the order is determined by the query itself.
	 * The persistence context is cleared after each entity to bound memory usage when
	 * processing large result sets.
	 *
	 * @param <T>          the entity type
	 * @param queryBuilder the query definition
	 * @param consumer     called once per matching entity
	 * @param sort         zero or more sort specifiers, applied in order
	 */
	<T> void apply(JPQLQueryBuilder<T> queryBuilder, Consumer<T> consumer, OrderSpecifier<?>... sort);

	/**
	 * Passes a page of entities matching the query to the given consumer, ordered by the given
	 * specifiers. If no specifiers are given, the order is determined by the query itself.
	 * The persistence context is cleared after each entity to bound memory usage when
	 * processing large result sets.
	 *
	 * @param <T>          the entity type
	 * @param queryBuilder the query definition
	 * @param consumer     called once per matching entity
	 * @param offset       zero-based index of the first result to process
	 * @param limit        maximum number of results to process
	 * @param sort         zero or more sort specifiers, applied in order
	 */
	<T> void apply(JPQLQueryBuilder<T> queryBuilder, Consumer<T> consumer, long offset, long limit, OrderSpecifier<?>... sort);

}
