package com.accenture.dhruv.healthdekho.customer.service;

import com.accenture.dhruv.healthdekho.customer.model.Customer;
import com.accenture.dhruv.healthdekho.customer.repository.CustomerRepository;
import com.accenture.dhruv.healthdekho.exception.DuplicateEntityException;
import com.accenture.dhruv.healthdekho.seed.SeedCsv;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Service
public class CustomerService {

    private final CustomerRepository repository;
    private final PasswordEncoder passwordEncoder;

    public CustomerService(CustomerRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public Customer create(Customer entity) {
        if (entity.getId() != null) {
            throw new IllegalArgumentException("New records must use a generated ID");
        }
        String email = entity.getEmail().toLowerCase(Locale.ROOT);
        if (repository.findByEmail(email).isPresent()) {
            throw duplicateEmail();
        }
        entity.setEmail(email);
        try {
            Customer customer = repository.create(entity);
            repository.flush();
            return customer;
        } catch (DataIntegrityViolationException error) {
            if (error.getMostSpecificCause() instanceof SQLException sql && "23505".equals(sql.getSQLState())) {
                throw duplicateEmail();
            }
            throw error;
        }
    }

    @Transactional(readOnly = true)
    public Optional<Customer> findById(long id) {
        return repository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Customer> findByEmail(String email) {
        return repository.findByEmail(email.toLowerCase(Locale.ROOT));
    }

    @Transactional(readOnly = true)
    public List<Customer> findAll() {
        return repository.findAll();
    }

    @Transactional
    public Customer update(long id, Customer replacement) {
        Customer current = requireExisting(id);
        current.setEmail(replacement.getEmail());
        current.setPasswordHash(replacement.getPasswordHash());
        current.setFullName(replacement.getFullName());
        current.setDateOfBirth(replacement.getDateOfBirth());
        current.setPhone(replacement.getPhone());
        current.setStatus(replacement.getStatus());
        repository.flush();
        return current;
    }

    @Transactional
    public void delete(long id) {
        repository.delete(requireExisting(id));
    }

    private DuplicateEntityException duplicateEmail() {
        return new DuplicateEntityException("email", "A customer with this email already exists");
    }

    private Customer requireExisting(long id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Customer with ID " + id + " was not found"));
    }

    @Transactional
    public void seed() {
        List<Map<String, String>> rows = SeedCsv.read("customer");
        repository.lockForSeeding();
        int inserted = 0;
        for (Map<String, String> row : rows) {
            if (repository.findById(Long.parseLong(row.get("customer_id"))).isPresent()) {
                continue;
            }
            inserted += repository.insertSeed(row, passwordEncoder.encode(row.get("password")));
        }
        if (inserted > 0) {
            repository.advanceIdentity();
        }
    }
}
