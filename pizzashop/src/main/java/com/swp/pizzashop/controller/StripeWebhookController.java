package com.swp.pizzashop.controller;

import com.swp.pizzashop.model.Payment;
import com.swp.pizzashop.model.Refund;
import com.swp.pizzashop.repository.PaymentRepository;
import com.swp.pizzashop.repository.RefundRepository;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Charge;
import com.stripe.model.Event;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/webhook/stripe")
public class StripeWebhookController {

    private final RefundRepository refundRepository;
    private final PaymentRepository paymentRepository;

    @Value("${stripe.webhook-secret}")
    private String webhookSecret;

    @PostMapping
    public ResponseEntity<String> handle(@RequestHeader(name = "Stripe-Signature", required = false) String sigHeader,
                                         @RequestBody String payload) {
        if (webhookSecret == null || webhookSecret.isBlank()) {
            log.warn("Stripe webhook secret is not configured; rejecting webhook");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("webhook not configured");
        }

        Event event;
        try {
            event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
        } catch (SignatureVerificationException e) {
            log.warn("Stripe webhook signature verification failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("invalid signature");
        } catch (Exception e) {
            log.error("Failed to parse stripe webhook: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("invalid payload");
        }

        String type = event.getType();
        log.info("Received stripe event: {}", type);

        try {
            switch (type) {
                case "refund.created":
                case "refund.updated":
                    handleRefundEvent(event);
                    break;
                case "charge.refunded":
                    handleChargeRefunded(event);
                    break;
                default:
                    // ignore other events
                    log.debug("Unhandled stripe event type: {}", type);
            }
        } catch (Exception ex) {
            log.error("Error handling stripe webhook {}: {}", type, ex.getMessage(), ex);
            return ResponseEntity.ok("ignored");
        }

        return ResponseEntity.ok("ok");
    }

    private void handleRefundEvent(Event event) {
        Object obj = event.getDataObjectDeserializer().getObject().orElse(null);
        if (obj == null) {
            log.warn("Stripe refund event has no payload object");
            return;
        }

        com.stripe.model.Refund stripeRefund;
        try {
            stripeRefund = (com.stripe.model.Refund) obj;
        } catch (ClassCastException e) {
            log.warn("Stripe event data is not a Refund instance: {}", obj.getClass());
            return;
        }

        String refundId = stripeRefund.getId();
        String status = stripeRefund.getStatus(); // 'succeeded', 'failed', 'pending', etc.

        Optional<Refund> maybe = refundRepository.findByProviderRefundId(refundId);
        if (maybe.isPresent()) {
            Refund r = maybe.get();
            r.setProviderRefundId(refundId);
            r.setStatus(translateStripeRefundStatus(status));
            r.setProcessedAt(LocalDateTime.now());
            refundRepository.save(r);

            // Update payment record if available
            String chargeId = stripeRefund.getCharge();
            String paymentIntentId = stripeRefund.getPaymentIntent();
            String txn = paymentIntentId != null ? paymentIntentId : chargeId;
            if (txn != null) {
                Optional<Payment> pOpt = paymentRepository.findByTransactionId(txn);
                if (pOpt.isPresent()) {
                    Payment p = pOpt.get();
                    if ("succeeded".equalsIgnoreCase(status)) {
                        p.setRefundStatus("REFUNDED");
                        p.setRefundedAt(LocalDateTime.now());
                        // use our local refund amount if present
                        p.setRefundedAmount(r.getAmount());
                    } else if ("failed".equalsIgnoreCase(status)) {
                        p.setRefundStatus("FAILED");
                    } else {
                        p.setRefundStatus("PROCESSING");
                    }
                    paymentRepository.save(p);
                }
            }
            log.info("Mapped stripe refund {} -> local refund {} (status={})", refundId, r.getId(), r.getStatus());
        } else {
            // No local refund record found; create one for tracking
            Refund created = Refund.builder()
                    .order(null)
                    .payment(null)
                    .amount(null)
                    .currency(stripeRefund.getCurrency())
                    .status(translateStripeRefundStatus(status))
                    .providerRefundId(refundId)
                    .idempotencyKey(null)
                    .build();
            // We cannot set order/payment without mapping; save minimal record for auditing
            refundRepository.save(created);
            log.info("Created audit refund record for stripe refund {} status={}", refundId, status);
        }
    }

    private void handleChargeRefunded(Event event) {
        Object obj = event.getDataObjectDeserializer().getObject().orElse(null);
        if (obj == null) {
            log.warn("Stripe charge.refunded event has no data object");
            return;
        }

        Charge charge;
        try {
            charge = (Charge) obj;
        } catch (ClassCastException e) {
            log.warn("Stripe event data is not a Charge instance: {}", obj.getClass());
            return;
        }

        // Charge contains a list of refunds; update local refunds for each
        if (charge.getRefunds() != null && charge.getRefunds().getData() != null) {
            for (Object rf : charge.getRefunds().getData()) {
                if (rf instanceof com.stripe.model.Refund) {
                    com.stripe.model.Refund stripeRefund = (com.stripe.model.Refund) rf;
                    Optional<Refund> maybe = refundRepository.findByProviderRefundId(stripeRefund.getId());
                    if (maybe.isPresent()) {
                        Refund r = maybe.get();
                        r.setStatus(translateStripeRefundStatus(stripeRefund.getStatus()));
                        r.setProcessedAt(LocalDateTime.now());
                        refundRepository.save(r);
                        // update payment similarly
                        Optional<Payment> pOpt = paymentRepository.findByTransactionId(charge.getPaymentIntent() != null ? charge.getPaymentIntent() : charge.getId());
                        if (pOpt.isPresent()) {
                            Payment p = pOpt.get();
                            p.setRefundStatus("REFUNDED");
                            p.setRefundedAt(LocalDateTime.now());
                            p.setRefundedAmount(r.getAmount());
                            paymentRepository.save(p);
                        }
                    }
                }
            }
        }
    }

    private String translateStripeRefundStatus(String stripeStatus) {
        if (stripeStatus == null) return "PENDING";
        switch (stripeStatus.toLowerCase()) {
            case "succeeded":
                return "SUCCEEDED";
            case "failed":
                return "FAILED";
            case "pending":
            default:
                return "PROCESSING";
        }
    }
}
