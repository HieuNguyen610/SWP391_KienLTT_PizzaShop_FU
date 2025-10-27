package com.swp.pizzashop.service;

import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.Authentication;

public interface PaymentService {
    PaymentService.Result confirmPayment(Authentication authentication, String orderRef);

        record Result(boolean success, String message, Long orderId, String orderRef) {

        public static Result ok(Long orderId, String orderRef) {
            return new Result(true, null, orderId, orderRef);
        }

        public static Result fail(String message) {
            return new Result(false, message, null, null);
        }
        }
}

