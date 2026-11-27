package com.accenture.dhruv.healthdekho.recommendation.service;

import com.accenture.dhruv.healthdekho.recommendation.repository.AiRecommendationItemRepository;
import com.accenture.dhruv.healthdekho.seed.SeedCsv;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class AiRecommendationItemService {

    private final AiRecommendationItemRepository repository;

    public AiRecommendationItemService(AiRecommendationItemRepository repository) {
        this.repository = repository;
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
