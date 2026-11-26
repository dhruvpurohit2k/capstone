package com.accenture.dhruv.healthdekho.seed;

import com.accenture.dhruv.healthdekho.policy.service.CustomerPolicyService;
import com.accenture.dhruv.healthdekho.premium.service.PremiumInstallmentService;
import com.accenture.dhruv.healthdekho.premium.service.PremiumPaymentService;
import com.accenture.dhruv.healthdekho.product.service.InsuranceProductService;
import com.accenture.dhruv.healthdekho.recommendation.service.AiRecommendationItemService;
import com.accenture.dhruv.healthdekho.recommendation.service.AiRecommendationRequestService;
import com.accenture.dhruv.healthdekho.user.service.CustomerService;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Profile("development & !production")
public class DatabaseSeeder {

    private final CustomerService customers;
    private final InsuranceProductService products;
    private final CustomerPolicyService policies;
    private final PremiumInstallmentService installments;
    private final PremiumPaymentService payments;
    private final AiRecommendationRequestService recommendationRequests;
    private final AiRecommendationItemService recommendationItems;

    public DatabaseSeeder(CustomerService customers, InsuranceProductService products,
                          CustomerPolicyService policies, PremiumInstallmentService installments,
                          PremiumPaymentService payments, AiRecommendationRequestService recommendationRequests,
                          AiRecommendationItemService recommendationItems) {
        this.customers = customers;
        this.products = products;
        this.policies = policies;
        this.installments = installments;
        this.payments = payments;
        this.recommendationRequests = recommendationRequests;
        this.recommendationItems = recommendationItems;
    }

    @EventListener(ApplicationStartedEvent.class)
    @Transactional
    public void seed() {
        customers.seed();
        products.seed();
        policies.seed();
        installments.seed();
        payments.seed();
        recommendationRequests.seed();
        recommendationItems.seed();
    }
}
