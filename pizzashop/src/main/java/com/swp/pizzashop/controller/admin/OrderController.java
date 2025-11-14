package com.swp.pizzashop.controller.admin;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.swp.pizzashop.dto.OrderSummaryDTO;
import com.swp.pizzashop.model.Order;
import com.swp.pizzashop.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.swp.pizzashop.model.OrderItem;
import com.swp.pizzashop.repository.OrderItemRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;

import java.time.LocalDate;
import java.time.Year;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {
    private final OrderService orderService;
    private final OrderItemRepository orderItemRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @GetMapping
    public String listOrders(Model model,
                             @RequestParam(defaultValue = "0") int page,
                             @RequestParam(defaultValue = "5") int size,
                             @RequestParam(required = false) String payment,
                             @RequestParam(required = false) String q,
                             @RequestParam(required = false) String date,
                             @RequestParam(required = false) Integer year,
                             @RequestParam(required = false) String status) {

        Pageable pageable = PageRequest.of(page, size);

        // Nếu người dùng không nhập ngày → mặc định hôm nay
        LocalDate targetDate;
        if (date == null || date.isBlank()) {
            targetDate = LocalDate.now();
        } else {
            targetDate = LocalDate.parse(date);
        }

        Page<OrderSummaryDTO> orderPage =
                orderService.searchOrdersByDate(payment, q, targetDate, status, pageable);

        // Gửi dữ liệu sang view
        model.addAttribute("orderPage", orderPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", orderPage.getTotalPages());
        model.addAttribute("paymentFilter", payment);
        model.addAttribute("query", q);
        model.addAttribute("dateFilter", date);  // giữ lại giá trị user chọn
        model.addAttribute("statusFilter", (status == null || status.isBlank()) ? "" : status);

        // Monthly chart giữ nguyên
        int currentYear = (year != null) ? year : Year.now().getValue();
        model.addAttribute("year", currentYear);

        List<Map<String, Object>> monthlyData = orderService.getMonthlySalesSummary();
        model.addAttribute("monthlyDataJson", monthlyData);

        List<Map<String, Object>> hourlyData = orderService.getHourlySummary(targetDate);
        try {
            String hourlyDataJson = objectMapper.writeValueAsString(hourlyData);
            model.addAttribute("hourlyDataJson", hourlyDataJson);
        } catch (JsonProcessingException e) {
            log.error("Lỗi khi chuyển hourlyData sang JSON", e);
            model.addAttribute("hourlyDataJson", "[]");
        }

        return "admin/orders";
    }

    @PostMapping("/{orderId}/update-status")
    public String updateOrderStatus(
            @PathVariable Long orderId,
            @RequestParam("newStatus") String newStatus,
            HttpServletRequest request
    ) {
        orderService.updateStatus(orderId, newStatus);
        return "redirect:/admin/orders" + buildQueryParams(request);
    }

    @PostMapping("/{orderId}/cancel")
    public String cancelOrder(@PathVariable Long orderId, HttpServletRequest request) {
        orderService.cancelOrder(orderId);
        return "redirect:/admin/orders" + buildQueryParams(request);
    }

    @GetMapping("/{orderId}")
    public String viewOrderDetails(@PathVariable Long orderId, Model model, RedirectAttributes ra) {
        Order order = orderService.getOrderById(orderId);
        if (order == null) {
            ra.addAttribute("err", "Order id = " + orderId + " not found");
            return "redirect:/admin/orders";
        }

        // Load order items explicitly (Order entity in this project doesn't expose items list)
        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);

        // Compute subtotal = sum(item.price * quantity)
        BigDecimal subtotal = items.stream()
                .map(it -> it.getPrice().multiply(BigDecimal.valueOf(it.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // VAT 10% (rounded to 2 decimals)
        BigDecimal vat = subtotal.multiply(BigDecimal.valueOf(0.1)).setScale(2, RoundingMode.HALF_UP);

        // If the order has stored totalPrice, use it; otherwise compute subtotal + vat
        BigDecimal total = order.getTotalPrice() != null ? order.getTotalPrice() : subtotal.add(vat);

        model.addAttribute("order", order);
        model.addAttribute("items", items);
        model.addAttribute("subtotal", subtotal);
        model.addAttribute("vat", vat);
        model.addAttribute("total", total);
        model.addAttribute("payments", order.getPayments());
        model.addAttribute("activeSection", "orders");

        return "admin/order-details";
    }




    private String buildQueryParams(HttpServletRequest request) {
        StringBuilder sb = new StringBuilder("?");

        String[] params = {"q", "payment", "date", "status", "page"};

        for (String p : params) {
            String value = request.getParameter(p);
            if (value != null && !value.isBlank()) {
                sb.append(p).append("=").append(value).append("&");
            }
        }

        return sb.toString();
    }
}
