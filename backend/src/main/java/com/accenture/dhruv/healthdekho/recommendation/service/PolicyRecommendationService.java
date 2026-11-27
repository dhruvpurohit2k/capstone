package com.accenture.dhruv.healthdekho.recommendation.service;

import com.accenture.dhruv.healthdekho.customer.repository.CustomerRepository;
import com.accenture.dhruv.healthdekho.exception.ApiException;
import com.accenture.dhruv.healthdekho.exception.UnauthenticatedException;
import com.accenture.dhruv.healthdekho.policy.dto.AvailablePolicyResponse;
import com.accenture.dhruv.healthdekho.product.model.InsuranceProduct;
import com.accenture.dhruv.healthdekho.product.repository.InsuranceProductRepository;
import com.accenture.dhruv.healthdekho.recommendation.dto.RecommendationRequest;
import com.accenture.dhruv.healthdekho.recommendation.dto.RecommendationResponse;
import com.accenture.dhruv.healthdekho.recommendation.dto.RecommendedPolicy;
import com.accenture.dhruv.healthdekho.recommendation.model.AiRecommendationItem;
import com.accenture.dhruv.healthdekho.recommendation.model.AiRecommendationRequest;
import com.accenture.dhruv.healthdekho.recommendation.repository.AiRecommendationItemRepository;
import com.accenture.dhruv.healthdekho.recommendation.repository.AiRecommendationRequestRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class PolicyRecommendationService {

    private static final Set<String> SUITABILITY = Set.of("HIGH", "MEDIUM", "LOW");
    private final CustomerRepository customers;
    private final InsuranceProductRepository products;
    private final AiRecommendationRequestRepository requests;
    private final AiRecommendationItemRepository items;
    private final RecommendationModel model;
    private final TransactionTemplate transactions;

    public PolicyRecommendationService(CustomerRepository customers, InsuranceProductRepository products,
                                       AiRecommendationRequestRepository requests, AiRecommendationItemRepository items,
                                       RecommendationModel model, TransactionTemplate transactions) {
        this.customers = customers;
        this.products = products;
        this.requests = requests;
        this.items = items;
        this.model = model;
        this.transactions = transactions;
    }

    public RecommendationResponse recommend(long customerId, RecommendationRequest input) {
        if (input.dateOfBirth() == null && input.preferredTermMonths() == null
                && !StringUtils.hasText(input.insuranceGoals()) && !StringUtils.hasText(input.preferredPolicyType())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "preferences", "Fill in at least one recommendation field");
        }
        var customer = customers.findById(customerId).filter(account -> "ACTIVE".equals(account.getStatus()))
                .orElseThrow(() -> new UnauthenticatedException("Authorization", "An active customer is required"));
        LocalDate birthDate = customer.getDateOfBirth();
        LocalDate today = LocalDate.now();
        if (birthDate.isAfter(today) || (input.dateOfBirth() != null && !input.dateOfBirth().equals(birthDate))) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "dateOfBirth", "Use the date of birth on your account");
        }
        int age = Period.between(birthDate, today).getYears();
        var catalog = eligibleProducts(customerId, age).stream()
                .map(product -> new AvailablePolicyResponse(product.getId(), product.getProductCode(),
                        product.getPolicyName(), product.getPolicyType(), product.getTermMonths(),
                        product.getSumInsured(), product.getPremiumFrequency(), product.getPremiumAmount()))
                .toList();
        Set<Long> offeredIds = catalog.stream().map(AvailablePolicyResponse::productId).collect(Collectors.toSet());
        var choices = catalog.isEmpty() ? List.<RecommendationModel.Choice>of()
                : model.recommend(age, input.insuranceGoals(), input.preferredTermMonths(), input.preferredPolicyType(), catalog);

        return transactions.execute(status -> {
            var available = eligibleProducts(customerId, age).stream()
                    .filter(product -> offeredIds.contains(product.getId()))
                    .collect(Collectors.toMap(InsuranceProduct::getId, Function.identity()));
            AiRecommendationRequest request = new AiRecommendationRequest();
            request.setCustomer(customer);
            request.setCalculatedAge(age);
            request.setInsuranceGoals(input.insuranceGoals());
            request.setPreferredTerm(input.preferredTermMonths());
            request.setPreferredPolicyType(input.preferredPolicyType());
            request.setModelName(catalog.isEmpty() ? "not-invoked" : model.modelName());
            request.setModelVersion(catalog.isEmpty() ? "not-invoked" : model.modelVersion());
            request.setPromptVersion(RecommendationModel.PROMPT_VERSION);
            requests.save(request);

            List<RecommendedPolicy> results = new ArrayList<>();
            Set<Long> seen = new HashSet<>();
            for (var choice : choices) {
                if (results.size() == 5) break;
                if (choice == null || choice.productId() == null || choice.reason() == null || choice.reason().isBlank()
                        || choice.reason().length() > 600 || choice.suitability() == null || !SUITABILITY.contains(choice.suitability())) {
                    continue;
                }
                InsuranceProduct product = available.get(choice.productId());
                if (product == null || !seen.add(product.getId())) continue;

                AiRecommendationItem item = new AiRecommendationItem();
                item.setRecommendationRequest(request);
                item.setProduct(product);
                item.setRankOrder(results.size() + 1);
                item.setRecommendationReason(choice.reason().trim());
                item.setSuitability(choice.suitability());
                items.save(item);

                results.add(new RecommendedPolicy(item.getId(), product.getId(), product.getProductCode(),
                        product.getPolicyName(), product.getPolicyType(), product.getTermMonths(), product.getSumInsured(),
                        product.getPremiumFrequency(), product.getPremiumAmount(), item.getRankOrder(),
                        item.getRecommendationReason(), item.getSuitability()));
            }
            return new RecommendationResponse(request.getId(), age, results);
        });
    }

    private List<InsuranceProduct> eligibleProducts(long customerId, int age) {
        return products.findAvailableForCustomer(customerId).stream()
                .filter(product -> product.acceptsEntryAge(age))
                .toList();
    }
}
