package com.swp.pizzashop.service.impl;

import com.swp.pizzashop.model.*;
import com.swp.pizzashop.repository.*;
import com.swp.pizzashop.service.AddressService;
import com.swp.pizzashop.service.CartService;
import com.swp.pizzashop.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final UserRepository userRepository;
    private final CartService cartService;
    private final AddressService addressService;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final PaymentRepository paymentRepository;
    private final CartItemRepository cartItemRepository;

    @Override
    @Transactional
    public Result confirmPayment(Authentication authentication, String orderRef) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return Result.fail("Not authenticated");
        }
        String email = authentication.getName();
        User user = userRepository.findByEmail(email);
        if (user == null) {
            return Result.fail("User not found");
        }
        Cart cart = cartService.getOrCreateActiveCart(user.getId());
        if (cart == null || cart.getItems() == null || cart.getItems().isEmpty()) {
            return Result.fail("Cart is empty");
        }

        BigDecimal total = cart.getItems().stream()
                .map(i -> i.getPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Order newOrder = Order.builder()
                .user(user)
                .orderTime(LocalDateTime.now())
                .status("PAID")
                .totalPrice(total)
                .paymentMethod("CARD")
                .deliveryAddress(addressService.findDefaultByUser(user))
                .build();
        final Order savedOrder = orderRepository.save(newOrder);

        List<OrderItem> orderItems = cart.getItems().stream().map(ci -> {
            OrderItem oi = new OrderItem();
            oi.setOrder(savedOrder);
            oi.setFood(ci.getFood());
            oi.setSizeId(ci.getSizeId());
            oi.setQuantity(ci.getQuantity());
            oi.setPrice(ci.getPrice());
            oi.setNotes(ci.getNotes());
            return oi;
        }).collect(Collectors.toList());
        orderItemRepository.saveAll(orderItems);

        String txnRef = Optional.ofNullable(orderRef)
                .filter(s -> !s.isBlank())
                .orElse(UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase());

        Payment payment = Payment.builder()
                .order(savedOrder)
                .paymentType("CARD")
                .amount(total)
                .status("SUCCESS")
                .transactionId(txnRef)
                .paidAt(LocalDateTime.now())
                .build();
        paymentRepository.save(payment);

        if (cart.getItems() != null && !cart.getItems().isEmpty()) {
            cartItemRepository.deleteAll(cart.getItems());
            cart.getItems().clear();
        }
        return Result.ok(savedOrder.getId(), txnRef);
    }
}

