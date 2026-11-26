package com.accenture.dhruv.healthdekho.premium.repository;

import com.accenture.dhruv.healthdekho.premium.model.PremiumPayment;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class PremiumPaymentRepository {

    private final EntityManager entityManager;

    public PremiumPaymentRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public PremiumPayment create(PremiumPayment entity) {
        entityManager.persist(entity);
        return entity;
    }

    public Optional<PremiumPayment> findById(long id) {
        return Optional.ofNullable(entityManager.find(PremiumPayment.class, id));
    }

    public List<PremiumPayment> findAll() {
        return entityManager.createQuery("select e from PremiumPayment e order by e.id", PremiumPayment.class)
                .getResultList();
    }

    public void lockForSeeding() {
        entityManager.createNativeQuery("lock table premium_payment in share row exclusive mode").executeUpdate();
    }

    public int insertSeed(Map<String, String> row) {
        return entityManager.createNativeQuery("""
                insert into premium_payment (payment_id, installment_id, transaction_reference, payment_date,
                        amount_paid, penalty_amount, payment_method, created_at)
                values (cast(:payment_id as bigint), cast(:installment_id as bigint), :transaction_reference,
                        cast(:payment_date as date), cast(:amount_paid as numeric), cast(:penalty_amount as
                        numeric), :payment_method, current_timestamp)
                on conflict (payment_id) do nothing
                """)
                .setParameter("payment_id", row.get("payment_id"))
                .setParameter("installment_id", row.get("installment_id"))
                .setParameter("transaction_reference", row.get("transaction_reference"))
                .setParameter("payment_date", row.get("payment_date"))
                .setParameter("amount_paid", row.get("amount_paid"))
                .setParameter("penalty_amount", row.get("penalty_amount"))
                .setParameter("payment_method", row.get("payment_method"))
                .executeUpdate();
    }

    public void advanceIdentity() {
        entityManager.createNativeQuery("""
                select setval(pg_get_serial_sequence('premium_payment', 'payment_id'),
                    greatest(nextval(pg_get_serial_sequence('premium_payment', 'payment_id')), max(payment_id)))
                from premium_payment
                """).getSingleResult();
    }
}
