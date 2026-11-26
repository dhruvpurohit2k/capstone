package com.accenture.dhruv.healthdekho.policy.controller;

import com.accenture.dhruv.healthdekho.common.ApiResponse;
import com.accenture.dhruv.healthdekho.policy.dto.AvailablePolicyResponse;
import com.accenture.dhruv.healthdekho.policy.dto.SubscribedPolicyResponse;
import com.accenture.dhruv.healthdekho.policy.service.CustomerPolicyService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/policy")
public class PolicyController {

    private final CustomerPolicyService policies;

    public PolicyController(CustomerPolicyService policies) {
        this.policies = policies;
    }

    @GetMapping("/subscribed")
    public ApiResponse<List<SubscribedPolicyResponse>> subscribed(Principal customer) {
        return ApiResponse.success(HttpStatus.OK, policies.findSubscribed(Long.parseLong(customer.getName())));
    }

    @GetMapping("/browse")
    public ApiResponse<List<AvailablePolicyResponse>> browse(Principal customer) {
        return ApiResponse.success(HttpStatus.OK, policies.findAvailable(Long.parseLong(customer.getName())));
    }
}
