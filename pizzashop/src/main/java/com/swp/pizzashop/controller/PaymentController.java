package com.swp.pizzashop.controller;

import com.swp.pizzashop.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;

@Controller
@RequiredArgsConstructor
@RequestMapping("/payment")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/confirm")
    public String confirm(@RequestParam(name = "orderRef", required = false) String orderRef,
                          Authentication authentication,
                          RedirectAttributes ra) {
        PaymentService.Result result = paymentService.confirmPayment(authentication, orderRef);
        if (!result.success()) {
            ra.addFlashAttribute("error", result.message());
            return "redirect:/checkout/pay";
        }
        String encodedRef = UriUtils.encode(result.orderRef(), StandardCharsets.UTF_8);
        return "redirect:/payment/success?orderRef=" + encodedRef;
    }

    @GetMapping("/stripe-success")
    public String stripeSuccess(@RequestParam("session_id") String sessionId,
                                Authentication authentication,
                                RedirectAttributes ra) {
        PaymentService.Result result = paymentService.confirmStripeSession(authentication, sessionId);
        if (!result.success()) {
            ra.addFlashAttribute("error", result.message());
            return "redirect:/checkout/pay";
        }
        String encodedRef = UriUtils.encode(result.orderRef(), StandardCharsets.UTF_8);
        return "redirect:/payment/success?orderRef=" + encodedRef;
    }

    @GetMapping("/stripe-cancel")
    public String stripeCancel(RedirectAttributes ra) {
        ra.addFlashAttribute("error", "Payment was cancelled.");
        return "redirect:/checkout/pay";
    }

    @GetMapping("/success")
    public String success(@RequestParam(value = "orderRef", required = false) String orderRef, Model model) {
        if (orderRef == null || orderRef.isBlank()) {
            orderRef = "UNKNOWN";
        }
        model.addAttribute("orderRef", orderRef);
        return "payment-success";
    }
}
