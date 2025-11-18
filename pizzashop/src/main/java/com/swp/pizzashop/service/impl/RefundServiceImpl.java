package com.swp.pizzashop.service.impl;

import com.swp.pizzashop.model.Payment;
import com.swp.pizzashop.model.Refund;
import com.swp.pizzashop.repository.PaymentRepository;
import com.swp.pizzashop.repository.RefundRepository;
import com.swp.pizzashop.service.RefundService;
import com.stripe.Stripe;
import com.stripe.net.RequestOptions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefundServiceImpl implements RefundService {

    private final RefundRepository refundRepository;
    private final PaymentRepository paymentRepository;

    @Value("${stripe.secret-key}")
    private String stripeSecretKey;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processRefund(Refund refund, Payment payment) {
        // Ensure Stripe key is set
        Stripe.apiKey = stripeSecretKey;

        // Mark refund processing
        refund.setStatus("PROCESSING");
        refundRepository.save(refund);

        try {
            Map<String, Object> params = new HashMap<>();

            String txn = payment.getTransactionId();
            if (txn == null || txn.isBlank()) {
                throw new IllegalStateException("Payment transaction id is missing for refund");
            }

            // Attach payment_intent or charge depending on transaction id prefix
            if (txn.startsWith("pi_") || txn.startsWith("pi-")) {
                params.put("payment_intent", txn);
            } else {
                params.put("charge", txn);
            }

            // Refund amount if specified (convert to cents)
            if (refund.getAmount() != null) {
                BigDecimal amount = refund.getAmount();
                long cents = amount.multiply(BigDecimal.valueOf(100)).longValue();
                params.put("amount", cents);
            }

            RequestOptions requestOptions = RequestOptions.builder()
                    .setIdempotencyKey(refund.getIdempotencyKey() != null ? refund.getIdempotencyKey() : "refund-" + refund.getId())
                    .build();

            // Call Stripe
            com.stripe.model.Refund stripeRefund = com.stripe.model.Refund.create(params, requestOptions);

            // Update refund record
            refund.setProviderRefundId(stripeRefund.getId());
            refund.setStatus("SUCCEEDED");
            refund.setProcessedAt(LocalDateTime.now());
            refundRepository.save(refund);

            // Update payment record
            payment.setRefundStatus("REFUNDED");
            payment.setRefundedAmount(refund.getAmount());
            payment.setRefundedAt(LocalDateTime.now());
            paymentRepository.save(payment);

            log.info("Refund succeeded for refundId={} stripeRefundId={}", refund.getId(), stripeRefund.getId());
        } catch (Exception ex) {
            log.error("Refund processing failed for refundId={}", refund.getId(), ex);
            refund.setStatus("FAILED");
            refund.setFailureReason(ex.getMessage());
            refundRepository.save(refund);
            throw new RuntimeException("Refund failed: " + ex.getMessage(), ex);
        }
    }
}

