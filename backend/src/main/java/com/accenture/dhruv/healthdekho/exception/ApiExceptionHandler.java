package com.accenture.dhruv.healthdekho.exception;

import com.accenture.dhruv.healthdekho.common.ApiError;
import com.accenture.dhruv.healthdekho.common.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiResponse<Void>> handleApiException(ApiException error) {
        return ResponseEntity.status(error.status()).body(error.response());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException error) {
        var errors = error.getBindingResult().getFieldErrors().stream()
                .map(field -> new ApiError(field.getField(), field.getDefaultMessage()))
                .toList();
        return ResponseEntity.badRequest().body(ApiResponse.failure(HttpStatus.BAD_REQUEST, errors));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnreadableBody() {
        return handleApiException(new ApiException(HttpStatus.BAD_REQUEST, "body", "Expected a valid JSON request body"));
    }
}
