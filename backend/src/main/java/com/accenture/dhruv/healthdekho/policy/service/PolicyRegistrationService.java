package com.accenture.dhruv.healthdekho.policy.service;

import com.accenture.dhruv.healthdekho.customer.model.Customer;
import com.accenture.dhruv.healthdekho.customer.repository.CustomerRepository;
import com.accenture.dhruv.healthdekho.exception.ApiException;
import com.accenture.dhruv.healthdekho.exception.DuplicateEntityException;
import com.accenture.dhruv.healthdekho.exception.PolicyEligibilityException;
import com.accenture.dhruv.healthdekho.exception.UnauthenticatedException;
import com.accenture.dhruv.healthdekho.policy.dto.PolicyRegistrationPreview;
import com.accenture.dhruv.healthdekho.policy.dto.PolicyRegistrationRequest;
import com.accenture.dhruv.healthdekho.policy.dto.PolicyRegistrationResponse;
import com.accenture.dhruv.healthdekho.policy.model.CustomerPolicy;
import com.accenture.dhruv.healthdekho.policy.repository.CustomerPolicyRepository;
import com.accenture.dhruv.healthdekho.premium.model.PremiumInstallment;
import com.accenture.dhruv.healthdekho.premium.repository.PremiumInstallmentRepository;
import com.accenture.dhruv.healthdekho.product.model.InsuranceProduct;
import com.accenture.dhruv.healthdekho.product.model.PremiumFrequency;
import com.accenture.dhruv.healthdekho.product.repository.InsuranceProductRepository;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.Period;
import java.util.UUID;
import java.util.stream.IntStream;

@Service
public class PolicyRegistrationService {

    private final CustomerRepository customers;
    private final InsuranceProductRepository products;
    private final CustomerPolicyRepository policies;
    private final PremiumInstallmentRepository installments;

    public PolicyRegistrationService(CustomerRepository customers, InsuranceProductRepository products,
                                     CustomerPolicyRepository policies, PremiumInstallmentRepository installments) {
        this.customers = customers;
        this.products = products;
        this.policies = policies;
        this.installments = installments;
    }

    @Transactional(readOnly = true)
    public PolicyRegistrationPreview preview(long customerId, PolicyRegistrationRequest request) {
        return calculate(findCustomer(customerId), findProduct(request.productId()), request.startDate());
    }

    @Transactional
    public PolicyRegistrationResponse register(long customerId, PolicyRegistrationRequest request) {
        Customer customer = findCustomer(customerId);
        InsuranceProduct product = findProduct(request.productId());
        PolicyRegistrationPreview details = calculate(customer, product, request.startDate());

        CustomerPolicy policy = new CustomerPolicy();
        policy.setPolicyNumber("HDP-" + details.startDate().getYear() + "-" + UUID.randomUUID());
        policy.setCustomer(customer);
        policy.setProduct(product);
        policy.setStartDate(details.startDate());
        policy.setMaturityDate(details.maturityDate());
        policy.setPremiumFrequency(details.premiumFrequency());
        policy.setPremiumAmount(details.premiumAmount());
        policy.setSumInsured(details.sumInsured());
        policy.setTotalPremiumPayable(details.totalPremiumPayable());
        policy.setStatus("ACTIVE");

        try {
            policies.saveAndFlush(policy);
        } catch (DataIntegrityViolationException error) {
            if (error.getCause() instanceof ConstraintViolationException violation
                    && "uq_customer_policy_customer_product".equals(violation.getConstraintName())) {
                throw duplicatePolicy();
            }
            throw error;
        }

        int monthsBetweenPayments = frequencyMonths(details.premiumFrequency());
        var schedule = IntStream.range(0, details.installmentCount())
                .mapToObj(index -> createInstallment(policy, index + 1,
                        details.startDate().plusMonths((long) index * monthsBetweenPayments)))
                .toList();
        installments.saveAll(schedule);

        return new PolicyRegistrationResponse(policy.getId(), policy.getPolicyNumber(), policy.getStatus(),
                "Policy registered successfully", details);
    }

    private PolicyRegistrationPreview calculate(Customer customer, InsuranceProduct product, LocalDate requestedStart) {
        if (!product.isActive()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "productId", "This insurance product is inactive");
        }
        if (policies.existsForCustomerAndProduct(customer.getId(), product.getId())) {
            throw duplicatePolicy();
        }

        LocalDate today = LocalDate.now();
        LocalDate start = requestedStart == null ? today : requestedStart;
        LocalDate birthDate = customer.getDateOfBirth();
        if (birthDate == null || birthDate.isAfter(today)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "dateOfBirth", "Customer date of birth must be valid");
        }
        if (start.isBefore(birthDate) || start.getYear() > 9999) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "startDate", "Policy start date is outside the supported range");
        }
        int age = Period.between(birthDate, start).getYears();
        if (!product.acceptsEntryAge(age)) {
            throw new PolicyEligibilityException();
        }

        int term = product.getTermMonths();
        if (term <= 0 || product.getPremiumAmount().signum() <= 0 || product.getSumInsured().signum() <= 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "productId", "Product term, premium and sum insured must be positive");
        }
        int frequency = frequencyMonths(product.getPremiumFrequency());
        int count = Math.ceilDiv(term, frequency);
        LocalDate maturity;
        try {
            maturity = start.plusMonths(term).minusDays(1);
            LocalDate lastGraceEnd = start.plusMonths((long) (count - 1) * frequency).plusMonths(1);
            if (maturity.isBefore(start) || maturity.getYear() > 9999 || lastGraceEnd.getYear() > 9999) {
                throw new DateTimeException("Policy dates are outside the supported range");
            }
        } catch (DateTimeException error) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "startDate", "Policy dates are outside the supported range");
        }

        BigDecimal total = product.getPremiumAmount().multiply(BigDecimal.valueOf(count));
        if (total.precision() > 19) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "productId", "Total premium exceeds the supported amount");
        }

        return new PolicyRegistrationPreview(product.getId(), product.getProductCode(), customer.getFullName(),
                birthDate, customer.getEmail(), customer.getPhone(), product.getPolicyName(), product.getPolicyType(),
                term, start, maturity, product.getPremiumFrequency(), product.getPremiumAmount(),
                product.getSumInsured(), count, total);
    }

    private PremiumInstallment createInstallment(CustomerPolicy policy, int number, LocalDate dueDate) {
        PremiumInstallment installment = new PremiumInstallment();
        installment.setPolicy(policy);
        installment.setInstallmentNumber(number);
        installment.setDueDate(dueDate);
        installment.setGracePeriodEndDate(dueDate.plusMonths(1));
        installment.setScheduledAmount(policy.getPremiumAmount());
        return installment;
    }

    private Customer findCustomer(long id) {
        return customers.findById(id).filter(customer -> "ACTIVE".equals(customer.getStatus()))
                .orElseThrow(() -> new UnauthenticatedException("Authorization", "An active customer is required"));
    }

    private InsuranceProduct findProduct(long id) {
        return products.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "productId", "Insurance product was not found"));
    }

    private DuplicateEntityException duplicatePolicy() {
        return new DuplicateEntityException("productId", "Customer already has a policy for this product");
    }

    private int frequencyMonths(PremiumFrequency frequency) {
        return switch (frequency) {
            case MONTHLY -> 1;
            case QUARTERLY -> 3;
            case HALF_YEARLY -> 6;
            case YEARLY -> 12;
        };
    }
}
