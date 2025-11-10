package com.swp.pizzashop.service;

import org.springframework.security.core.Authentication;

public interface PaymentService {
    PaymentService.Result confirmPayment(Authentication authentication, String orderRef);

    // Called after Stripe redirects to success URL to finalize order & persist payment using the session id
    PaymentService.Result confirmStripeSession(Authentication authentication, String sessionId);

    record Result(boolean success, String message, Long orderId, String orderRef) {

        public static Result ok(Long orderId, String orderRef) {
            return new Result(true, null, orderId, orderRef);
        }

        public static Result fail(String message) {
            return new Result(false, message, null, null);
        }
    }
}
