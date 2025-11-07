package com.swp.pizzashop.dto;

import com.swp.pizzashop.model.enums.OrderStatus;
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
public class OrderSummaryDTO {
    private Long id;
    private String customerName;
    private BigDecimal totalPrice;
    private String status;
    private String paymentStatus;
    private String paymentType;
    private LocalDateTime orderTime;
}
