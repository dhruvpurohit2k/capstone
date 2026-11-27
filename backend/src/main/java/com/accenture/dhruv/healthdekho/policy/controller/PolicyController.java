package com.accenture.dhruv.healthdekho.policy.controller;

import com.accenture.dhruv.healthdekho.common.ApiResponse;
import com.accenture.dhruv.healthdekho.exception.ApiException;
import com.accenture.dhruv.healthdekho.policy.dto.AvailablePolicyResponse;
import com.accenture.dhruv.healthdekho.policy.dto.DueInstallmentResponse;
import com.accenture.dhruv.healthdekho.policy.dto.DueSumResponse;
import com.accenture.dhruv.healthdekho.policy.dto.PaymentActivityItem;
import com.accenture.dhruv.healthdekho.policy.dto.PolicyRegistrationPreview;
import com.accenture.dhruv.healthdekho.policy.dto.PolicyRegistrationRequest;
import com.accenture.dhruv.healthdekho.policy.dto.PolicyRegistrationResponse;
import com.accenture.dhruv.healthdekho.policy.dto.PolicyStatusResponse;
import com.accenture.dhruv.healthdekho.policy.dto.SubscribedPolicyResponse;
import com.accenture.dhruv.healthdekho.policy.service.CustomerPolicyService;
import com.accenture.dhruv.healthdekho.policy.service.PolicyRegistrationService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.TransactionException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/policy")
public class PolicyController {

    private static final Logger LOG = LoggerFactory.getLogger(PolicyController.class);

    private final CustomerPolicyService policies;
    private final PolicyRegistrationService registration;

    public PolicyController(CustomerPolicyService policies, PolicyRegistrationService registration) {
        this.policies = policies;
        this.registration = registration;
    }

    @GetMapping("/subscribed")
    public ApiResponse<List<SubscribedPolicyResponse>> subscribed(Principal customer) {
        return ApiResponse.success(HttpStatus.OK, policies.findSubscribed(Long.parseLong(customer.getName())));
    }

    @GetMapping("/{policyId}/status")
    public ApiResponse<PolicyStatusResponse> status(Principal customer, @PathVariable("policyId") long policyId) {
        return ApiResponse.success(HttpStatus.OK,
                policies.findStatus(Long.parseLong(customer.getName()), policyId, LocalDate.now()));
    }

    @GetMapping("/browse")
    public ApiResponse<List<AvailablePolicyResponse>> browse(Principal customer) {
        return ApiResponse.success(HttpStatus.OK, policies.findAvailable(Long.parseLong(customer.getName())));
    }

    @GetMapping("/due")
    public ApiResponse<List<DueInstallmentResponse>> due(Principal customer) {
        return ApiResponse.success(HttpStatus.OK,
                policies.findDue(Long.parseLong(customer.getName()), LocalDate.now()));
    }

    @GetMapping("/due/sum")
    public ApiResponse<DueSumResponse> dueSum(Principal customer) {
        return ApiResponse.success(HttpStatus.OK,
                policies.sumDue(Long.parseLong(customer.getName()), LocalDate.now()));
    }

    @GetMapping("/due/upcoming")
    public ApiResponse<List<DueInstallmentResponse>> upcoming(Principal customer) {
        return ApiResponse.success(HttpStatus.OK,
                policies.findUpcoming(Long.parseLong(customer.getName()), LocalDate.now()));
    }

    @GetMapping("/payments")
    public ApiResponse<List<PaymentActivityItem>> payments(Principal customer) {
        return ApiResponse.success(HttpStatus.OK,
                policies.findPayments(Long.parseLong(customer.getName()), LocalDate.now()));
    }

    @GetMapping("/register/preview")
    public ApiResponse<PolicyRegistrationPreview> previewRegistration(
            Principal customer, @Valid @ModelAttribute PolicyRegistrationRequest request) {
        return ApiResponse.success(HttpStatus.OK,
                registration.preview(Long.parseLong(customer.getName()), request));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<PolicyRegistrationResponse>> register(
            Principal customer, @Valid @RequestBody PolicyRegistrationRequest request) {
        try {
            var result = registration.register(Long.parseLong(customer.getName()), request);
            return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(HttpStatus.CREATED, result));
        } catch (DataAccessException | TransactionException error) {
            LOG.error("Policy registration persistence failed", error);
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, null, "Could not register the policy. Please try again.");
        }
    }
}
