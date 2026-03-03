package io.github.alterioncorp.jpql.pagination;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.github.alterioncorp.jpql.pagination.entities.Person;
import io.github.alterioncorp.jpql.pagination.entities.path.QPerson;
import io.github.alterioncorp.jpql.pagination.test.JpaTestBase;
import com.querydsl.core.types.OrderSpecifier;

public class QueryTemplateImplTest extends JpaTestBase {

	private QueryTemplateImpl queryTemplate;

	@BeforeEach
	@Override
	public void before() {
		super.before();
		queryTemplate = new QueryTemplateImpl(entityManager);
	}

	@Test
	public void testFind() {

		Person person1 = new Person("A");
		Person person2 = new Person("B");
		Person person3 = new Person("C");

		entityManagerFactory.runInTransaction(entityManager -> {
			entityManager.persist(person1);
			entityManager.persist(person2);
			entityManager.persist(person3);
		});

		List<Person> results = queryTemplate.find(Person.class, (queryFactory) -> {
			QPerson person = QPerson.person;
			return queryFactory
					.select(person)
					.from(person)
					.where(person.name.in(Arrays.asList(person2.getName(), person3.getName())))
					.orderBy(person.name.asc());
		});

		Assertions.assertNotNull(results);
		Assertions.assertEquals(2, results.size());
		Assertions.assertEquals(person2.getId(), results.get(0).getId());
		Assertions.assertEquals(person3.getId(), results.get(1).getId());
	}

	@Test
	public void testFind_Sorting() {

		Person person1 = new Person("C");
		Person person2 = new Person("A");
		Person person3 = new Person("B");

		entityManagerFactory.runInTransaction(entityManager -> {
			entityManager.persist(person1);
			entityManager.persist(person2);
			entityManager.persist(person3);
		});

		List<Person> results = queryTemplate.find(Person.class, (queryFactory) -> {
			QPerson person = QPerson.person;
			return queryFactory
					.select(person)
					.from(person)
					.where(person.name.in(Arrays.asList(person2.getName(), person3.getName())));
			},
			QPerson.person.name.asc());

		Assertions.assertNotNull(results);
		Assertions.assertEquals(2, results.size());
		Assertions.assertEquals(person2.getId(), results.get(0).getId());
		Assertions.assertEquals(person3.getId(), results.get(1).getId());
	}

	@Test
	public void testFind_SortingBySeveralField() {

		Person person1 = new Person("B");
		Person person2 = new Person("A");
		Person person3 = new Person("A");
		Person person4 = new Person("B");
		Person person5 = new Person("B");
		Person person6 = new Person("A");

		entityManagerFactory.runInTransaction(entityManager -> {
			entityManager.persist(person1);
			entityManager.persist(person2);
			entityManager.persist(person3);
			entityManager.persist(person4);
			entityManager.persist(person5);
			entityManager.persist(person6);
		});

		List<Person> results = queryTemplate.find(Person.class, (queryFactory) -> {
				QPerson person = QPerson.person;
				return queryFactory
						.select(person)
						.from(person)
						.where(person.name.in(Arrays.asList(person1.getName(), person2.getName())));
			},
			new OrderSpecifier[]{QPerson.person.name.asc(), QPerson.person.id.asc()});

		Assertions.assertNotNull(results);
		Assertions.assertEquals(6, results.size());
		Assertions.assertEquals(person2.getId(), results.get(0).getId());
		Assertions.assertEquals(person3.getId(), results.get(1).getId());
		Assertions.assertEquals(person6.getId(), results.get(2).getId());
		Assertions.assertEquals(person1.getId(), results.get(3).getId());
		Assertions.assertEquals(person4.getId(), results.get(4).getId());
		Assertions.assertEquals(person5.getId(), results.get(5).getId());
	}

	@Test
	public void testFind_Pagination() {

		Person person1 = new Person("C");
		Person person2 = new Person("A");
		Person person3 = new Person("B");

		entityManagerFactory.runInTransaction(entityManager -> {
			entityManager.persist(person1);
			entityManager.persist(person2);
			entityManager.persist(person3);
		});

		List<Person> results = queryTemplate.find(Person.class, (queryFactory) -> {
			QPerson person = QPerson.person;
			return queryFactory
					.select(person)
					.from(person)
					.where(person.name.in(Arrays.asList(person2.getName(), person3.getName())));
			},
			QPerson.person.name.asc(), 1, 1);

		Assertions.assertNotNull(results);
		Assertions.assertEquals(1, results.size());
		Assertions.assertEquals(person3.getId(), results.get(0).getId());
	}

