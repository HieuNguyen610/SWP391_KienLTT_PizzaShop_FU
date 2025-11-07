package com.swp.pizzashop.controller.admin;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.swp.pizzashop.dto.OrderSummaryDTO;
import com.swp.pizzashop.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.Year;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {
    private final OrderService orderService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @GetMapping
    public String listOrders(Model model,
                             @RequestParam(defaultValue = "0") int page,
                             @RequestParam(defaultValue = "5") int size,
                             @RequestParam(required = false) String status,
                             @RequestParam(required = false) String q,
                             @RequestParam(required = false) Integer year) {

        Pageable pageable = PageRequest.of(page, size);
        Page<OrderSummaryDTO> orderPage;

        if (status != null && !status.isBlank()) {
            orderPage = orderService.getOrdersByStatus(status, pageable);
        } else if (q != null && !q.isBlank()) {
            orderPage = orderService.searchOrders(q, pageable);
        } else {
            orderPage = orderService.findAllOrderSummaries(pageable);
        }

        model.addAttribute("orderPage", orderPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", orderPage.getTotalPages());
        model.addAttribute("statusFilter", status);
        model.addAttribute("query", q);

        int currentYear = (year != null) ? year : Year.now().getValue();
        List<Map<String, Object>> monthlyData = orderService.getMonthlySalesSummary();

// Format tháng
        for (Map<String, Object> item : monthlyData) {
            int monthNum = Integer.parseInt(item.get("month").toString());
            item.put("month", String.valueOf(monthNum));
        }

        try {
            String monthlyDataJson = objectMapper.writeValueAsString(monthlyData);
            model.addAttribute("monthlyDataJson", monthlyDataJson);
        } catch (JsonProcessingException e) {
            log.error("Lỗi khi chuyển dữ liệu sang JSON", e);
            model.addAttribute("monthlyDataJson", "[]");
        }

        model.addAttribute("year", currentYear);
        return "admin/orders";
    }
}
