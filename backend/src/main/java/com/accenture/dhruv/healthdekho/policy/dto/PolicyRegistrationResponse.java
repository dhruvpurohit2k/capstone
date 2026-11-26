package com.accenture.dhruv.healthdekho.policy.dto;

public record PolicyRegistrationResponse(
        Long policyId, String policyNumber, String status, String message, PolicyRegistrationPreview details) {
}
