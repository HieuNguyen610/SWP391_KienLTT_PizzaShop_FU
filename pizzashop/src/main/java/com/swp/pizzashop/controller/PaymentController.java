package com.swp.pizzashop.controller;

import com.swp.pizzashop.dto.CartItemDto;
import com.swp.pizzashop.dto.CreateSessionRequest;
import com.swp.pizzashop.service.PaymentService;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/payment")
public class PaymentController {

    private final PaymentService paymentService;

    @Value("${app.base-url}")
    private String baseUrl;

    @Value("${stripe.publishable-key}")
    private String publishableKey;

    @Value("${stripe.success-url}")
    private String successUrl;

    @Value("${stripe.cancel-url}")
    private String cancelUrl;

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
            // Still render success, but with a placeholder
            orderRef = "UNKNOWN";
        }
        model.addAttribute("orderRef", orderRef);
        return "payment-success";
    }

    @PostMapping(value = "/create-session", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> createCheckoutSession(
            @RequestBody CreateSessionRequest req,
            @AuthenticationPrincipal UserDetails user
    ) throws StripeException {

        if (req == null || req.getCart() == null || req.getCart().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Cart is empty"));
        }

        // Build line items from the real cart
        SessionCreateParams.Builder builder = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(successUrl)
                .setCancelUrl(cancelUrl)
                .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD);

        for (CartItemDto item : req.getCart()) {
            if (item == null || !StringUtils.hasText(item.getName()) || item.getQuantity() == null || item.getQuantity() < 1) {
                continue;
            }
            String currency = StringUtils.hasText(item.getCurrency()) ? item.getCurrency().toLowerCase(Locale.ROOT) : "usd";
            BigDecimal price = item.getUnitPrice() != null ? item.getUnitPrice() : BigDecimal.ZERO;

            // Convert to smallest currency unit (Stripe expects long)
            long unitAmount;
            if ("jpy".equals(currency) || "vnd".equals(currency)) {
                unitAmount = price.setScale(0, RoundingMode.HALF_UP).longValueExact();
            } else {
                unitAmount = price.movePointRight(2).setScale(0, RoundingMode.HALF_UP).longValueExact();
            }

            SessionCreateParams.LineItem.PriceData.ProductData product =
                    SessionCreateParams.LineItem.PriceData.ProductData.builder()
                            .setName(item.getName())
                            .build();

            SessionCreateParams.LineItem.PriceData priceData =
                    SessionCreateParams.LineItem.PriceData.builder()
                            .setCurrency(currency)
                            .setUnitAmount(unitAmount)
                            .setProductData(product)
                            .build();

            SessionCreateParams.LineItem lineItem =
                    SessionCreateParams.LineItem.builder()
                            .setQuantity(item.getQuantity().longValue())
                            .setPriceData(priceData)
                            .build();

            builder.addLineItem(lineItem);
        }

        // Optional: attach metadata (e.g., order reference, user)
        Map<String, String> metadata = new HashMap<>();
        if (StringUtils.hasText(req.getOrderRef())) metadata.put("orderRef", req.getOrderRef());
        if (user != null && StringUtils.hasText(user.getUsername())) metadata.put("user", user.getUsername());
        if (!metadata.isEmpty()) {
            builder.putAllMetadata(metadata);
        }

        Session session = Session.create(builder.build());
        return ResponseEntity.ok(Map.of("id", session.getId()));
    }
}
