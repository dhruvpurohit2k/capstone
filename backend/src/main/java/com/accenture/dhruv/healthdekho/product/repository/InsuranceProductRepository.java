package com.accenture.dhruv.healthdekho.product.repository;

import com.accenture.dhruv.healthdekho.product.model.InsuranceProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;

public interface InsuranceProductRepository extends JpaRepository<InsuranceProduct, Long> {

    @Query("select e from InsuranceProduct e order by e.id")
    List<InsuranceProduct> findAll();

    @Query("""
            select product from InsuranceProduct product
            where product.active = true and not exists (
            select 1 from CustomerPolicy policy
            where policy.customer.id = :customerId and policy.product = product
            )
            order by product.id
            """)
    List<InsuranceProduct> findAvailableForCustomer(@Param("customerId") long customerId);

    @Modifying
    @Query(value = "lock table insurance_product in share row exclusive mode", nativeQuery = true)
    void lockForSeeding();

    @Modifying
    @Query(value = """
            insert into insurance_product (product_id, product_code, policy_name, policy_type, term_months,
                premium_frequency, premium_amount, sum_insured, min_entry_age, max_entry_age, active)
            values (cast(:#{#row['product_id']} as bigint), :#{#row['product_code']}, :#{#row['policy_name']}, :#{#row['policy_type']}, cast(:#{#row['term_months']}
                as integer), :#{#row['premium_frequency']}, cast(:#{#row['premium_amount']} as numeric), cast(:#{#row['sum_insured']} as
                numeric), cast(:#{#row['min_entry_age']} as integer), cast(:#{#row['max_entry_age']} as integer), cast(:#{#row['active']} as
                boolean))
            on conflict (product_id) do nothing
            """, nativeQuery = true)
    int insertSeed(@Param("row") Map<String, String> row);

    @Query(value = """
            select setval(pg_get_serial_sequence('insurance_product', 'product_id'),
            greatest(nextval(pg_get_serial_sequence('insurance_product', 'product_id')), max(product_id)))
            from insurance_product
            """, nativeQuery = true)
    Long advanceIdentity();
}
