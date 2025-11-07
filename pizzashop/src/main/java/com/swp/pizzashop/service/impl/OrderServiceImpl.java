package com.swp.pizzashop.service.impl;

import com.swp.pizzashop.dto.OrderSummaryDTO;
import com.swp.pizzashop.model.Order;
import com.swp.pizzashop.repository.OrderRepository;
import com.swp.pizzashop.repository.PaymentRepository;
import com.swp.pizzashop.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
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


}
