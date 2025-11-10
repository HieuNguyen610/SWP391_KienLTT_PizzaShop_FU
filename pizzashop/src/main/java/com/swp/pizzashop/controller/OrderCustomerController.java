package com.swp.pizzashop.controller;

import com.swp.pizzashop.dto.OrderDetailDTO;
import com.swp.pizzashop.model.User;
import com.swp.pizzashop.repository.OrderRepository;
import com.swp.pizzashop.repository.UserRepository;
import com.swp.pizzashop.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/user")
@RequiredArgsConstructor
public class OrderCustomerController {

    private final OrderService orderService;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;

    @GetMapping("/orders")
    public String listCustomerOrders(
            Authentication authentication,
            RedirectAttributes ra,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            Model model
    ) {
        if (authentication == null || !authentication.isAuthenticated()) {
            ra.addFlashAttribute("error", "Vui lòng đăng nhập để xem đơn hàng");
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
}
