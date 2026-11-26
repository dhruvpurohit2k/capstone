package com.accenture.dhruv.healthdekho.premium.repository;

import com.accenture.dhruv.healthdekho.premium.model.PremiumInstallment;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class PremiumInstallmentRepository {

    private final EntityManager entityManager;

    public PremiumInstallmentRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public PremiumInstallment create(PremiumInstallment entity) {
        entityManager.persist(entity);
        return entity;
    }

    public Optional<PremiumInstallment> findById(long id) {
        return Optional.ofNullable(entityManager.find(PremiumInstallment.class, id));
    }

    public List<PremiumInstallment> findAll() {
        return entityManager.createQuery("select e from PremiumInstallment e order by e.id", PremiumInstallment.class)
                .getResultList();
    }

    public void delete(PremiumInstallment entity) {
        entityManager.remove(entity);
    }

    public void flush() {
        entityManager.flush();
    }

    public void lockForSeeding() {
        entityManager.createNativeQuery("lock table premium_installment in share row exclusive mode").executeUpdate();
    }

    public int insertSeed(Map<String, String> row) {
        return entityManager.createNativeQuery("""
                insert into premium_installment (installment_id, policy_id, installment_number, due_date,
                        grace_period_end_date, scheduled_amount, paid_amount, payment_status,
                        assessed_penalty_amount)
                values (cast(:installment_id as bigint), cast(:policy_id as bigint), cast(:installment_number as
                        integer), cast(:due_date as date), cast(:grace_period_end_date as date),
                        cast(:scheduled_amount as numeric), cast(:paid_amount as numeric), :payment_status,
                        cast(:assessed_penalty_amount as numeric))
                on conflict (installment_id) do nothing
                """)
                .setParameter("installment_id", row.get("installment_id"))
                .setParameter("policy_id", row.get("policy_id"))
                .setParameter("installment_number", row.get("installment_number"))
                .setParameter("due_date", row.get("due_date"))
                .setParameter("grace_period_end_date", row.get("grace_period_end_date"))
                .setParameter("scheduled_amount", row.get("scheduled_amount"))
                .setParameter("paid_amount", row.get("paid_amount"))
                .setParameter("payment_status", row.get("payment_status"))
                .setParameter("assessed_penalty_amount", row.get("assessed_penalty_amount"))
                .executeUpdate();
    }

    public void advanceIdentity() {
        entityManager.createNativeQuery("""
                select setval(pg_get_serial_sequence('premium_installment', 'installment_id'),
                    greatest(nextval(pg_get_serial_sequence('premium_installment', 'installment_id')), max(installment_id)))
                from premium_installment
                """).getSingleResult();
    }
}