	@Test
	public void testFind_PaginationWithSorting() {

		Person person1 = new Person("B");
		Person person2 = new Person("A");
		Person person3 = new Person("A");
		Person person4 = new Person("B");
		Person person5 = new Person("B");
		Person person6 = new Person("A");

		entityManagerFactory.runInTransaction(entityManager -> {
			entityManager.persist(person1);
			entityManager.persist(person2);
			entityManager.persist(person3);
			entityManager.persist(person4);
			entityManager.persist(person5);
			entityManager.persist(person6);
		});

		List<Person> results = queryTemplate.find(Person.class, (queryFactory) -> {
				QPerson person = QPerson.person;
				return queryFactory
						.select(person)
						.from(person)
						.where(person.name.in(Arrays.asList(person1.getName(), person2.getName())));
			},
			new OrderSpecifier[]{QPerson.person.name.asc(), QPerson.person.id.asc()}, 0, 3);

		Assertions.assertNotNull(results);
		Assertions.assertEquals(3, results.size());
		Assertions.assertEquals(person2.getId(), results.get(0).getId());
		Assertions.assertEquals(person3.getId(), results.get(1).getId());
		Assertions.assertEquals(person6.getId(), results.get(2).getId());

		results = queryTemplate.find(Person.class, (queryFactory) -> {
				QPerson person = QPerson.person;
				return queryFactory
						.select(person)
						.from(person)
						.where(person.name.in(Arrays.asList(person1.getName(), person2.getName())));
			},
			new OrderSpecifier[]{QPerson.person.name.asc(), QPerson.person.id.asc()}, 2, 3);

		Assertions.assertNotNull(results);
		Assertions.assertEquals(3, results.size());
		Assertions.assertEquals(person6.getId(), results.get(0).getId());
		Assertions.assertEquals(person1.getId(), results.get(1).getId());
		Assertions.assertEquals(person4.getId(), results.get(2).getId());
	}

	@Test
	public void testCount() {

		Person person1 = new Person("C");
		Person person2 = new Person("A");
		Person person3 = new Person("B");

		entityManagerFactory.runInTransaction(entityManager -> {
			entityManager.persist(person1);
			entityManager.persist(person2);
			entityManager.persist(person3);
		});

		long count = queryTemplate.count(Person.class, (queryFactory) -> {
			QPerson person = QPerson.person;
			return queryFactory
					.select(person)
					.from(person)
					.where(person.name.in(Arrays.asList(person2.getName(), person3.getName())));
			});

		Assertions.assertEquals(2, count);
	}

	@Test
	public void testApply() {

		Person person1 = new Person("A");
		Person person2 = new Person("B");
		Person person3 = new Person("C");

		entityManagerFactory.runInTransaction(entityManager -> {
			entityManager.persist(person1);
			entityManager.persist(person2);
			entityManager.persist(person3);
		});

		ArrayList<Person> results = new ArrayList<>();

		queryTemplate.apply(
				Person.class,
				queryFactory -> {
					QPerson person = QPerson.person;
					return queryFactory
							.select(person)
							.from(person)
							.where(person.name.in(Arrays.asList(person2.getName(), person3.getName())))
							.orderBy(person.name.asc());
				},
				entity -> results.add(entity)
		);

		Assertions.assertNotNull(results);
		Assertions.assertEquals(2, results.size());
		Assertions.assertEquals(person2.getId(), results.get(0).getId());
		Assertions.assertEquals(person3.getId(), results.get(1).getId());
	}

	@Test
	public void testApply_Sorting() {

		Person person1 = new Person("C");
		Person person2 = new Person("A");
		Person person3 = new Person("B");

		entityManagerFactory.runInTransaction(entityManager -> {
			entityManager.persist(person1);
			entityManager.persist(person2);
			entityManager.persist(person3);
		});

		ArrayList<Person> results = new ArrayList<>();

		queryTemplate.apply(
				Person.class,
				queryFactory -> {
					QPerson person = QPerson.person;
					return queryFactory
							.select(person)
							.from(person)
							.where(person.name.in(Arrays.asList(person2.getName(), person3.getName())));
				},
				QPerson.person.name.asc(),
				entity -> results.add(entity)
		);

		Assertions.assertNotNull(results);
		Assertions.assertEquals(2, results.size());
		Assertions.assertEquals(person2.getId(), results.get(0).getId());
		Assertions.assertEquals(person3.getId(), results.get(1).getId());
	}

