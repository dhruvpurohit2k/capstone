package com.accenture.dhruv.healthdekho.premium.service;

import com.accenture.dhruv.healthdekho.premium.dto.PaymentHistoryItem;
import com.accenture.dhruv.healthdekho.premium.repository.PremiumPaymentRepository;
import com.accenture.dhruv.healthdekho.seed.SeedCsv;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class PremiumPaymentService {

    private final PremiumPaymentRepository repository;

    public PremiumPaymentService(PremiumPaymentRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<PaymentHistoryItem> findHistory(long customerId) {
        return repository.findByCustomerId(customerId).stream()
                .map(payment -> {
                    var installment = payment.getInstallment();
                    return new PaymentHistoryItem(payment.getId(), installment.getId(),
                            installment.getPolicy().getPolicyNumber(), installment.getInstallmentNumber(),
                            payment.getTransactionReference(), payment.getPaymentDate(), payment.getAmountPaid(),
                            payment.getPenaltyAmount(), payment.getPaymentMethod());
                })
                .toList();
    }

    @Transactional
    public void seed() {
        List<Map<String, String>> rows = SeedCsv.read("premium_payment");
        repository.lockForSeeding();
        int inserted = 0;
        for (Map<String, String> row : rows) {
            inserted += repository.insertSeed(row);
        }
        if (inserted > 0) {
            repository.advanceIdentity();
        }
    }
}
