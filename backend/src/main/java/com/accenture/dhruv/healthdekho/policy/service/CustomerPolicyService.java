package com.accenture.dhruv.healthdekho.policy.service;

import com.accenture.dhruv.healthdekho.customer.repository.CustomerRepository;
import com.accenture.dhruv.healthdekho.exception.ApiException;
import com.accenture.dhruv.healthdekho.exception.UnauthenticatedException;
import com.accenture.dhruv.healthdekho.policy.dto.AvailablePolicyResponse;
import com.accenture.dhruv.healthdekho.policy.dto.DueInstallmentResponse;
import com.accenture.dhruv.healthdekho.policy.dto.DueSumResponse;
import com.accenture.dhruv.healthdekho.policy.dto.PaymentActivityItem;
import com.accenture.dhruv.healthdekho.policy.dto.PolicyStatusResponse;
import com.accenture.dhruv.healthdekho.policy.dto.SubscribedPolicyResponse;
import com.accenture.dhruv.healthdekho.policy.model.CustomerPolicy;
import com.accenture.dhruv.healthdekho.policy.repository.CustomerPolicyRepository;
import com.accenture.dhruv.healthdekho.premium.model.PaymentStatus;
import com.accenture.dhruv.healthdekho.premium.model.PremiumInstallment;
import com.accenture.dhruv.healthdekho.premium.repository.PremiumInstallmentRepository;
import com.accenture.dhruv.healthdekho.premium.service.PremiumPaymentService;
import com.accenture.dhruv.healthdekho.product.repository.InsuranceProductRepository;
import com.accenture.dhruv.healthdekho.seed.SeedCsv;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Service
public class CustomerPolicyService {

    private static final BigDecimal LATE_PENALTY_RATE = new BigDecimal("0.10");
    private static final List<PaymentStatus> OPEN_STATUSES = List.of(
            PaymentStatus.PENDING, PaymentStatus.PARTIALLY_PAID, PaymentStatus.OVERDUE);

    private final CustomerPolicyRepository repository;
    private final InsuranceProductRepository products;
    private final CustomerRepository customers;
    private final PremiumInstallmentRepository installments;
    private final PremiumPaymentService payments;

    public CustomerPolicyService(CustomerPolicyRepository repository, InsuranceProductRepository products,
                                 CustomerRepository customers, PremiumInstallmentRepository installments,
                                 PremiumPaymentService payments) {
        this.repository = repository;
        this.products = products;
        this.customers = customers;
        this.installments = installments;
        this.payments = payments;
    }

