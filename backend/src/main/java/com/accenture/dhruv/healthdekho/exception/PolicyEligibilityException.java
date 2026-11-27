package com.accenture.dhruv.healthdekho.exception;

import org.springframework.http.HttpStatus;

public class PolicyEligibilityException extends ApiException {

    public PolicyEligibilityException() {
        super(HttpStatus.BAD_REQUEST, "startDate", "Customer does not meet the product's entry age requirements on the policy start date");
    }
}
