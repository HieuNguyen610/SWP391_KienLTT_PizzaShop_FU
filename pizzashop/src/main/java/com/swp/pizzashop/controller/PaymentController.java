package com.swp.pizzashop.controller;

import com.swp.pizzashop.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
}

