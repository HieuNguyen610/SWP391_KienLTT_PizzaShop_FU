package com.swp.pizzashop.service.impl;

import com.swp.pizzashop.repository.OrderRepository;
import com.swp.pizzashop.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;

    @Override
    public long countAll() {
        return orderRepository.countActive();
    }

    @Override
    public BigDecimal totalRevenue() {
        return orderRepository.sumTotalPriceActive();
    }
}
