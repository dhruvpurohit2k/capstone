package com.accenture.dhruv.healthdekho.premium.service;

import com.accenture.dhruv.healthdekho.premium.model.PremiumInstallment;
import com.accenture.dhruv.healthdekho.premium.repository.PremiumInstallmentRepository;
import com.accenture.dhruv.healthdekho.seed.SeedCsv;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class PremiumInstallmentService {

    private final PremiumInstallmentRepository repository;

    public PremiumInstallmentService(PremiumInstallmentRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public PremiumInstallment create(PremiumInstallment entity) {
        if (entity.getId() != null) {
            throw new IllegalArgumentException("New records must use a generated ID");
        }
        return repository.create(entity);
    }

    @Transactional(readOnly = true)
    public Optional<PremiumInstallment> findById(long id) {
        return repository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<PremiumInstallment> findAll() {
        return repository.findAll();
    }

    @Transactional
    public PremiumInstallment update(long id, PremiumInstallment replacement) {
        PremiumInstallment current = requireExisting(id);
        current.setPolicy(replacement.getPolicy());
        current.setInstallmentNumber(replacement.getInstallmentNumber());
        current.setDueDate(replacement.getDueDate());
        current.setGracePeriodEndDate(replacement.getGracePeriodEndDate());
        current.setScheduledAmount(replacement.getScheduledAmount());
        current.setAssessedPenaltyAmount(replacement.getAssessedPenaltyAmount());
        current.setPaidAmount(replacement.getPaidAmount());
        current.setPaymentStatus(replacement.getPaymentStatus());
        repository.flush();
        return current;
    }

    @Transactional
    public void delete(long id) {
        repository.delete(requireExisting(id));
    }

    private PremiumInstallment requireExisting(long id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("PremiumInstallment with ID " + id + " was not found"));
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
