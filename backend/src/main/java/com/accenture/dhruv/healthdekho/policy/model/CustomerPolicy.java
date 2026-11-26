package com.accenture.dhruv.healthdekho.policy.model;

import com.accenture.dhruv.healthdekho.product.model.InsuranceProduct;
import com.accenture.dhruv.healthdekho.product.model.PremiumFrequency;
import com.accenture.dhruv.healthdekho.customer.model.Customer;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Check;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "customer_policy",
        indexes = {
                @Index(name = "idx_customer_policy_customer_id", columnList = "customer_id"),
                @Index(name = "idx_customer_policy_status", columnList = "status")
        },
        uniqueConstraints = @UniqueConstraint(name = "uq_customer_policy_customer_product", columnNames = {"customer_id", "product_id"}))
@Check(constraints = "maturity_date >= start_date AND premium_amount > 0 AND sum_insured > 0 AND total_premium_payable > 0")
@Getter
@NoArgsConstructor
@Setter
public class CustomerPolicy {

    @Setter(AccessLevel.NONE)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "policy_id")
    private Long id;

    @Column(name = "policy_number", nullable = false, unique = true)
    private String policyNumber;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private InsuranceProduct product;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "maturity_date", nullable = false)
    private LocalDate maturityDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "premium_frequency", nullable = false, length = 32)
    private PremiumFrequency premiumFrequency;

    @Column(name = "premium_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal premiumAmount;

    @Column(name = "sum_insured", nullable = false, precision = 19, scale = 2)
    private BigDecimal sumInsured;

    @Column(name = "total_premium_payable", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalPremiumPayable;

    @Column(name = "status", nullable = false, length = 32)
    private String status;

    @Setter(AccessLevel.NONE)
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
