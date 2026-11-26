package com.accenture.dhruv.healthdekho.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.http.HttpStatusCode;

import java.util.List;
import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(int status, boolean success, T data, List<ApiError> errors) {

    public ApiResponse {
        HttpStatusCode httpStatus = HttpStatusCode.valueOf(status);
        if (success) {
            if (!httpStatus.is2xxSuccessful() || errors != null) {
                throw new IllegalArgumentException("A successful response requires a 2xx status and no errors");
            }
            Objects.requireNonNull(data, "A successful response requires data");
        } else {
            if (!httpStatus.isError() || data != null || errors == null || errors.isEmpty()) {
                throw new IllegalArgumentException("A failure requires a 4xx or 5xx status, errors and no data");
            }
            errors = List.copyOf(errors);
        }
    }

    public static <T> ApiResponse<T> success(HttpStatusCode status, T data) {
        return new ApiResponse<>(status.value(), true, data, null);
    }

    public static <T> ApiResponse<T> failure(HttpStatusCode status, List<ApiError> errors) {
        return new ApiResponse<>(status.value(), false, null, errors);
    }
}
