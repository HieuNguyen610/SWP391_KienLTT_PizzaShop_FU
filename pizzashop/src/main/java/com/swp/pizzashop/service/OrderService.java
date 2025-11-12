package com.swp.pizzashop.service;


import com.swp.pizzashop.dto.OrderDetailDTO;
import com.swp.pizzashop.dto.OrderSummaryDTO;
import com.swp.pizzashop.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface OrderService {
    long countAll();
    BigDecimal totalRevenue();

    Page<OrderSummaryDTO> findAllOrderSummaries(Pageable pageable);
    Page<OrderSummaryDTO> getOrdersByStatus(String status, Pageable pageable);
    Page<OrderSummaryDTO> searchOrders(String keyword, Pageable pageable);
    List<Map<String, Object>> getMonthlySalesSummary();
    Page<OrderDetailDTO> getOrdersByCustomer(Long userId, Pageable pageable);
    Page<Order> searchTodayOrders(String keyword, String paymentMethod, int page, int size);
    Page<OrderSummaryDTO> searchOrdersByDate(String payment, String keyword, LocalDate date, Pageable pageable);
    List<Map<String, Object>> getHourlySummary(LocalDate date);
    void updateStatus(Long orderId, String newStatus);

    void cancelOrder(Long orderId);

    Order getOrderById(Long orderId);

}