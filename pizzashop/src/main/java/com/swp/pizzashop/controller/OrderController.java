package com.swp.pizzashop.controller;

import com.swp.pizzashop.dto.CheckoutRequest;
import com.swp.pizzashop.dto.CheckoutResponse;
import com.swp.pizzashop.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/checkout")
    public ResponseEntity<CheckoutResponse> checkout(@Valid @RequestBody CheckoutRequest request, Authentication authentication) {
        String email = authentication.getName();
        CheckoutResponse response = orderService.createCheckoutSession(request, email);
        return ResponseEntity.ok(response);
    }
}