	@Test
	public void testApply_SortingBySeveralField() {

		Person person1 = new Person("B");
		Person person2 = new Person("A");
		Person person3 = new Person("A");
		Person person4 = new Person("B");
		Person person5 = new Person("B");
		Person person6 = new Person("A");

		entityManagerFactory.runInTransaction(entityManager -> {
			entityManager.persist(person1);
			entityManager.persist(person2);
			entityManager.persist(person3);
			entityManager.persist(person4);
			entityManager.persist(person5);
			entityManager.persist(person6);
		});

		ArrayList<Person> results = new ArrayList<>();

		queryTemplate.apply(
				Person.class,
				queryFactory -> {
					QPerson person = QPerson.person;
					return queryFactory
							.select(person)
							.from(person)
							.where(person.name.in(Arrays.asList(person1.getName(), person2.getName())));
				},
				new OrderSpecifier[]{QPerson.person.name.asc(), QPerson.person.id.asc()},
				entity -> results.add(entity)
		);

		Assertions.assertNotNull(results);
		Assertions.assertEquals(6, results.size());
		Assertions.assertEquals(person2.getId(), results.get(0).getId());
		Assertions.assertEquals(person3.getId(), results.get(1).getId());
		Assertions.assertEquals(person6.getId(), results.get(2).getId());
		Assertions.assertEquals(person1.getId(), results.get(3).getId());
		Assertions.assertEquals(person4.getId(), results.get(4).getId());
		Assertions.assertEquals(person5.getId(), results.get(5).getId());
	}

	@Test
	public void testApply_Pagination() {

		Person person1 = new Person("C");
		Person person2 = new Person("A");
		Person person3 = new Person("B");

		entityManagerFactory.runInTransaction(entityManager -> {
			entityManager.persist(person1);
			entityManager.persist(person2);
			entityManager.persist(person3);
		});

		ArrayList<Person> results = new ArrayList<>();

		queryTemplate.apply(
				Person.class,
				queryFactory -> {
					QPerson person = QPerson.person;
					return queryFactory
							.select(person)
							.from(person)
							.where(person.name.in(Arrays.asList(person2.getName(), person3.getName())));
				},
				QPerson.person.name.asc(), 1, 1,
				entity -> results.add(entity)
		);

		Assertions.assertNotNull(results);
		Assertions.assertEquals(1, results.size());
		Assertions.assertEquals(person3.getId(), results.get(0).getId());
	}

	@Test
	public void testApply_PaginationWithSorting() {

		Person person1 = new Person("B");
		Person person2 = new Person("A");
		Person person3 = new Person("A");
		Person person4 = new Person("B");
		Person person5 = new Person("B");
		Person person6 = new Person("A");

		entityManagerFactory.runInTransaction(entityManager -> {
			entityManager.persist(person1);
			entityManager.persist(person2);
			entityManager.persist(person3);
			entityManager.persist(person4);
			entityManager.persist(person5);
			entityManager.persist(person6);
		});

		ArrayList<Person> results = new ArrayList<>();

		queryTemplate.apply(
				Person.class,
				queryFactory -> {
					QPerson person = QPerson.person;
					return queryFactory
							.select(person)
							.from(person)
							.where(person.name.in(Arrays.asList(person1.getName(), person2.getName())));
				},
				new OrderSpecifier[]{QPerson.person.name.asc(), QPerson.person.id.asc()}, 0, 3,
				entity -> results.add(entity)
		);

		Assertions.assertNotNull(results);
		Assertions.assertEquals(3, results.size());
		Assertions.assertEquals(person2.getId(), results.get(0).getId());
		Assertions.assertEquals(person3.getId(), results.get(1).getId());
		Assertions.assertEquals(person6.getId(), results.get(2).getId());

		results.clear();

		queryTemplate.apply(
				Person.class,
				queryFactory -> {
					QPerson person = QPerson.person;
					return queryFactory
							.select(person)
							.from(person)
							.where(person.name.in(Arrays.asList(person1.getName(), person2.getName())));
				},
				new OrderSpecifier[]{QPerson.person.name.asc(), QPerson.person.id.asc()}, 2, 3,
				entity -> results.add(entity)
		);

		Assertions.assertNotNull(results);
		Assertions.assertEquals(3, results.size());
		Assertions.assertEquals(person6.getId(), results.get(0).getId());
		Assertions.assertEquals(person1.getId(), results.get(1).getId());
		Assertions.assertEquals(person4.getId(), results.get(2).getId());
	}
}
