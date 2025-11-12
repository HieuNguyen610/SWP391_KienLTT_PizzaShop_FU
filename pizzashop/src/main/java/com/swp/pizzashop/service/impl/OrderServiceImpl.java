package com.swp.pizzashop.service.impl;

import com.swp.pizzashop.dto.OrderDetailDTO;
import com.swp.pizzashop.dto.OrderSummaryDTO;
import com.swp.pizzashop.model.Order;
import com.swp.pizzashop.repository.OrderRepository;
import com.swp.pizzashop.repository.PaymentRepository;
import com.swp.pizzashop.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Year;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;

    @Override
    public long countAll() {
        return orderRepository.countActive();
    }

    @Override
    public BigDecimal totalRevenue() {
        return orderRepository.sumTotalPriceActive();
    }

    @Override
    public Page<OrderSummaryDTO> findAllOrderSummaries(Pageable pageable) {
        return orderRepository.findAllOrderSummaries(pageable);
    }

    @Override
    public Page<OrderSummaryDTO> getOrdersByStatus(String status, Pageable pageable) {
        return orderRepository.findByStatus(status, pageable);
    }

    @Override
    public Page<OrderSummaryDTO> searchOrders(String keyword, Pageable pageable) {
        return orderRepository.searchByCustomerName(keyword, pageable);
    }

    @Override
    public List<Map<String, Object>> getMonthlySalesSummary() {
        int currentYear = Year.now().getValue();
        List<Map<String, Object>> rawData = orderRepository.findMonthlySalesSummary(currentYear);

        // Convert danh sách thành map theo tháng
        Map<Integer, Map<String, Object>> dataMap = new HashMap<>();
        for (Map<String, Object> row : rawData) {
            Integer month = ((Number) row.get("month")).intValue();
            dataMap.put(month, row);
        }

        // Tạo danh sách đủ 12 tháng
        List<Map<String, Object>> fullYearData = new ArrayList<>();
        for (int m = 1; m <= 12; m++) {
            Map<String, Object> item = new HashMap<>();
            item.put("month", String.format("%02d", m));
            item.put("completedTotal",
                    dataMap.get(m) != null ? dataMap.get(m).get("completedTotal") : 0);
            item.put("cancelledTotal",
                    dataMap.get(m) != null ? dataMap.get(m).get("cancelledTotal") : 0);
            fullYearData.add(item);
        }

        return fullYearData;
    }

    @Override
    public Page<OrderDetailDTO> getOrdersByCustomer(Long userId, Pageable pageable) {
        return orderRepository.findAllOrderByUserId(userId, pageable);
    }

    @Override
    public Page<Order> searchTodayOrders(String keyword, String paymentMethod, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        if (keyword != null && !keyword.isEmpty()) {
            try {
                Long id = Long.parseLong(keyword);
                List<Order> list = orderRepository.findTodayOrdersById(id);
                return new PageImpl<>(list, pageable, list.size());
            } catch (NumberFormatException e) {
                return Page.empty(pageable);
            }
        }

        if (paymentMethod != null && !paymentMethod.isEmpty()) {
            return orderRepository.findTodayOrdersByPayment(paymentMethod, pageable);
        }

        return orderRepository.findTodayOrders(pageable);
    }

    @Override
    public Page<OrderSummaryDTO> searchOrdersByDate(
            String payment,
            String keyword,
            LocalDate date,
            String status,
            Pageable pageable
    ) {

        // Nếu search bằng ID
        if (keyword != null && !keyword.isBlank()) {
            try {
                Long id = Long.parseLong(keyword);
                List<OrderSummaryDTO> result =
                        orderRepository.findOrdersByIdAndDateAndStatus(id, date, status);
                return new PageImpl<>(result, pageable, result.size());
            } catch (NumberFormatException e) {
                return Page.empty(pageable);
            }
        }

        // Nếu lọc Payment
        if (payment != null && !payment.isBlank()) {
            return orderRepository.findOrdersByPaymentDateStatus(payment, date, status, pageable);
        }

        // Lọc theo ngày + status
        return orderRepository.findOrdersByDateAndStatus(date, status, pageable);
    }


    @Override
    public List<Map<String, Object>> getHourlySummary(LocalDate date) {
        List<Map<String, Object>> raw = orderRepository.findHourlySummary(date);

        Map<Integer, Map<String, Object>> map = new HashMap<>();
        for (Map<String, Object> row : raw) {
            Integer hour = ((Number) row.get("hour")).intValue();
            map.put(hour, row);
        }

        List<Map<String, Object>> full = new ArrayList<>();
        for (int h = 0; h < 24; h++) {
            Map<String, Object> item = new HashMap<>();
            item.put("hour", String.format("%02d:00", h));
            item.put("completed", map.get(h) != null ? map.get(h).get("completed") : 0);
            item.put("cancelled", map.get(h) != null ? map.get(h).get("cancelled") : 0);
            full.add(item);
        }

        return full;
    }

    @Override
    public void updateStatus(Long orderId, String newStatus) {
        Order order = getOrderById(orderId);
        String current = order.getStatus();


        if (newStatus.equals("CANCELLED") && !current.equals("PAID")) {
            throw new IllegalStateException("Order can only be cancelled at PAID stage.");
        }

        boolean valid =
                (current.equals("PAID") && newStatus.equals("COOKING")) ||
                        (current.equals("COOKING") && newStatus.equals("DELIVERING")) ||
                        (current.equals("DELIVERING") && newStatus.equals("COMPLETED")) ||
                        (newStatus.equals("CANCELLED"));

        if (!valid) {
            throw new IllegalStateException("Invalid status transition: " + current + " → " + newStatus);
        }

        // ✔ set status + save DB
        order.setStatus(newStatus);
        orderRepository.save(order);
    }

    @Override
    public void cancelOrder(Long orderId) {
        updateStatus(orderId, "CANCELLED");
    }

    @Override
    public Order getOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
    }


}
