package com.accenture.dhruv.healthdekho.auth.dto;

import java.time.LocalDate;

public record SignupResponse(Long id, String email, String fullName, LocalDate dateOfBirth,
                             String phone, String status) {
}
