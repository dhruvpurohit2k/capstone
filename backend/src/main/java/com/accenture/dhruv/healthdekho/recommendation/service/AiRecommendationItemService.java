package com.accenture.dhruv.healthdekho.recommendation.service;

import com.accenture.dhruv.healthdekho.recommendation.model.AiRecommendationItem;
import com.accenture.dhruv.healthdekho.recommendation.repository.AiRecommendationItemRepository;
import com.accenture.dhruv.healthdekho.seed.SeedCsv;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class AiRecommendationItemService {

    private final AiRecommendationItemRepository repository;

    public AiRecommendationItemService(AiRecommendationItemRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public AiRecommendationItem create(AiRecommendationItem entity) {
        if (entity.getId() != null) {
            throw new IllegalArgumentException("New records must use a generated ID");
        }
        return repository.create(entity);
    }

    @Transactional(readOnly = true)
    public Optional<AiRecommendationItem> findById(long id) {
        return repository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<AiRecommendationItem> findAll() {
        return repository.findAll();
    }

    @Transactional
    public AiRecommendationItem update(long id, AiRecommendationItem replacement) {
        AiRecommendationItem current = requireExisting(id);
        current.setRecommendationRequest(replacement.getRecommendationRequest());
        current.setProduct(replacement.getProduct());
        current.setRankOrder(replacement.getRankOrder());
        current.setRecommendationReason(replacement.getRecommendationReason());
        current.setSuitability(replacement.getSuitability());
        current.setSelectedForRegistration(replacement.isSelectedForRegistration());
        repository.flush();
        return current;
    }

    @Transactional
    public void delete(long id) {
        repository.delete(requireExisting(id));
    }

    private AiRecommendationItem requireExisting(long id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("AiRecommendationItem with ID " + id + " was not found"));
    }

    @Transactional
    public void seed() {
        List<Map<String, String>> rows = SeedCsv.read("ai_recommendation_item");
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
