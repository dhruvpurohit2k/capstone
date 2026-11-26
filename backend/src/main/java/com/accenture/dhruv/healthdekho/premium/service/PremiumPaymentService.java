package com.accenture.dhruv.healthdekho.premium.service;

import com.accenture.dhruv.healthdekho.premium.model.PremiumPayment;
import com.accenture.dhruv.healthdekho.premium.repository.PremiumPaymentRepository;
import com.accenture.dhruv.healthdekho.seed.SeedCsv;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class PremiumPaymentService {

    private final PremiumPaymentRepository repository;

    public PremiumPaymentService(PremiumPaymentRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public PremiumPayment create(PremiumPayment entity) {
        if (entity.getId() != null) {
            throw new IllegalArgumentException("New records must use a generated ID");
        }
        return repository.create(entity);
    }

    @Transactional(readOnly = true)
    public Optional<PremiumPayment> findById(long id) {
        return repository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<PremiumPayment> findAll() {
        return repository.findAll();
    }

    @Transactional
    public void seed() {
        List<Map<String, String>> rows = SeedCsv.read("premium_payment");
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
