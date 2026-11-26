package com.accenture.dhruv.healthdekho.customer.repository;

import com.accenture.dhruv.healthdekho.customer.model.Customer;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class CustomerRepository {

    private final EntityManager entityManager;

    public CustomerRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public Customer create(Customer entity) {
        entityManager.persist(entity);
        return entity;
    }

    public Optional<Customer> findById(long id) {
        return Optional.ofNullable(entityManager.find(Customer.class, id));
    }

    public Optional<Customer> findByEmail(String email) {
        return entityManager.createQuery("select c from Customer c where lower(c.email) = :email", Customer.class)
                .setParameter("email", email)
                .setMaxResults(1)
                .getResultList().stream()
                .findFirst();
    }

    public List<Customer> findAll() {
        return entityManager.createQuery("select e from Customer e order by e.id", Customer.class)
                .getResultList();
    }

    public void delete(Customer entity) {
        entityManager.remove(entity);
    }

    public void flush() {
        entityManager.flush();
    }

    public void lockForSeeding() {
        entityManager.createNativeQuery("lock table customer in share row exclusive mode").executeUpdate();
    }

    public int insertSeed(Map<String, String> row, String passwordHash) {
        return entityManager.createNativeQuery("""
                insert into customer (customer_id, email, password_hash, full_name, date_of_birth, phone, status,
                        created_at, updated_at)
                values (cast(:customer_id as bigint), :email, :password_hash, :full_name, cast(:date_of_birth as
                        date), :phone, :status, current_timestamp, current_timestamp)
                on conflict (customer_id) do nothing
                """)
                .setParameter("customer_id", row.get("customer_id"))
                .setParameter("email", row.get("email"))
                .setParameter("password_hash", passwordHash)
                .setParameter("full_name", row.get("full_name"))
                .setParameter("date_of_birth", row.get("date_of_birth"))
                .setParameter("phone", row.get("phone"))
                .setParameter("status", row.get("status"))
                .executeUpdate();
    }

    public void advanceIdentity() {
        entityManager.createNativeQuery("""
                select setval(pg_get_serial_sequence('customer', 'customer_id'),
                    greatest(nextval(pg_get_serial_sequence('customer', 'customer_id')), max(customer_id)))
                from customer
                """).getSingleResult();
    }
}
