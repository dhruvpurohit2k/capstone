package com.accenture.dhruv.healthdekho.policy.dto;

import com.accenture.dhruv.healthdekho.premium.model.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DueInstallmentResponse(
        Long installmentId,
        String policyNumber,
        int installmentNumber,
        LocalDate dueDate,
        LocalDate gracePeriodEndDate,
        PaymentStatus paymentStatus,
        boolean overdue,
        BigDecimal scheduledAmount,
        BigDecimal paidAmount,
        BigDecimal penaltyAmount,
        BigDecimal amountDue) {
}
