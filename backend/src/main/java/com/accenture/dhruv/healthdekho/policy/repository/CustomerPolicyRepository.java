package com.accenture.dhruv.healthdekho.policy.repository;

import com.accenture.dhruv.healthdekho.policy.model.CustomerPolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface CustomerPolicyRepository extends JpaRepository<CustomerPolicy, Long> {

    @Query("""
            select policy from CustomerPolicy policy
            join fetch policy.product
            where policy.customer.id = :customerId
            order by policy.id
            """)
    List<CustomerPolicy> findByCustomerId(@Param("customerId") long customerId);

    @Query("""
            select count(policy) > 0 from CustomerPolicy policy
            where policy.customer.id = :customerId and policy.product.id = :productId
            """)
    boolean existsForCustomerAndProduct(@Param("customerId") long customerId, @Param("productId") long productId);

    @Query("""
            select policy from CustomerPolicy policy
            join fetch policy.product
            where policy.id = :policyId and policy.customer.id = :customerId
            """)
    Optional<CustomerPolicy> findOwnedPolicy(@Param("customerId") long customerId, @Param("policyId") long policyId);

    @Query(value = """
            with next_policy as (
                select nextval(pg_get_serial_sequence('customer_policy', 'policy_id')) as id
            )
            insert into customer_policy (policy_id, policy_number, customer_id, product_id, start_date,
                maturity_date, premium_frequency, premium_amount, sum_insured, total_premium_payable, status, created_at)
            select id, concat(:numberPrefix, id), :customerId, :productId, :startDate, :maturityDate,
                :frequency, :premiumAmount, :sumInsured, :totalPremium, 'ACTIVE', current_timestamp
            from next_policy
            on conflict (customer_id, product_id) do nothing
            returning policy_id
            """, nativeQuery = true)
    Optional<Long> insertRegisteredPolicy(@Param("numberPrefix") String numberPrefix,
                                          @Param("customerId") long customerId, @Param("productId") long productId,
                                          @Param("startDate") LocalDate startDate, @Param("maturityDate") LocalDate maturityDate,
                                          @Param("frequency") String frequency, @Param("premiumAmount") BigDecimal premiumAmount,
                                          @Param("sumInsured") BigDecimal sumInsured, @Param("totalPremium") BigDecimal totalPremium);

    @Modifying
    @Query(value = "lock table customer_policy in share row exclusive mode", nativeQuery = true)
    void lockForSeeding();

    @Modifying
    @Query(value = """
            insert into customer_policy (policy_id, policy_number, customer_id, product_id, start_date,
                maturity_date, premium_frequency, premium_amount, sum_insured, total_premium_payable,
                status, created_at)
            values (cast(:#{#row['policy_id']} as bigint), :#{#row['policy_number']}, cast(:#{#row['customer_id']} as bigint), cast(:#{#row['product_id']}
                as bigint), cast(:#{#row['start_date']} as date), cast(:#{#row['maturity_date']} as date), :#{#row['premium_frequency']},
                cast(:#{#row['premium_amount']} as numeric), cast(:#{#row['sum_insured']} as numeric),
                cast(:#{#row['total_premium_payable']} as numeric), :#{#row['status']}, current_timestamp)
            on conflict (policy_id) do nothing
            """, nativeQuery = true)
    int insertSeed(@Param("row") Map<String, String> row);

    @Query(value = """
            select setval(pg_get_serial_sequence('customer_policy', 'policy_id'),
            greatest(nextval(pg_get_serial_sequence('customer_policy', 'policy_id')), max(policy_id)))
            from customer_policy
            """, nativeQuery = true)
    Long advanceIdentity();
}