    @Transactional(readOnly = true)
    public List<SubscribedPolicyResponse> findSubscribed(long customerId) {
        return repository.findByCustomerId(customerId).stream()
                .map(policy -> {
                    var product = policy.getProduct();
                    return new SubscribedPolicyResponse(policy.getId(), policy.getPolicyNumber(),
                            product.getPolicyName(), product.getPolicyType(), product.getTermMonths(),
                            policy.getStartDate(), policy.getMaturityDate(), policy.getPremiumFrequency(),
                            policy.getPremiumAmount(), policy.getSumInsured(), policy.getStatus());
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AvailablePolicyResponse> findAvailable(long customerId) {
        customers.findById(customerId)
                .filter(account -> "ACTIVE".equals(account.getStatus()))
                .orElseThrow(() -> new UnauthenticatedException("Authorization", "An active customer is required"));
        return products.findAvailableForCustomer(customerId).stream()
                .map(product -> new AvailablePolicyResponse(product.getId(), product.getProductCode(),
                        product.getPolicyName(), product.getPolicyType(), product.getTermMonths(),
                        product.getSumInsured(), product.getPremiumFrequency(), product.getPremiumAmount()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<DueInstallmentResponse> findDue(long customerId, LocalDate today) {
        return installments.findOpenForCustomer(customerId, OPEN_STATUSES).stream()
                .map(installment -> toDueResponse(installment, today))
                .toList();
    }

    @Transactional(readOnly = true)
    public DueSumResponse sumDue(long customerId, LocalDate today) {
        BigDecimal total = findDue(customerId, today).stream()
                .filter(installment -> !installment.dueDate().isAfter(today))
                .map(DueInstallmentResponse::amountDue)
                .reduce(new BigDecimal("0.00"), BigDecimal::add);
        return new DueSumResponse(total);
    }

    @Transactional(readOnly = true)
    public List<DueInstallmentResponse> findUpcoming(long customerId, LocalDate today) {
        var upcoming = findDue(customerId, today).stream()
                .dropWhile(installment -> installment.dueDate().isBefore(today))
                .toList();
        if (upcoming.isEmpty()) {
            return List.of();
        }
        LocalDate nextDueDate = upcoming.getFirst().dueDate();
        return upcoming.stream().takeWhile(installment -> installment.dueDate().equals(nextDueDate)).toList();
    }

    @Transactional(readOnly = true)
    public List<PaymentActivityItem> findPayments(long customerId, LocalDate today) {
        var history = payments.findHistory(customerId).stream()
                .map(payment -> new PaymentActivityItem(PaymentActivityItem.Type.PAYMENT, payment, null));
        var open = findDue(customerId, today).stream()
                .map(installment -> new PaymentActivityItem(PaymentActivityItem.Type.OPEN_INSTALLMENT, null, installment));
        return Stream.concat(history, open).toList();
    }

    @Transactional(readOnly = true)
    public PolicyStatusResponse findStatus(long customerId, long policyId, LocalDate today) {
        CustomerPolicy policy = repository.findOwnedPolicy(customerId, policyId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "policyId", "Policy was not found"));
        var product = policy.getProduct();
        BigDecimal totalPremium = policy.getTotalPremiumPayable();
        BigDecimal totalPaid = new BigDecimal("0.00");
        BigDecimal principalPaid = totalPaid;
        BigDecimal totalPenalty = totalPaid;
        BigDecimal amountDue = totalPaid;
        BigDecimal overdueAmount = totalPaid;

        for (PremiumInstallment installment : installments.findByPolicyId(policyId)) {
            BigDecimal paid = installment.getPaidAmount();
            totalPaid = totalPaid.add(paid);
            principalPaid = principalPaid.add(paid.min(installment.getScheduledAmount()));
            totalPenalty = totalPenalty.add(installment.getAssessedPenaltyAmount());

            if (OPEN_STATUSES.contains(installment.getPaymentStatus())
                    && paid.compareTo(installment.getScheduledAmount().add(installment.getAssessedPenaltyAmount())) < 0) {
                var balance = toDueResponse(installment, today);
                totalPenalty = totalPenalty.add(balance.penaltyAmount().subtract(installment.getAssessedPenaltyAmount()));
                if (!balance.dueDate().isAfter(today)) {
                    amountDue = amountDue.add(balance.amountDue());
                }
                if (balance.overdue()) {
                    overdueAmount = overdueAmount.add(balance.amountDue());
                }
            }
        }

        long daysUntilMaturity = Math.max(0, ChronoUnit.DAYS.between(today, policy.getMaturityDate()));
        return new PolicyStatusResponse(policy.getId(), policy.getPolicyNumber(), product.getId(),
                product.getProductCode(), product.getPolicyName(), product.getPolicyType(), product.getTermMonths(),
                policy.getStatus(), policy.getCreatedAt(), policy.getStartDate(), policy.getMaturityDate(),
                daysUntilMaturity, policy.getPremiumFrequency(), policy.getPremiumAmount(), policy.getSumInsured(),
                totalPremium, totalPaid, totalPremium.subtract(principalPaid).max(BigDecimal.ZERO),
                totalPremium.add(totalPenalty).subtract(totalPaid).max(BigDecimal.ZERO), amountDue, overdueAmount);
    }

    private DueInstallmentResponse toDueResponse(PremiumInstallment installment, LocalDate today) {
        boolean overdue = today.isAfter(installment.getGracePeriodEndDate());
        BigDecimal principalDue = installment.getScheduledAmount().subtract(installment.getPaidAmount());
        BigDecimal penalty = installment.getAssessedPenaltyAmount();
        if (overdue && penalty.signum() == 0) {
            penalty = principalDue.multiply(LATE_PENALTY_RATE).setScale(2, RoundingMode.HALF_UP);
        }
        return new DueInstallmentResponse(installment.getId(), installment.getPolicy().getPolicyNumber(),
                installment.getInstallmentNumber(), installment.getDueDate(), installment.getGracePeriodEndDate(),
                installment.getPaymentStatus(), overdue, installment.getScheduledAmount(), installment.getPaidAmount(),
                penalty, principalDue.add(penalty));
    }

    @Transactional
    public void seed() {
        List<Map<String, String>> rows = SeedCsv.read("customer_policy");
        repository.lockForSeeding();
        int inserted = 0;
        for (Map<String, String> row : rows) {
            inserted += repository.insertSeed(row);
        }
        if (inserted > 0) {
            repository.advanceIdentity();
        }
    }
}
