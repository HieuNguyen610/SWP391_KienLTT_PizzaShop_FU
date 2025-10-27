package com.swp.pizzashop.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class PaymentViewController {

    @GetMapping("/payment/success")
    public String success(@RequestParam(value = "orderRef", required = false) String orderRef, Model model) {
        if (orderRef == null || orderRef.isBlank()) {
            // Still render success, but with a placeholder
            orderRef = "UNKNOWN";
        }
        model.addAttribute("orderRef", orderRef);
        return "payment-success";
    }
}
