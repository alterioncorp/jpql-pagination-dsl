package com.alexlitovsky.jpql.pagination;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

/**
 * CDI-managed implementation of {@link QueryTemplate}.
 *
 * <p>Registered as an {@code @ApplicationScoped} bean and injected with the persistence unit
 * named {@code default} via {@code @PersistenceContext}. Inject via {@link QueryTemplate}:
 *
 * <pre>{@code
 * @Inject
 * QueryTemplate queryTemplate;
 * }</pre>
 */
@ApplicationScoped
public class QueryTemplateImpl implements QueryTemplate {

	@PersistenceContext(unitName = "default")
	EntityManager entityManager;

	/**
	 * No-arg constructor required by the CDI container.
	 */
	public QueryTemplateImpl() {
	}

	/**
	 * Constructor for use in tests, bypassing CDI.
	 *
	 * @param entityManager the entity manager to use for query execution
	 */
	public QueryTemplateImpl(EntityManager entityManager) {
		super();
		this.entityManager = entityManager;
	}

	@Override
	public <T> List<T> find(Class<T> clazz, JPQLQueryBuilder<T> queryBuilder) {
		return this.createQuery(clazz, queryBuilder).fetch();
	}

	@Override
	public <T> List<T> find(Class<T> clazz, JPQLQueryBuilder<T> queryBuilder, OrderSpecifier<?> sort) {
		return this.createQuery(clazz, queryBuilder)
			.orderBy(sort)
			.fetch();
	}

	@Override
	public <T> List<T> find(Class<T> clazz, JPQLQueryBuilder<T> queryBuilder, OrderSpecifier<?>[] sort) {
		return this.createQuery(clazz, queryBuilder)
				.orderBy(sort)
				.fetch();
	}

	@Override
	public <T> List<T> find(Class<T> clazz, JPQLQueryBuilder<T> queryBuilder, OrderSpecifier<?> sort, long offset, long limit) {
		return this.createQuery(clazz, queryBuilder)
				.orderBy(sort)
				.offset(offset)
				.limit(limit)
				.fetch();
	}

	@Override
	public <T> List<T> find(Class<T> clazz, JPQLQueryBuilder<T> queryBuilder, OrderSpecifier<?>[] sort, long offset, long limit) {
		return this.createQuery(clazz, queryBuilder)
				.orderBy(sort)
				.offset(offset)
				.limit(limit)
				.fetch();
	}

	private <T> Stream<T> streamMultipleByQuery(Class<T> clazz, JPQLQueryBuilder<T> queryBuilder) {
		return this.createQuery(clazz, queryBuilder).stream();
	}

	private <T> Stream<T> streamMultipleByQuery(Class<T> clazz, JPQLQueryBuilder<T> queryBuilder,
			OrderSpecifier<?> sort) {
		return this.createQuery(clazz, queryBuilder)
				.orderBy(sort)
				.stream();
	}

	private <T> Stream<T> streamMultipleByQuery(Class<T> clazz, JPQLQueryBuilder<T> queryBuilder,
			OrderSpecifier<?>[] sort) {
		return this.createQuery(clazz, queryBuilder)
				.orderBy(sort)
				.stream();
	}

	private <T> Stream<T> streamMultipleByQuery(Class<T> clazz, JPQLQueryBuilder<T> queryBuilder, OrderSpecifier<?> sort,
			long offset, long limit) {
		return this.createQuery(clazz, queryBuilder)
				.orderBy(sort)
				.offset(offset)
				.limit(limit)
				.stream();
	}

	private <T> Stream<T> streamMultipleByQuery(Class<T> clazz, JPQLQueryBuilder<T> queryBuilder,
			OrderSpecifier<?>[] sort, long offset, long limit) {
		return this.createQuery(clazz, queryBuilder)
				.orderBy(sort)
				.offset(offset)
				.limit(limit)
				.stream();
	}

	@Override
	public <T> long count(Class<T> clazz, JPQLQueryBuilder<T> queryBuilder) {
		return this.createQuery(clazz, queryBuilder).fetchCount();
	}

	private <T> JPQLQuery<T> createQuery(Class<T> clazz, JPQLQueryBuilder<T> queryBuilder) {
		JPAQueryFactory queryFactory = new JPAQueryFactory(entityManager);
		JPQLQuery<T> query = queryBuilder.createQuery(queryFactory);
		return query;
	}

	@Override
	public <T> void apply(Class<T> clazz, JPQLQueryBuilder<T> queryBuilder, Consumer<T> consumer) {
		streamMultipleByQuery(clazz, queryBuilder).forEach(entity -> {
			consumer.accept(entity);
			entityManager.clear();
		});
	}

	@Override
	public <T> void apply(Class<T> clazz, JPQLQueryBuilder<T> queryBuilder, OrderSpecifier<?> sort,
			Consumer<T> consumer) {
		streamMultipleByQuery(clazz, queryBuilder, sort).forEach(entity -> {
			consumer.accept(entity);
			entityManager.clear();
		});
	}

	@Override
	public <T> void apply(Class<T> clazz, JPQLQueryBuilder<T> queryBuilder, OrderSpecifier<?>[] sort,
			Consumer<T> consumer) {
		streamMultipleByQuery(clazz, queryBuilder, sort).forEach(entity -> {
			consumer.accept(entity);
			entityManager.clear();
		});
	}

	@Override
	public <T> void apply(Class<T> clazz, JPQLQueryBuilder<T> queryBuilder, OrderSpecifier<?> sort,
			long offset, long limit, Consumer<T> consumer) {
		streamMultipleByQuery(clazz, queryBuilder, sort, offset, limit).forEach(entity -> {
			consumer.accept(entity);
			entityManager.clear();
		});
	}

	@Override
	public <T> void apply(Class<T> clazz, JPQLQueryBuilder<T> queryBuilder, OrderSpecifier<?>[] sort,
			long offset, long limit, Consumer<T> consumer) {
		streamMultipleByQuery(clazz, queryBuilder, sort, offset, limit).forEach(entity -> {
			consumer.accept(entity);
			entityManager.clear();
		});
	}
}
