package com.accenture.dhruv.healthdekho.policy.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public record PolicyRegistrationRequest(
        @NotNull @Positive Long productId,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @Positive Long recommendationItemId) {
}
