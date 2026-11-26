package com.accenture.dhruv.healthdekho.premium.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PaymentHistoryItem(
        Long paymentId,
        Long installmentId,
        String policyNumber,
        int installmentNumber,
        String transactionReference,
        LocalDate paymentDate,
        BigDecimal amountPaid,
        BigDecimal penaltyAmount,
        String paymentMethod) {
}
