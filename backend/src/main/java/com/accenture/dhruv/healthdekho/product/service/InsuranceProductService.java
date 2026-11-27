package com.accenture.dhruv.healthdekho.product.service;

import com.accenture.dhruv.healthdekho.product.repository.InsuranceProductRepository;
import com.accenture.dhruv.healthdekho.seed.SeedCsv;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class InsuranceProductService {

    private final InsuranceProductRepository repository;

    public InsuranceProductService(InsuranceProductRepository repository) {
        this.repository = repository;
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
