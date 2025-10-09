package com.swp.pizzashop.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckoutRequest {

    @NotEmpty
    private List<Item> items;

    private String currency = "usd"; // default

    private String successUrl = "http://localhost:8080/payment/success";
    private String cancelUrl = "http://localhost:8080/payment/cancel";

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Item {
        @NotNull
        private Long foodId;
        @NotNull
        @Min(1)
        private Integer quantity;
    }
}

