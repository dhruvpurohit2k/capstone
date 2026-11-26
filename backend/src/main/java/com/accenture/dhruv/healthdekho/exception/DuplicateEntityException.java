package com.accenture.dhruv.healthdekho.exception;

import org.springframework.http.HttpStatus;

public class DuplicateEntityException extends ApiException {

    public DuplicateEntityException(String field, String message) {
        super(HttpStatus.CONFLICT, field, message);
    }
}
