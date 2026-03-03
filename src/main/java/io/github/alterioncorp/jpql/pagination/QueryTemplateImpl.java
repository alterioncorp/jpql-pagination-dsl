package io.github.alterioncorp.jpql.pagination;

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
	public <T> List<T> find(JPQLQueryBuilder<T> queryBuilder) {
		return this.createQuery(queryBuilder).fetch();
	}

	@Override
	public <T> List<T> find(JPQLQueryBuilder<T> queryBuilder, OrderSpecifier<?> sort) {
		return this.createQuery(queryBuilder)
			.orderBy(sort)
			.fetch();
	}

	@Override
	public <T> List<T> find(JPQLQueryBuilder<T> queryBuilder, OrderSpecifier<?>[] sort) {
		return this.createQuery(queryBuilder)
				.orderBy(sort)
				.fetch();
	}

	@Override
	public <T> List<T> find(JPQLQueryBuilder<T> queryBuilder, OrderSpecifier<?> sort, long offset, long limit) {
		return this.createQuery(queryBuilder)
				.orderBy(sort)
				.offset(offset)
				.limit(limit)
				.fetch();
	}

	@Override
	public <T> List<T> find(JPQLQueryBuilder<T> queryBuilder, OrderSpecifier<?>[] sort, long offset, long limit) {
		return this.createQuery(queryBuilder)
				.orderBy(sort)
				.offset(offset)
				.limit(limit)
				.fetch();
	}

	private <T> Stream<T> streamMultipleByQuery(JPQLQueryBuilder<T> queryBuilder) {
		return this.createQuery(queryBuilder).stream();
	}

	private <T> Stream<T> streamMultipleByQuery(JPQLQueryBuilder<T> queryBuilder, OrderSpecifier<?> sort) {
		return this.createQuery(queryBuilder)
				.orderBy(sort)
				.stream();
	}

	private <T> Stream<T> streamMultipleByQuery(JPQLQueryBuilder<T> queryBuilder, OrderSpecifier<?>[] sort) {
		return this.createQuery(queryBuilder)
				.orderBy(sort)
				.stream();
	}

	private <T> Stream<T> streamMultipleByQuery(JPQLQueryBuilder<T> queryBuilder, OrderSpecifier<?> sort,
			long offset, long limit) {
		return this.createQuery(queryBuilder)
				.orderBy(sort)
				.offset(offset)
				.limit(limit)
				.stream();
	}

	private <T> Stream<T> streamMultipleByQuery(JPQLQueryBuilder<T> queryBuilder,
			OrderSpecifier<?>[] sort, long offset, long limit) {
		return this.createQuery(queryBuilder)
				.orderBy(sort)
				.offset(offset)
				.limit(limit)
				.stream();
	}

	@Override
	public <T> long count(JPQLQueryBuilder<T> queryBuilder) {
		return this.createQuery(queryBuilder).fetchCount();
	}

	private <T> JPQLQuery<T> createQuery(JPQLQueryBuilder<T> queryBuilder) {
		JPAQueryFactory queryFactory = new JPAQueryFactory(entityManager);
		return queryBuilder.createQuery(queryFactory);
	}

	@Override
	public <T> void apply(JPQLQueryBuilder<T> queryBuilder, Consumer<T> consumer) {
		streamMultipleByQuery(queryBuilder).forEach(entity -> {
			consumer.accept(entity);
			entityManager.clear();
		});
	}

	@Override
	public <T> void apply(JPQLQueryBuilder<T> queryBuilder, OrderSpecifier<?> sort, Consumer<T> consumer) {
		streamMultipleByQuery(queryBuilder, sort).forEach(entity -> {
			consumer.accept(entity);
			entityManager.clear();
		});
	}

	@Override
	public <T> void apply(JPQLQueryBuilder<T> queryBuilder, OrderSpecifier<?>[] sort, Consumer<T> consumer) {
		streamMultipleByQuery(queryBuilder, sort).forEach(entity -> {
			consumer.accept(entity);
			entityManager.clear();
		});
	}

	@Override
	public <T> void apply(JPQLQueryBuilder<T> queryBuilder, OrderSpecifier<?> sort,
			long offset, long limit, Consumer<T> consumer) {
		streamMultipleByQuery(queryBuilder, sort, offset, limit).forEach(entity -> {
			consumer.accept(entity);
			entityManager.clear();
		});
	}

	@Override
	public <T> void apply(JPQLQueryBuilder<T> queryBuilder, OrderSpecifier<?>[] sort,
			long offset, long limit, Consumer<T> consumer) {
		streamMultipleByQuery(queryBuilder, sort, offset, limit).forEach(entity -> {
			consumer.accept(entity);
			entityManager.clear();
		});
	}
}
