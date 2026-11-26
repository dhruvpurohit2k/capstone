package com.accenture.dhruv.healthdekho.policy.dto;

import com.accenture.dhruv.healthdekho.product.model.PremiumFrequency;

import java.math.BigDecimal;

public record AvailablePolicyResponse(
        Long productId,
        String productCode,
        String policyName,
        String policyType,
        int termMonths,
        BigDecimal sumInsured,
        PremiumFrequency premiumFrequency,
        BigDecimal premiumAmount) {
}
