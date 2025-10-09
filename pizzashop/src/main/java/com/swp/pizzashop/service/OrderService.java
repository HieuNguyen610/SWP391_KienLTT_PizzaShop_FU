package com.swp.pizzashop.service;

import com.swp.pizzashop.dto.CheckoutRequest;
import com.swp.pizzashop.dto.CheckoutResponse;
import com.swp.pizzashop.model.Order;

public interface OrderService {
    CheckoutResponse createCheckoutSession(CheckoutRequest request, String userEmail);
    void handleCheckoutSessionCompleted(String sessionId, String paymentIntentId);
    Order findByStripeSessionId(String sessionId);
}

