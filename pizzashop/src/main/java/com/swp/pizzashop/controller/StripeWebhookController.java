package com.swp.pizzashop.controller;

import com.swp.pizzashop.service.OrderService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stripe")
@RequiredArgsConstructor
@Slf4j
public class StripeWebhookController {

    private final OrderService orderService;

    @Value("${stripe.webhook.secret:}")
    private String webhookSecret;

    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(@RequestBody String payload, @RequestHeader(name = "Stripe-Signature", required = false) String sigHeader) {
        if (webhookSecret == null || webhookSecret.isBlank()) {
            log.warn("Stripe webhook secret not configured; rejecting");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Webhook secret not configured");
        }
        if (sigHeader == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Missing signature header");
        }
        Event event;
        try {
            event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
        } catch (SignatureVerificationException e) {
            log.error("Invalid Stripe signature", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid signature");
        }

        try {
            switch (event.getType()) {
                case "checkout.session.completed" -> {
                    Session session = (Session) event.getDataObjectDeserializer().getObject().orElse(null);
                    if (session != null) {
                        String sessionId = session.getId();
                        String paymentIntentId = session.getPaymentIntent();
                        orderService.handleCheckoutSessionCompleted(sessionId, paymentIntentId);
                        log.info("Order marked as PAID for session {}", sessionId);
                    }
                }
                case "checkout.session.expired" -> {
                    // Could set order to CANCELED if desired
                    log.info("Checkout session expired: {}", event.getId());
                }
                default -> log.debug("Unhandled Stripe event type: {}", event.getType());
            }
            return ResponseEntity.ok("Received");
        } catch (Exception e) {
            log.error("Error processing Stripe webhook", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Webhook processing error");
        }
    }
}

