package com.accenture.dhruv.healthdekho.recommendation.service;

import com.accenture.dhruv.healthdekho.recommendation.model.AiRecommendationRequest;
import com.accenture.dhruv.healthdekho.recommendation.repository.AiRecommendationRequestRepository;
import com.accenture.dhruv.healthdekho.seed.SeedCsv;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class AiRecommendationRequestService {

    private final AiRecommendationRequestRepository repository;

    public AiRecommendationRequestService(AiRecommendationRequestRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public AiRecommendationRequest create(AiRecommendationRequest entity) {
        if (entity.getId() != null) {
            throw new IllegalArgumentException("New records must use a generated ID");
        }
        return repository.save(entity);
    }

    @Transactional(readOnly = true)
    public Optional<AiRecommendationRequest> findById(long id) {
        return repository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<AiRecommendationRequest> findAll() {
        return repository.findAll();
    }

    @Transactional
    public AiRecommendationRequest update(long id, AiRecommendationRequest replacement) {
        AiRecommendationRequest current = requireExisting(id);
        current.setCustomer(replacement.getCustomer());
        current.setInsuranceGoals(replacement.getInsuranceGoals());
        current.setPreferredTerm(replacement.getPreferredTerm());
        current.setPreferredPolicyType(replacement.getPreferredPolicyType());
        current.setCalculatedAge(replacement.getCalculatedAge());
        current.setModelName(replacement.getModelName());
        current.setModelVersion(replacement.getModelVersion());
        current.setPromptVersion(replacement.getPromptVersion());
        repository.flush();
        return current;
    }

    @Transactional
    public void delete(long id) {
        repository.delete(requireExisting(id));
    }

    private AiRecommendationRequest requireExisting(long id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("AiRecommendationRequest with ID " + id + " was not found"));
    }

    @Transactional
    public void seed() {
        List<Map<String, String>> rows = SeedCsv.read("ai_recommendation_request");
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
