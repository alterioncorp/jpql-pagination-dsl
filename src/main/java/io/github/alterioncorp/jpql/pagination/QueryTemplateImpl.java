package io.github.alterioncorp.jpql.pagination;

import java.util.List;
import java.util.function.Consumer;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import jakarta.persistence.EntityManager;

/**
 * Implementation of {@link QueryTemplate}.
 */
public class QueryTemplateImpl implements QueryTemplate {

	private final EntityManager entityManager;

	/**
	 * Constructor.
	 *
	 * @param entityManager the entity manager to use for query execution
	 */
	public QueryTemplateImpl(EntityManager entityManager) {
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
