package com.swp.pizzashop.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CartItemDto {
    private String name;
    private BigDecimal unitPrice; // major units, e.g., 10.50
    private Integer quantity;
    private String currency = "usd"; // e.g., "usd" or "vnd"
}

