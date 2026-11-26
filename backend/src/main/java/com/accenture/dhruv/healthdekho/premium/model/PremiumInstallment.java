package com.accenture.dhruv.healthdekho.premium.model;

import com.accenture.dhruv.healthdekho.policy.model.CustomerPolicy;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "premium_installment",
        uniqueConstraints = @UniqueConstraint(name = "uq_premium_installment_policy_number", columnNames = {"policy_id", "installment_number"}))
@Check(constraints = "installment_number > 0 AND grace_period_end_date >= due_date AND scheduled_amount > 0 "
        + "AND assessed_penalty_amount >= 0 AND paid_amount BETWEEN 0 AND scheduled_amount + assessed_penalty_amount")
@Getter
@NoArgsConstructor
@Setter
public class PremiumInstallment {

    @Setter(AccessLevel.NONE)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "installment_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "policy_id", nullable = false)
    private CustomerPolicy policy;

    @Column(name = "installment_number", nullable = false)
    private int installmentNumber;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "grace_period_end_date", nullable = false)
    private LocalDate gracePeriodEndDate;

    @Column(name = "scheduled_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal scheduledAmount;

    @Column(name = "assessed_penalty_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal assessedPenaltyAmount = BigDecimal.ZERO;

    @Column(name = "paid_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal paidAmount = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false, length = 32)
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;
}
