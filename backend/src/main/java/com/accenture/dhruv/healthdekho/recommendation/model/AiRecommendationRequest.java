package com.accenture.dhruv.healthdekho.recommendation.model;

import com.accenture.dhruv.healthdekho.customer.model.Customer;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Check;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "ai_recommendation_request",
        indexes = {
                @Index(name = "idx_ai_recommendation_request_customer_id", columnList = "customer_id"),
                @Index(name = "idx_ai_recommendation_request_requested_at", columnList = "requested_at")
        })
@Check(constraints = "calculated_age >= 0 AND (preferred_term IS NULL OR preferred_term > 0)")
@Getter
@NoArgsConstructor
@Setter
public class AiRecommendationRequest {

    @Setter(AccessLevel.NONE)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "recommendation_request_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(name = "insurance_goals", columnDefinition = "text")
    private String insuranceGoals;

    @Column(name = "preferred_term")
    private Integer preferredTerm;

    @Column(name = "preferred_policy_type", length = 32)
    private String preferredPolicyType;

    @Column(name = "calculated_age", nullable = false)
    private int calculatedAge;

    @Column(name = "model_name", nullable = false)
    private String modelName;

    @Column(name = "model_version", nullable = false)
    private String modelVersion;

    @Column(name = "prompt_version", nullable = false)
    private String promptVersion;

    @Setter(AccessLevel.NONE)
    @CreationTimestamp
    @Column(name = "requested_at", nullable = false, updatable = false)
    private Instant requestedAt;
}
