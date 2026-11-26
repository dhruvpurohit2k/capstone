package com.accenture.dhruv.healthdekho.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record SignupRequest(
        @NotBlank @Email @Size(max = 254) String email,
        @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
        @NotBlank @Size(min = 8, max = 72) String password,
        @NotBlank @Size(max = 255) String fullName,
        @NotNull @PastOrPresent LocalDate dateOfBirth,
        @NotBlank @Pattern(regexp = "\\+?[0-9]{7,15}", message = "Use 7 to 15 digits with an optional leading +")
        String phone) {
}
