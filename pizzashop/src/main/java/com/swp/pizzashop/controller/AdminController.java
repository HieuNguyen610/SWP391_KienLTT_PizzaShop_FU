package com.swp.pizzashop.controller;

import com.swp.pizzashop.service.FoodCategoryService;
import com.swp.pizzashop.service.UserService;
import com.swp.pizzashop.service.OrderService;
import com.swp.pizzashop.dto.CategorySummary;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminController {

    private final FoodCategoryService categoryService;
    private final UserService userService;
    private final OrderService orderService;

    @GetMapping
    public String dashboard(Model model,
                            @RequestParam(value = "msg", required = false) String msg,
                            @RequestParam(value = "err", required = false) String err) {
        if (msg != null) model.addAttribute("success", msg);
        if (err != null) model.addAttribute("error", err);
        model.addAttribute("activeSection", "dashboard");

        long totalUsers = userService.countByIsDeletedFalse();
        long activeUsers = userService.countByStatusAndIsDeletedFalse("ACTIVE");
        long totalOrders = 0L;
        BigDecimal totalRevenue = BigDecimal.ZERO;
        try {
            totalOrders = orderService.countAll();
            totalRevenue = orderService.totalRevenue();
        } catch (Exception ex) {
            log.warn("Order stats unavailable: {}", ex.getMessage());
        }
        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("activeUsers", activeUsers);
        model.addAttribute("totalOrders", totalOrders);
        model.addAttribute("totalRevenue", totalRevenue);

        List<CategorySummary> catSums = categoryService.getCategorySummaries();
        model.addAttribute("categorySummaries", catSums);

        return "admin/dashboard";
    }
}
