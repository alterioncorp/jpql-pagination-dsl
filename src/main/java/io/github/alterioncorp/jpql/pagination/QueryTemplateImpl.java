package io.github.alterioncorp.jpql.pagination;

import java.util.List;
import java.util.function.Consumer;

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
	public <T> List<T> find(JPQLQueryBuilder<T> queryBuilder, OrderSpecifier<?>... sort) {
		return createQuery(queryBuilder).orderBy(sort).fetch();
	}

	@Override
	public <T> List<T> find(JPQLQueryBuilder<T> queryBuilder, long offset, long limit, OrderSpecifier<?>... sort) {
		return createQuery(queryBuilder).orderBy(sort).offset(offset).limit(limit).fetch();
	}

	@Override
	public <T> long count(JPQLQueryBuilder<T> queryBuilder) {
		return createQuery(queryBuilder).fetchCount();
	}

	@Override
	public <T> void apply(JPQLQueryBuilder<T> queryBuilder, Consumer<T> consumer, OrderSpecifier<?>... sort) {
		createQuery(queryBuilder).orderBy(sort).stream().forEach(entity -> {
			consumer.accept(entity);
			entityManager.clear();
		});
	}

	@Override
	public <T> void apply(JPQLQueryBuilder<T> queryBuilder, Consumer<T> consumer, long offset, long limit, OrderSpecifier<?>... sort) {
		createQuery(queryBuilder).orderBy(sort).offset(offset).limit(limit).stream().forEach(entity -> {
			consumer.accept(entity);
			entityManager.clear();
		});
	}

	private <T> JPQLQuery<T> createQuery(JPQLQueryBuilder<T> queryBuilder) {
		return queryBuilder.createQuery(new JPAQueryFactory(entityManager));
	}

}
