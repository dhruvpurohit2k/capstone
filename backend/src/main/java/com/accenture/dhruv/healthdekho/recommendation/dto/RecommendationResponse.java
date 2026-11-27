package com.accenture.dhruv.healthdekho.recommendation.dto;

import java.util.List;

public record RecommendationResponse(Long recommendationRequestId, int calculatedAge,
                                     List<RecommendedPolicy> recommendations) {
}
