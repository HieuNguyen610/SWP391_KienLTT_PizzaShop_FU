package com.swp.pizzashop.service;

import java.math.BigDecimal;

public interface OrderService {
    long countAll();
    BigDecimal totalRevenue();
}