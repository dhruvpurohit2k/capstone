package com.accenture.dhruv.healthdekho.policy.dto;

import com.accenture.dhruv.healthdekho.product.model.PremiumFrequency;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record PolicyStatusResponse(
        Long policyId,
        String policyNumber,
        Long productId,
        String productCode,
        String policyName,
        String policyType,
        int termMonths,
        String status,
        Instant subscribedAt,
        LocalDate startDate,
        LocalDate maturityDate,
        long daysUntilMaturity,
        PremiumFrequency premiumFrequency,
        BigDecimal premiumAmount,
        BigDecimal sumInsured,
        BigDecimal totalPremiumPayable,
        BigDecimal totalPaid,
        BigDecimal remainingPremium,
        BigDecimal outstandingBalance,
        BigDecimal amountDue,
        BigDecimal overdueAmount) {
}
