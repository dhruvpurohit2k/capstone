package com.accenture.dhruv.healthdekho.premium.repository;

import com.accenture.dhruv.healthdekho.premium.model.PaymentStatus;
import com.accenture.dhruv.healthdekho.premium.model.PremiumInstallment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;

public interface PremiumInstallmentRepository extends JpaRepository<PremiumInstallment, Long> {

    @Query("""
            select installment from PremiumInstallment installment
            join fetch installment.policy policy
            where policy.customer.id = :customerId
              and installment.paymentStatus in :statuses
              and installment.paidAmount < installment.scheduledAmount + installment.assessedPenaltyAmount
            order by installment.dueDate, installment.id
            """)
    List<PremiumInstallment> findOpenForCustomer(@Param("customerId") long customerId,
                                                 @Param("statuses") List<PaymentStatus> statuses);

    @Query("""
            select installment from PremiumInstallment installment
            where installment.policy.id = :policyId
            """)
    List<PremiumInstallment> findByPolicyId(@Param("policyId") long policyId);

    @Modifying
    @Query(value = "lock table premium_installment in share row exclusive mode", nativeQuery = true)
    void lockForSeeding();

    @Modifying
    @Query(value = """
            insert into premium_installment (installment_id, policy_id, installment_number, due_date,
                grace_period_end_date, scheduled_amount, paid_amount, payment_status,
                assessed_penalty_amount)
            values (cast(:#{#row['installment_id']} as bigint), cast(:#{#row['policy_id']} as bigint), cast(:#{#row['installment_number']} as
                integer), cast(:#{#row['due_date']} as date), cast(:#{#row['grace_period_end_date']} as date),
                cast(:#{#row['scheduled_amount']} as numeric), cast(:#{#row['paid_amount']} as numeric), :#{#row['payment_status']},
                cast(:#{#row['assessed_penalty_amount']} as numeric))
            on conflict (installment_id) do nothing
            """, nativeQuery = true)
    int insertSeed(@Param("row") Map<String, String> row);

    @Query(value = """
            select setval(pg_get_serial_sequence('premium_installment', 'installment_id'),
            greatest(nextval(pg_get_serial_sequence('premium_installment', 'installment_id')), max(installment_id)))
            from premium_installment
            """, nativeQuery = true)
    Long advanceIdentity();
}
