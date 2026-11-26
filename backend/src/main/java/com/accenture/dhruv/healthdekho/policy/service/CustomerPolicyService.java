package com.accenture.dhruv.healthdekho.policy.service;

import com.accenture.dhruv.healthdekho.policy.model.CustomerPolicy;
import com.accenture.dhruv.healthdekho.policy.repository.CustomerPolicyRepository;
import com.accenture.dhruv.healthdekho.seed.SeedCsv;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class CustomerPolicyService {

    private final CustomerPolicyRepository repository;

    public CustomerPolicyService(CustomerPolicyRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public CustomerPolicy create(CustomerPolicy entity) {
        if (entity.getId() != null) {
            throw new IllegalArgumentException("New records must use a generated ID");
        }
        return repository.create(entity);
    }

    @Transactional(readOnly = true)
    public Optional<CustomerPolicy> findById(long id) {
        return repository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<CustomerPolicy> findAll() {
        return repository.findAll();
    }

    @Transactional
    public CustomerPolicy update(long id, CustomerPolicy replacement) {
        CustomerPolicy current = requireExisting(id);
        current.setPolicyNumber(replacement.getPolicyNumber());
        current.setCustomer(replacement.getCustomer());
        current.setProduct(replacement.getProduct());
        current.setStartDate(replacement.getStartDate());
        current.setMaturityDate(replacement.getMaturityDate());
        current.setPremiumFrequency(replacement.getPremiumFrequency());
        current.setPremiumAmount(replacement.getPremiumAmount());
        current.setSumInsured(replacement.getSumInsured());
        current.setTotalPremiumPayable(replacement.getTotalPremiumPayable());
        current.setStatus(replacement.getStatus());
        repository.flush();
        return current;
    }

    @Transactional
    public void delete(long id) {
        repository.delete(requireExisting(id));
    }

    private CustomerPolicy requireExisting(long id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("CustomerPolicy with ID " + id + " was not found"));
    }

    @Transactional
    public void seed() {
        List<Map<String, String>> rows = SeedCsv.read("customer_policy");
        repository.lockForSeeding();
        int inserted = 0;
        for (Map<String, String> row : rows) {
            inserted += repository.insertSeed(row);
        }
        if (inserted > 0) {
            repository.advanceIdentity();
        }
    }
}
