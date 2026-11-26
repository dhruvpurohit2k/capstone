package com.accenture.dhruv.healthdekho.controller;

import com.accenture.dhruv.healthdekho.common.ApiError;
import com.accenture.dhruv.healthdekho.common.ApiResponse;
import com.accenture.dhruv.healthdekho.policy.model.CustomerPolicy;
import com.accenture.dhruv.healthdekho.policy.service.CustomerPolicyService;
import com.accenture.dhruv.healthdekho.premium.model.PaymentStatus;
import com.accenture.dhruv.healthdekho.premium.model.PremiumInstallment;
import com.accenture.dhruv.healthdekho.premium.model.PremiumPayment;
import com.accenture.dhruv.healthdekho.premium.service.PremiumInstallmentService;
import com.accenture.dhruv.healthdekho.premium.service.PremiumPaymentService;
import com.accenture.dhruv.healthdekho.product.model.InsuranceProduct;
import com.accenture.dhruv.healthdekho.product.model.PremiumFrequency;
import com.accenture.dhruv.healthdekho.product.service.InsuranceProductService;
import com.accenture.dhruv.healthdekho.recommendation.model.AiRecommendationItem;
import com.accenture.dhruv.healthdekho.recommendation.model.AiRecommendationRequest;
import com.accenture.dhruv.healthdekho.recommendation.service.AiRecommendationItemService;
import com.accenture.dhruv.healthdekho.recommendation.service.AiRecommendationRequestService;
import com.accenture.dhruv.healthdekho.customer.model.Customer;
import com.accenture.dhruv.healthdekho.customer.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@RestController
@RequestMapping("/api/dev")
@Profile("development & !production")
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class BasicCrudController {

    private final CustomerService customers;
    private final InsuranceProductService products;
    private final CustomerPolicyService policies;
    private final PremiumInstallmentService installments;
    private final PremiumPaymentService payments;
    private final AiRecommendationRequestService recommendationRequests;
    private final AiRecommendationItemService recommendationItems;

    @GetMapping("/customers")
    public ApiResponse<List<CustomerView>> customers() {
        return collection(customers.findAll(), CustomerView::from);
    }

    @GetMapping("/customers/{id}")
    public ResponseEntity<ApiResponse<CustomerView>> customersById(@PathVariable("id") long id) {
        return item(customers.findById(id), CustomerView::from, id);
    }

    @GetMapping("/insurance-products")
    public ApiResponse<List<InsuranceProductView>> products() {
        return collection(products.findAll(), InsuranceProductView::from);
    }

    @GetMapping("/insurance-products/{id}")
    public ResponseEntity<ApiResponse<InsuranceProductView>> productsById(@PathVariable("id") long id) {
        return item(products.findById(id), InsuranceProductView::from, id);
    }

    @GetMapping("/customer-policies")
    public ApiResponse<List<CustomerPolicyView>> policies() {
        return collection(policies.findAll(), CustomerPolicyView::from);
    }

    @GetMapping("/customer-policies/{id}")
    public ResponseEntity<ApiResponse<CustomerPolicyView>> policiesById(@PathVariable("id") long id) {
        return item(policies.findById(id), CustomerPolicyView::from, id);
    }

    @GetMapping("/premium-installments")
    public ApiResponse<List<PremiumInstallmentView>> installments() {
        return collection(installments.findAll(), PremiumInstallmentView::from);
    }

    @GetMapping("/premium-installments/{id}")
    public ResponseEntity<ApiResponse<PremiumInstallmentView>> installmentsById(@PathVariable("id") long id) {
        return item(installments.findById(id), PremiumInstallmentView::from, id);
    }

    @GetMapping("/premium-payments")
    public ApiResponse<List<PremiumPaymentView>> payments() {
        return collection(payments.findAll(), PremiumPaymentView::from);
    }

    @GetMapping("/premium-payments/{id}")
    public ResponseEntity<ApiResponse<PremiumPaymentView>> paymentsById(@PathVariable("id") long id) {
        return item(payments.findById(id), PremiumPaymentView::from, id);
    }

    @GetMapping("/ai-recommendation-requests")
    public ApiResponse<List<AiRecommendationRequestView>> recommendationRequests() {
        return collection(recommendationRequests.findAll(), AiRecommendationRequestView::from);
    }

    @GetMapping("/ai-recommendation-requests/{id}")
    public ResponseEntity<ApiResponse<AiRecommendationRequestView>> recommendationRequestsById(@PathVariable("id") long id) {
        return item(recommendationRequests.findById(id), AiRecommendationRequestView::from, id);
    }

    @GetMapping("/ai-recommendation-items")
    public ApiResponse<List<AiRecommendationItemView>> recommendationItems() {
        return collection(recommendationItems.findAll(), AiRecommendationItemView::from);
    }

    @GetMapping("/ai-recommendation-items/{id}")
    public ResponseEntity<ApiResponse<AiRecommendationItemView>> recommendationItemsById(@PathVariable("id") long id) {
        return item(recommendationItems.findById(id), AiRecommendationItemView::from, id);
    }

    private static <T, V> ApiResponse<List<V>> collection(List<T> rows, Function<T, V> toView) {
        return ApiResponse.success(HttpStatus.OK, rows.stream().map(toView).toList());
    }

    private static <T, V> ResponseEntity<ApiResponse<V>> item(Optional<T> row, Function<T, V> toView, long id) {
        return row.map(toView)
                .map(view -> ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, view)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        ApiResponse.failure(HttpStatus.NOT_FOUND,
                                List.of(new ApiError("id", "No record found with ID " + id)))));
    }

    public record CustomerView(Long id, String email, String fullName, LocalDate dateOfBirth, String phone,
            String status, Instant createdAt, Instant updatedAt) {
        private static CustomerView from(Customer row) {
            return new CustomerView(row.getId(), row.getEmail(), row.getFullName(), row.getDateOfBirth(),
                    row.getPhone(), row.getStatus(), row.getCreatedAt(), row.getUpdatedAt());
        }
    }

    public record InsuranceProductView(Long id, String productCode, String policyName, String policyType,
            int termMonths, PremiumFrequency premiumFrequency, BigDecimal premiumAmount, BigDecimal sumInsured,
            int minEntryAge, int maxEntryAge, boolean active) {
        private static InsuranceProductView from(InsuranceProduct row) {
            return new InsuranceProductView(row.getId(), row.getProductCode(), row.getPolicyName(),
                    row.getPolicyType(), row.getTermMonths(), row.getPremiumFrequency(), row.getPremiumAmount(),
                    row.getSumInsured(), row.getMinEntryAge(), row.getMaxEntryAge(), row.isActive());
        }
    }

    public record CustomerPolicyView(Long id, String policyNumber, Long customerId, Long productId,
            LocalDate startDate, LocalDate maturityDate, PremiumFrequency premiumFrequency, BigDecimal premiumAmount,
            BigDecimal sumInsured, BigDecimal totalPremiumPayable, String status, Instant createdAt) {
        private static CustomerPolicyView from(CustomerPolicy row) {
            return new CustomerPolicyView(row.getId(), row.getPolicyNumber(), row.getCustomer().getId(),
                    row.getProduct().getId(), row.getStartDate(), row.getMaturityDate(),
                    row.getPremiumFrequency(), row.getPremiumAmount(), row.getSumInsured(),
                    row.getTotalPremiumPayable(), row.getStatus(), row.getCreatedAt());
        }
    }

    public record PremiumInstallmentView(Long id, Long policyId, int installmentNumber, LocalDate dueDate,
            LocalDate gracePeriodEndDate, BigDecimal scheduledAmount, BigDecimal assessedPenaltyAmount,
            BigDecimal paidAmount, PaymentStatus paymentStatus) {
        private static PremiumInstallmentView from(PremiumInstallment row) {
            return new PremiumInstallmentView(row.getId(), row.getPolicy().getId(), row.getInstallmentNumber(),
                    row.getDueDate(), row.getGracePeriodEndDate(), row.getScheduledAmount(),
                    row.getAssessedPenaltyAmount(), row.getPaidAmount(), row.getPaymentStatus());
        }
    }

    public record PremiumPaymentView(Long id, Long installmentId, String transactionReference,
            LocalDate paymentDate, BigDecimal amountPaid, BigDecimal penaltyAmount, String paymentMethod,
            Instant createdAt) {
        private static PremiumPaymentView from(PremiumPayment row) {
            return new PremiumPaymentView(row.getId(), row.getInstallment().getId(),
                    row.getTransactionReference(), row.getPaymentDate(), row.getAmountPaid(),
                    row.getPenaltyAmount(), row.getPaymentMethod(), row.getCreatedAt());
        }
    }

    public record AiRecommendationRequestView(Long id, Long customerId, String insuranceGoals,
            Integer preferredTerm, String preferredPolicyType, int calculatedAge, String modelName, String modelVersion,
            String promptVersion, Instant requestedAt) {
        private static AiRecommendationRequestView from(AiRecommendationRequest row) {
            return new AiRecommendationRequestView(row.getId(), row.getCustomer().getId(),
                    row.getInsuranceGoals(), row.getPreferredTerm(), row.getPreferredPolicyType(),
                    row.getCalculatedAge(), row.getModelName(), row.getModelVersion(), row.getPromptVersion(),
                    row.getRequestedAt());
        }
    }

    public record AiRecommendationItemView(Long id, Long recommendationRequestId, Long productId, int rankOrder,
            String recommendationReason, String suitability, boolean selectedForRegistration) {
        private static AiRecommendationItemView from(AiRecommendationItem row) {
            return new AiRecommendationItemView(row.getId(), row.getRecommendationRequest().getId(),
                    row.getProduct().getId(), row.getRankOrder(), row.getRecommendationReason(),
                    row.getSuitability(), row.isSelectedForRegistration());
        }
    }
}
