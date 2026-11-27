package com.accenture.dhruv.healthdekho.recommendation.dto;

import com.accenture.dhruv.healthdekho.product.model.PremiumFrequency;

import java.math.BigDecimal;

public record RecommendedPolicy(
        Long recommendationItemId,
        Long productId,
        String productCode,
        String policyName,
        String policyType,
        int termMonths,
        BigDecimal sumInsured,
        PremiumFrequency premiumFrequency,
        BigDecimal premiumAmount,
        int rank,
        String reason,
        String suitability) {
}
