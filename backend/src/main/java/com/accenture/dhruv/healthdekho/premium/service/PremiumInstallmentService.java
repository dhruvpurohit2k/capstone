package com.accenture.dhruv.healthdekho.premium.service;

import com.accenture.dhruv.healthdekho.premium.repository.PremiumInstallmentRepository;
import com.accenture.dhruv.healthdekho.seed.SeedCsv;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class PremiumInstallmentService {

    private final PremiumInstallmentRepository repository;

    public PremiumInstallmentService(PremiumInstallmentRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void seed() {
        List<Map<String, String>> rows = SeedCsv.read("premium_installment");
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
