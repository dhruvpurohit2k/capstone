package com.accenture.dhruv.healthdekho.product.repository;

import com.accenture.dhruv.healthdekho.product.model.InsuranceProduct;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class InsuranceProductRepository {

    private final EntityManager entityManager;

    public InsuranceProductRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public InsuranceProduct create(InsuranceProduct entity) {
        entityManager.persist(entity);
        return entity;
    }

    public Optional<InsuranceProduct> findById(long id) {
        return Optional.ofNullable(entityManager.find(InsuranceProduct.class, id));
    }

    public List<InsuranceProduct> findAll() {
        return entityManager.createQuery("select e from InsuranceProduct e order by e.id", InsuranceProduct.class)
                .getResultList();
    }

    public void delete(InsuranceProduct entity) {
        entityManager.remove(entity);
    }

    public void flush() {
        entityManager.flush();
    }

    public void lockForSeeding() {
        entityManager.createNativeQuery("lock table insurance_product in share row exclusive mode").executeUpdate();
    }

    public int insertSeed(Map<String, String> row) {
        return entityManager.createNativeQuery("""
                insert into insurance_product (product_id, product_code, policy_name, policy_type, term_months,
                        premium_frequency, premium_amount, sum_insured, min_entry_age, max_entry_age, active)
                values (cast(:product_id as bigint), :product_code, :policy_name, :policy_type, cast(:term_months
                        as integer), :premium_frequency, cast(:premium_amount as numeric), cast(:sum_insured as
                        numeric), cast(:min_entry_age as integer), cast(:max_entry_age as integer), cast(:active as
                        boolean))
                on conflict (product_id) do nothing
                """)
                .setParameter("product_id", row.get("product_id"))
                .setParameter("product_code", row.get("product_code"))
                .setParameter("policy_name", row.get("policy_name"))
                .setParameter("policy_type", row.get("policy_type"))
                .setParameter("term_months", row.get("term_months"))
                .setParameter("premium_frequency", row.get("premium_frequency"))
                .setParameter("premium_amount", row.get("premium_amount"))
                .setParameter("sum_insured", row.get("sum_insured"))
                .setParameter("min_entry_age", row.get("min_entry_age"))
                .setParameter("max_entry_age", row.get("max_entry_age"))
                .setParameter("active", row.get("active"))
                .executeUpdate();
    }

    public void advanceIdentity() {
        entityManager.createNativeQuery("""
                select setval(pg_get_serial_sequence('insurance_product', 'product_id'),
                    greatest(nextval(pg_get_serial_sequence('insurance_product', 'product_id')), max(product_id)))
                from insurance_product
                """).getSingleResult();
    }
}
