package com.accenture.dhruv.healthdekho.product.service;

import com.accenture.dhruv.healthdekho.product.model.InsuranceProduct;
import com.accenture.dhruv.healthdekho.product.repository.InsuranceProductRepository;
import com.accenture.dhruv.healthdekho.seed.SeedCsv;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class InsuranceProductService {

    private final InsuranceProductRepository repository;

    public InsuranceProductService(InsuranceProductRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public InsuranceProduct create(InsuranceProduct entity) {
        if (entity.getId() != null) {
            throw new IllegalArgumentException("New records must use a generated ID");
        }
        return repository.save(entity);
    }

    @Transactional(readOnly = true)
    public Optional<InsuranceProduct> findById(long id) {
        return repository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<InsuranceProduct> findAll() {
        return repository.findAll();
    }

    @Transactional
    public InsuranceProduct update(long id, InsuranceProduct replacement) {
        InsuranceProduct current = requireExisting(id);
        current.setProductCode(replacement.getProductCode());
        current.setPolicyName(replacement.getPolicyName());
        current.setPolicyType(replacement.getPolicyType());
        current.setTermMonths(replacement.getTermMonths());
        current.setPremiumFrequency(replacement.getPremiumFrequency());
        current.setPremiumAmount(replacement.getPremiumAmount());
        current.setSumInsured(replacement.getSumInsured());
        current.setMinEntryAge(replacement.getMinEntryAge());
        current.setMaxEntryAge(replacement.getMaxEntryAge());
        current.setActive(replacement.isActive());
        repository.flush();
        return current;
    }

    @Transactional
    public void delete(long id) {
        repository.delete(requireExisting(id));
    }

    private InsuranceProduct requireExisting(long id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("InsuranceProduct with ID " + id + " was not found"));
    }

    @Transactional
    public void seed() {
        List<Map<String, String>> rows = SeedCsv.read("insurance_product");
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
