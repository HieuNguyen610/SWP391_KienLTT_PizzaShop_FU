package com.swp.pizzashop.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class OrderDetailDTO {
    private Long id;
    private String username;
    private LocalDateTime orderTime;
    private String status;
    private BigDecimal totalPrice;
    private String paymentMethod;
    private String deliveryAddress;
    private String phone;
    private Long discountId;
    private LocalDateTime createdAt;
}
