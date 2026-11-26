package com.accenture.dhruv.healthdekho.exception;

import org.springframework.http.HttpStatus;

public class PolicyEligibilityException extends ApiException {

    public PolicyEligibilityException() {
        super(HttpStatus.BAD_REQUEST, "productId", "Customer does not meet the product's entry age requirements");
    }
}
