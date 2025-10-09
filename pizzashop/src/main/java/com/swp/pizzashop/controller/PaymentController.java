package com.swp.pizzashop.controller;

import com.swp.pizzashop.model.Order;
import com.swp.pizzashop.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class PaymentController {

    private final OrderService orderService;

    @GetMapping("/payment/success")
    public String success(@RequestParam(name = "session_id", required = false) String sessionId, Model model) {
        Order order = null;
        if (sessionId != null) {
            order = orderService.findByStripeSessionId(sessionId);
        }
        model.addAttribute("sessionId", sessionId);
        model.addAttribute("order", order);
        return "payment-success";
    }

    @GetMapping("/payment/cancel")
    public String cancel() {
        return "payment-cancel";
    }
}

