package be.valuya.hibernate.param.detached.repro;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class DetachedEntityParameterTest {

    private static EntityManagerFactory entityManagerFactory;

    @BeforeAll
    static void setUpEntityManagerFactory() {
        entityManagerFactory = Persistence.createEntityManagerFactory("repro");
    }

    @AfterAll
    static void tearDownEntityManagerFactory() {
        entityManagerFactory.close();
    }

    @Test
    void reproducesDetachedEntityWithNullVersionDuringAutoFlush() {
        Long customerId = withEntityManager(entityManager -> {
            Trustee trustee = new Trustee();
            entityManager.persist(trustee);

            Customer customer = new Customer();
            customer.setTrustee(trustee);
            entityManager.persist(customer);

            return customer.getId();
        });

        Trustee detachedTrustee = withEntityManager(entityManager -> entityManager.find(Customer.class, customerId).getTrustee());

        assertThatCode(() -> runInTransaction(entityManager -> {
            Customer managedCustomer = entityManager.find(Customer.class, customerId);
            managedCustomer.setName("updated");

            List<Customer> customers = entityManager.createQuery(
                            "select c from Customer c where c.trustee = :trustee",
                            Customer.class
                    )
                    .setParameter("trustee", detachedTrustee)
                    .getResultList();

            assertThat(customers).hasSize(1);
        })).doesNotThrowAnyException();
    }

    private static void runInTransaction(Consumer<EntityManager> work) {
        withEntityManager(entityManager -> {
            work.accept(entityManager);
            return null;
        });
    }

    private static <T> T withEntityManager(Function<EntityManager, T> work) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();
        transaction.begin();
        try {
            T result = work.apply(entityManager);
            transaction.commit();
            return result;
        } finally {
            entityManager.close();
        }
    }
}
