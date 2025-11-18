package com.swp.pizzashop.controller;

import com.swp.pizzashop.dto.OrderDetailDTO;
import com.swp.pizzashop.model.Order;
import com.swp.pizzashop.model.OrderItem;
import com.swp.pizzashop.model.User;
import com.swp.pizzashop.repository.OrderItemRepository;
import com.swp.pizzashop.repository.UserRepository;
import com.swp.pizzashop.service.OrderCancellationService;
import com.swp.pizzashop.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Controller
@RequestMapping("/user")
@RequiredArgsConstructor
public class OrderCustomerController {

    private final OrderService orderService;
    private final UserRepository userRepository;
    private final OrderCancellationService orderCancellationService;
    private final OrderItemRepository orderItemRepository;

    @GetMapping("/orders")
    public String listCustomerOrders(
            Authentication authentication,
            RedirectAttributes ra,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            Model model
    ) {
        if (authentication == null || !authentication.isAuthenticated()) {
            ra.addFlashAttribute("error", "Please log in to view your orders");
            return "redirect:/login";
        }
        String email = authentication.getName();
        User user = userRepository.findByEmail(email);
        if (user == null) {
            ra.addFlashAttribute("error", "User not found. Please log in");
            return "redirect:/login";
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<OrderDetailDTO> ordersPage = orderService.getOrdersByCustomer(user.getId(), pageable);


        model.addAttribute("user", user);
        model.addAttribute("ordersPage", ordersPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", ordersPage.getTotalPages());

        return "orders";
    }

    @GetMapping("/orders/{orderId}")
    public String viewOrderDetails(@PathVariable Long orderId, Authentication authentication, Model model, RedirectAttributes ra) {
        // ensure user is authenticated
        if (authentication == null || !authentication.isAuthenticated()) {
            ra.addFlashAttribute("error", "Please login to view order details");
            return "redirect:/login";
        }
        String email = authentication.getName();
        User user = userRepository.findByEmail(email);
        if (user == null) {
            ra.addFlashAttribute("error", "User not found. Please log in");
            return "redirect:/login";
        }

        Order order = orderService.getOrderById(orderId);
        if (order == null) {
            ra.addFlashAttribute("err", "Order id = " + orderId + " not found");
            return "redirect:/user/orders";
        }

        // Authorization: only allow the owner of the order to view details
        if (order.getUser() == null || !order.getUser().getId().equals(user.getId())) {
            ra.addFlashAttribute("error", "This order does not belong to you");
            return "redirect:/user/orders";
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

        return "order-details";
    }

    @PostMapping("/orders/{orderId}/cancel")
    public String cancelOrder(@PathVariable Long orderId, Authentication authentication, RedirectAttributes ra) {
        // ensure user is authenticated
        if (authentication == null || !authentication.isAuthenticated()) {
            ra.addFlashAttribute("error", "Please login to view order details");
            return "redirect:/login";
        }
        String email = authentication.getName();
        User user = userRepository.findByEmail(email);
        if (user == null) {
            ra.addFlashAttribute("error", "User not found. Please log in");
            return "redirect:/login";
        }

        Order order = orderService.getOrderById(orderId);
        if (order == null) {
            ra.addFlashAttribute("err", "Order id = " + orderId + " not found");
            return "redirect:/user/orders";
        }

        // Authorization: only allow the owner of the order to cancel
        if (order.getUser() == null || !order.getUser().getId().equals(user.getId())) {
            ra.addFlashAttribute("error", "This order does not belong to you");
            return "redirect:/user/orders";
        }
        orderCancellationService.cancelOrderByCustomer(orderId);
        return "redirect:/user/orders";
    }
}
