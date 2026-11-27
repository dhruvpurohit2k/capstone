package com.accenture.dhruv.healthdekho.recommendation.dto;

import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record RecommendationRequest(
        @PastOrPresent LocalDate dateOfBirth,
        @Size(max = 1000) String insuranceGoals,
        @Positive Integer preferredTermMonths,
        @Size(max = 32) String preferredPolicyType) {
}
