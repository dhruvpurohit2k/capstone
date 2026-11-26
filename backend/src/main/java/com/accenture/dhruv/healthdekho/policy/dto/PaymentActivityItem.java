package com.accenture.dhruv.healthdekho.policy.dto;

import com.accenture.dhruv.healthdekho.premium.dto.PaymentHistoryItem;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record PaymentActivityItem(Type type, PaymentHistoryItem payment, DueInstallmentResponse installment) {

    public enum Type {
        PAYMENT,
        OPEN_INSTALLMENT
    }
}
