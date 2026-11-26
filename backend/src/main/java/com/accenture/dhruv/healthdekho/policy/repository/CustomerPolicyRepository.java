package com.accenture.dhruv.healthdekho.policy.repository;

import com.accenture.dhruv.healthdekho.policy.model.CustomerPolicy;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class CustomerPolicyRepository {

    private final EntityManager entityManager;

    public CustomerPolicyRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public CustomerPolicy create(CustomerPolicy entity) {
        entityManager.persist(entity);
        return entity;
    }

    public Optional<CustomerPolicy> findById(long id) {
        return Optional.ofNullable(entityManager.find(CustomerPolicy.class, id));
    }

    public List<CustomerPolicy> findAll() {
        return entityManager.createQuery("select e from CustomerPolicy e order by e.id", CustomerPolicy.class)
                .getResultList();
    }

    public void delete(CustomerPolicy entity) {
        entityManager.remove(entity);
    }

    public void flush() {
        entityManager.flush();
    }

    public void lockForSeeding() {
        entityManager.createNativeQuery("lock table customer_policy in share row exclusive mode").executeUpdate();
    }

    public int insertSeed(Map<String, String> row) {
        return entityManager.createNativeQuery("""
                insert into customer_policy (policy_id, policy_number, customer_id, product_id, start_date,
                        maturity_date, premium_frequency, premium_amount, sum_insured, total_premium_payable,
                        status, created_at)
                values (cast(:policy_id as bigint), :policy_number, cast(:customer_id as bigint), cast(:product_id
                        as bigint), cast(:start_date as date), cast(:maturity_date as date), :premium_frequency,
                        cast(:premium_amount as numeric), cast(:sum_insured as numeric),
                        cast(:total_premium_payable as numeric), :status, current_timestamp)
                on conflict (policy_id) do nothing
                """)
                .setParameter("policy_id", row.get("policy_id"))
                .setParameter("policy_number", row.get("policy_number"))
                .setParameter("customer_id", row.get("customer_id"))
                .setParameter("product_id", row.get("product_id"))
                .setParameter("start_date", row.get("start_date"))
                .setParameter("maturity_date", row.get("maturity_date"))
                .setParameter("premium_frequency", row.get("premium_frequency"))
                .setParameter("premium_amount", row.get("premium_amount"))
                .setParameter("sum_insured", row.get("sum_insured"))
                .setParameter("total_premium_payable", row.get("total_premium_payable"))
                .setParameter("status", row.get("status"))
                .executeUpdate();
    }

    public void advanceIdentity() {
        entityManager.createNativeQuery("""
                select setval(pg_get_serial_sequence('customer_policy', 'policy_id'),
                    greatest(nextval(pg_get_serial_sequence('customer_policy', 'policy_id')), max(policy_id)))
                from customer_policy
                """).getSingleResult();
    }
}
