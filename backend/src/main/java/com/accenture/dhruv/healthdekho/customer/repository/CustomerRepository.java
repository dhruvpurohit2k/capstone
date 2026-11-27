package com.accenture.dhruv.healthdekho.customer.repository;

import com.accenture.dhruv.healthdekho.customer.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Map;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    @Query(value = """
            select * from customer where lower(email) = :email order by customer_id limit 1
            """, nativeQuery = true)
    Optional<Customer> findByEmail(@Param("email") String email);

    @Modifying
    @Query(value = "lock table customer in share row exclusive mode", nativeQuery = true)
    void lockForSeeding();

    @Modifying
    @Query(value = """
            insert into customer (customer_id, email, password_hash, full_name, date_of_birth, phone, status,
                created_at, updated_at)
            values (cast(:#{#row['customer_id']} as bigint), :#{#row['email']}, :password_hash, :#{#row['full_name']}, cast(:#{#row['date_of_birth']} as
                date), :#{#row['phone']}, :#{#row['status']}, current_timestamp, current_timestamp)
            on conflict (customer_id) do nothing
            """, nativeQuery = true)
    int insertSeed(@Param("row") Map<String, String> row, @Param("password_hash") String passwordHash);

    @Query(value = """
            select setval(pg_get_serial_sequence('customer', 'customer_id'),
            greatest(nextval(pg_get_serial_sequence('customer', 'customer_id')), max(customer_id)))
            from customer
            """, nativeQuery = true)
    Long advanceIdentity();
}
