package com.accenture.dhruv.healthdekho.premium.repository;

import com.accenture.dhruv.healthdekho.premium.model.PremiumPayment;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;

public interface PremiumPaymentRepository extends Repository<PremiumPayment, Long> {

    @Query("""
            select payment from PremiumPayment payment
            join fetch payment.installment installment
            join fetch installment.policy policy
            where policy.customer.id = :customerId
            order by payment.paymentDate desc, payment.id desc
            """)
    List<PremiumPayment> findByCustomerId(@Param("customerId") long customerId);

    @Modifying
    @Query(value = "lock table premium_payment in share row exclusive mode", nativeQuery = true)
    void lockForSeeding();

    @Modifying
    @Query(value = """
            insert into premium_payment (payment_id, installment_id, transaction_reference, payment_date,
                amount_paid, penalty_amount, payment_method, created_at)
            values (cast(:#{#row['payment_id']} as bigint), cast(:#{#row['installment_id']} as bigint), :#{#row['transaction_reference']},
                cast(:#{#row['payment_date']} as date), cast(:#{#row['amount_paid']} as numeric), cast(:#{#row['penalty_amount']} as
                numeric), :#{#row['payment_method']}, current_timestamp)
            on conflict (payment_id) do nothing
            """, nativeQuery = true)
    int insertSeed(@Param("row") Map<String, String> row);

    @Query(value = """
            select setval(pg_get_serial_sequence('premium_payment', 'payment_id'),
            greatest(nextval(pg_get_serial_sequence('premium_payment', 'payment_id')), max(payment_id)))
            from premium_payment
            """, nativeQuery = true)
    Long advanceIdentity();
}
