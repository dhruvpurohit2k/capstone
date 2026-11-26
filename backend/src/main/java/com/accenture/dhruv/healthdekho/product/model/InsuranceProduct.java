package com.accenture.dhruv.healthdekho.product.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Check;

import java.math.BigDecimal;

@Entity
@Table(name = "insurance_product",
        indexes = {
                @Index(name = "idx_insurance_product_active", columnList = "active")
        })
@Check(constraints = "premium_amount > 0 AND sum_insured > 0 AND term_months > 0 AND min_entry_age >= 0 AND max_entry_age >= min_entry_age")
@Getter
@NoArgsConstructor
@Setter
public class InsuranceProduct {

    @Setter(AccessLevel.NONE)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long id;

    @Column(name = "product_code", nullable = false, unique = true)
    private String productCode;

    @Column(name = "policy_name", nullable = false)
    private String policyName;

    @Column(name = "policy_type", nullable = false, length = 32)
    private String policyType;

    @Column(name = "term_months", nullable = false)
    private int termMonths;

    @Enumerated(EnumType.STRING)
    @Column(name = "premium_frequency", nullable = false, length = 32)
    private PremiumFrequency premiumFrequency;

    @Column(name = "premium_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal premiumAmount;

    @Column(name = "sum_insured", nullable = false, precision = 19, scale = 2)
    private BigDecimal sumInsured;

    @Column(name = "min_entry_age", nullable = false)
    private int minEntryAge;

    @Column(name = "max_entry_age", nullable = false)
    private int maxEntryAge;

    @Column(name = "active", nullable = false)
    private boolean active;

    public boolean acceptsEntryAge(int age) {
        return age >= minEntryAge && age <= maxEntryAge;
    }
}
