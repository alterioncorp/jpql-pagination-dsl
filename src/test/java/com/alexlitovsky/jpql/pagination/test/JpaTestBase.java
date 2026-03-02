package com.alexlitovsky.jpql.pagination.test;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import com.alexlitovsky.jpql.pagination.entities.Country;
import com.alexlitovsky.jpql.pagination.entities.Organization;
import com.alexlitovsky.jpql.pagination.entities.Person;
import com.alexlitovsky.test.derby.DerbyEmbeddedUtils;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.PersistenceUtil;

public abstract class JpaTestBase {

	private static final String DB_NAME = "unit-test";
	private static final String PERSISTENCE_UNIT_NAME = "unit-test";

	private static final Class<?>[] ENTITIES_TO_DELETE = new Class<?>[] {
		Person.class,
		Organization.class,
		Country.class
	};

	protected static EntityManagerFactory entityManagerFactory;
	protected static PersistenceUtil persistenceUtil;

	@BeforeAll
	public static void beforeClass() {

		DerbyEmbeddedUtils.createDatabase(DB_NAME);

		entityManagerFactory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);

        persistenceUtil = Persistence.getPersistenceUtil();
	}

	@AfterAll
	public static void afterClass() {

		if (entityManagerFactory != null) {
			entityManagerFactory.close();
		}

		DerbyEmbeddedUtils.dropDatabase(DB_NAME);
	}

	protected EntityManager entityManager;

	@BeforeEach
	public void before() {
		entityManager = entityManagerFactory.createEntityManager();
	}

	@AfterEach
	public void after() {
		if (entityManager != null) {
			entityManager.getTransaction().begin();
			for (Class<?> entityClass : ENTITIES_TO_DELETE) {
				entityManager.createQuery("delete from " + entityClass.getSimpleName()).executeUpdate();
			}
			entityManager.getTransaction().commit();
			entityManager.close();
		}
	}
}
