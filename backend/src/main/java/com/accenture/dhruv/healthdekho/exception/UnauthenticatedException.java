package com.accenture.dhruv.healthdekho.exception;

import org.springframework.http.HttpStatus;

public class UnauthenticatedException extends ApiException {

    public UnauthenticatedException(String field, String message) {
        super(HttpStatus.UNAUTHORIZED, field, message);
    }
}
