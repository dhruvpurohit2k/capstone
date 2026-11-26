package com.accenture.dhruv.healthdekho.policy.dto;

import com.accenture.dhruv.healthdekho.product.model.PremiumFrequency;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PolicyRegistrationPreview(
        Long productId,
        String productCode,
        String customerName,
        LocalDate dateOfBirth,
        String email,
        String phone,
        String policyName,
        String policyType,
        int termMonths,
        LocalDate startDate,
        LocalDate maturityDate,
        PremiumFrequency premiumFrequency,
        BigDecimal premiumAmount,
        BigDecimal sumInsured,
        int installmentCount,
        BigDecimal totalPremiumPayable) {
}
