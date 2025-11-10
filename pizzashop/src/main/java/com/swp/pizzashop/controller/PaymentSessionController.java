package com.swp.pizzashop.controller;

import com.swp.pizzashop.dto.CartItemDto;
import com.swp.pizzashop.dto.CreateSessionRequest;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/payment")
public class PaymentSessionController {

    @Value("${stripe.success-url}")
    private String successUrl;

    @Value("${stripe.cancel-url}")
    private String cancelUrl;

    @PostMapping(value = "/create-session", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> createSession(
            @RequestBody CreateSessionRequest req,
            @AuthenticationPrincipal UserDetails user) throws StripeException {

        log.info("[StripeSession] Request received: orderRef={}, items={}",
                req != null ? req.getOrderRef() : null,
                (req != null && req.getCart()!=null) ? req.getCart().size() : 0);

        if (req == null || req.getCart() == null || req.getCart().isEmpty()) {
            log.warn("[StripeSession] Empty cart");
            return ResponseEntity.badRequest().body(Map.of("error", "Cart is empty"));
        }

        SessionCreateParams.Builder builder = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(successUrl)
                .setCancelUrl(cancelUrl)
                .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD);

        int added = 0;
        for (CartItemDto item : req.getCart()) {
            if (item == null || !StringUtils.hasText(item.getName()) || item.getQuantity() == null || item.getQuantity() < 1) {
                continue;
            }
            String currency = StringUtils.hasText(item.getCurrency()) ? item.getCurrency().toLowerCase(Locale.ROOT) : "usd";
            BigDecimal price = item.getUnitPrice() != null ? item.getUnitPrice() : BigDecimal.ZERO;
            long unitAmount = ("jpy".equals(currency) || "vnd".equals(currency))
                    ? price.setScale(0, RoundingMode.HALF_UP).longValueExact()
                    : price.movePointRight(2).setScale(0, RoundingMode.HALF_UP).longValueExact();

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
            added++;
        }

        if (added == 0) {
            log.warn("[StripeSession] No valid line items built");
            return ResponseEntity.badRequest().body(Map.of("error", "No valid items"));
        }

        Map<String, String> metadata = new HashMap<>();
        if (req.getOrderRef() != null && !req.getOrderRef().isBlank()) metadata.put("orderRef", req.getOrderRef());
        if (user != null && StringUtils.hasText(user.getUsername())) metadata.put("user", user.getUsername());
        if (!metadata.isEmpty()) builder.putAllMetadata(metadata);

        Session session = Session.create(builder.build());
        log.info("[StripeSession] Created session id={} for orderRef={} items={} user={}",
                session.getId(), req.getOrderRef(), added, user != null ? user.getUsername() : null);
        return ResponseEntity.ok(Map.of("id", session.getId()));
    }
}


