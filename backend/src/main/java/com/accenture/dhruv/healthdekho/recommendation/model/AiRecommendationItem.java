package com.accenture.dhruv.healthdekho.recommendation.model;

import com.accenture.dhruv.healthdekho.product.model.InsuranceProduct;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Check;

@Entity
@Table(name = "ai_recommendation_item",
        uniqueConstraints = @UniqueConstraint(name = "uq_ai_recommendation_item_request_product", columnNames = {"recommendation_request_id", "product_id"}))
@Check(constraints = "rank_order > 0")
@Getter
@NoArgsConstructor
@Setter
public class AiRecommendationItem {

    @Setter(AccessLevel.NONE)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "recommendation_item_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recommendation_request_id", nullable = false)
    private AiRecommendationRequest recommendationRequest;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private InsuranceProduct product;

    @Column(name = "rank_order", nullable = false)
    private int rankOrder;

    @Column(name = "recommendation_reason", nullable = false, columnDefinition = "text")
    private String recommendationReason;

    @Column(name = "suitability", nullable = false, length = 32)
    private String suitability;

    @Column(name = "selected_for_registration", nullable = false)
    private boolean selectedForRegistration;
}
