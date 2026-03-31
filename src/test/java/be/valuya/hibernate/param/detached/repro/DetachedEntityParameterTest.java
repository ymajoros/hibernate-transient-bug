package be.valuya.hibernate.param.detached.repro;

import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
class DetachedEntityParameterTest {

    @Inject
    EntityManager entityManager;

    @Test
    void reproducesDetachedEntityWithNullVersionDuringAutoFlush() {
        Long customerId = QuarkusTransaction.requiringNew().call(() -> {
            Trustee trustee = new Trustee();
            entityManager.persist(trustee);

            Customer customer = new Customer();
            customer.setTrustee(trustee);
            entityManager.persist(customer);

            return customer.getId();
        });

        Trustee detachedTrustee = QuarkusTransaction.requiringNew().call(() -> entityManager.find(Customer.class, customerId).getTrustee());

        QuarkusTransaction.requiringNew().run(() -> {
            entityManager.find(Customer.class, customerId);

            List<Customer> customers = entityManager.createQuery(
                            "select c from Customer c where c.trustee = :trustee",
                            Customer.class
                    )
                    .setParameter("trustee", detachedTrustee)
                    .getResultList();

            assertThat(customers).hasSize(1);
        });
    }
}
