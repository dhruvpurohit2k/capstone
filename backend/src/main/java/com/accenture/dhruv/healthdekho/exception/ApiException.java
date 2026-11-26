package com.accenture.dhruv.healthdekho.exception;

import com.accenture.dhruv.healthdekho.common.ApiError;
import com.accenture.dhruv.healthdekho.common.ApiResponse;
import org.springframework.http.HttpStatus;

import java.util.List;

public class ApiException extends RuntimeException {

    private final HttpStatus status;
    private final String field;

    public ApiException(HttpStatus status, String field, String message) {
        super(message);
        this.status = status;
        this.field = field;
    }

    public HttpStatus status() {
        return status;
    }

    public ApiResponse<Void> response() {
        return ApiResponse.failure(status, List.of(new ApiError(field, getMessage())));
    }
}
