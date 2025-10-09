package com.swp.pizzashop.service.impl;

import com.swp.pizzashop.dto.CheckoutRequest;
import com.swp.pizzashop.dto.CheckoutResponse;
import com.swp.pizzashop.model.*;
import com.swp.pizzashop.repository.FoodRepository;
import com.swp.pizzashop.repository.OrderRepository;
import com.swp.pizzashop.service.OrderService;
import com.swp.pizzashop.service.UserService;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final FoodRepository foodRepository;
    private final OrderRepository orderRepository;
    private final UserService userService;

    @Override
    @Transactional
    public CheckoutResponse createCheckoutSession(CheckoutRequest request, String userEmail) {
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("No items provided");
        }
        var user = userService.findByEmail(userEmail);
        if (user == null) {
            throw new IllegalArgumentException("User not found: " + userEmail);
        }
        Order order = Order.builder()
                .user(user)
                .status(OrderStatus.PENDING_PAYMENT)
                .currency(request.getCurrency())
                .totalAmount(BigDecimal.ZERO)
                .build();
        List<SessionCreateParams.LineItem> stripeLineItems = new ArrayList<>();

        BigDecimal total = BigDecimal.ZERO;
        for (CheckoutRequest.Item item : request.getItems()) {
            var food = foodRepository.findById(item.getFoodId())
                    .orElseThrow(() -> new IllegalArgumentException("Food not found id=" + item.getFoodId()));
            BigDecimal qty = BigDecimal.valueOf(item.getQuantity());
            BigDecimal lineTotal = food.getBasePrice().multiply(qty);

            OrderItem orderItem = OrderItem.builder()
                    .foodId(food.getId())
                    .foodName(food.getName())
                    .unitPrice(food.getBasePrice())
                    .quantity(item.getQuantity())
                    .lineTotal(lineTotal)
                    .build();
            order.addItem(orderItem);
            total = total.add(lineTotal);

            // Stripe amount in cents (long)
            long unitAmountCents = food.getBasePrice().multiply(BigDecimal.valueOf(100)).longValue();

            SessionCreateParams.LineItem lineItem = SessionCreateParams.LineItem.builder()
                    .setQuantity((long) item.getQuantity())
                    .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                            .setCurrency(request.getCurrency())
                            .setUnitAmount(unitAmountCents)
                            .setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                    .setName(food.getName())
                                    .build())
                            .build())
                    .build();
            stripeLineItems.add(lineItem);
        }
        order.setTotalAmount(total);
        orderRepository.save(order);

        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setClientReferenceId(order.getId().toString()) // trace order in Stripe dashboard
                .putMetadata("orderId", order.getId().toString())
                .setSuccessUrl(request.getSuccessUrl() + "?session_id={CHECKOUT_SESSION_ID}")
                .setCancelUrl(request.getCancelUrl())
                .addAllLineItem(stripeLineItems)
                .build();
        try {
            Session session = Session.create(params);
            order.setStripeSessionId(session.getId());
            // Payment Intent may be null at creation time; will capture via webhook later
            orderRepository.save(order);
            return CheckoutResponse.builder()
                    .sessionId(session.getId())
                    .url(session.getUrl())
                    .status("CREATED")
                    .build();
        } catch (StripeException e) {
            log.error("Stripe session creation failed", e);
            throw new RuntimeException("Failed to create checkout session", e);
        }
    }

    @Override
    @Transactional
    public void handleCheckoutSessionCompleted(String sessionId, String paymentIntentId) {
        orderRepository.findByStripeSessionId(sessionId).ifPresent(order -> {
            order.setStatus(OrderStatus.PAID);
            order.setStripePaymentIntentId(paymentIntentId);
            orderRepository.save(order);
        });
    }

    @Override
    public Order findByStripeSessionId(String sessionId) {
        return orderRepository.findByStripeSessionId(sessionId).orElse(null);
    }
}
