package com.alexlitovsky.jpql.pagination;

import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.JPQLQueryFactory;

/**
 * A reusable QueryDSL query definition, expressed as a lambda that builds a {@link JPQLQuery}
 * from a {@link JPQLQueryFactory}.
 *
 * <p>Implementations should define only the structural parts of the query — the select, from,
 * join, and where clauses — and leave sorting and pagination to the caller. This allows the
 * same builder to be passed to {@link QueryTemplate#find}, {@link QueryTemplate#count}, and
 * {@link QueryTemplate#apply} without modification.
 *
 * <pre>{@code
 * JPQLQueryBuilder<Person> query = qf -> qf.select(QPerson.person)
 *         .from(QPerson.person)
 *         .where(QPerson.person.department.eq("Engineering"));
 * }</pre>
 *
 * @param <T> the type of entity returned by the query
 */
@FunctionalInterface
public interface JPQLQueryBuilder<T> {

	/**
	 * Builds and returns a QueryDSL query using the provided factory.
	 *
	 * @param queryFactory the factory used to construct the query
	 * @return the constructed query
	 */
	JPQLQuery<T> createQuery(JPQLQueryFactory queryFactory);
}
