package com.swp.pizzashop.controller;

import com.swp.pizzashop.form.FoodForm;
import com.swp.pizzashop.model.Food;
import com.swp.pizzashop.service.FoodService;
import com.swp.pizzashop.service.FoodCategoryService;
import com.swp.pizzashop.service.UserService;
import com.swp.pizzashop.service.OrderService;
import com.swp.pizzashop.dto.CategorySummary;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminController {

    private final FoodService foodService;
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
        try {
            totalOrders = orderService.countAll();
        } catch (Exception ex) {
            log.warn("Order count unavailable: {}", ex.getMessage());
        }
        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("activeUsers", activeUsers);
        model.addAttribute("totalOrders", totalOrders);

        List<CategorySummary> catSums = categoryService.getCategorySummaries();
        model.addAttribute("categorySummaries", catSums);

        return "admin/dashboard";
    }

    @GetMapping("/foods")
    public String foodsPage(Model model) {
        model.addAttribute("activeSection", "foods");
        model.addAttribute("foodForm", new FoodForm());
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("foods", foodService.findAll());
        return "admin/foods";
    }

    @PostMapping("/foods")
    public String addFood(@Valid @ModelAttribute("foodForm") FoodForm form,
                          BindingResult result,
                          Model model,
                          RedirectAttributes ra) {
        if (result.hasErrors()) {
            model.addAttribute("activeSection", "foods");
            model.addAttribute("categories", categoryService.findAll());
            model.addAttribute("foods", foodService.findAll());
            return "admin/foods";
        }
        try {
            Food saved = foodService.createFood(form);
            ra.addAttribute("msg", "Food created: " + saved.getName());
            return "redirect:/admin/foods";
        } catch (Exception ex) {
            log.error("Create food failed", ex);
            result.reject("createFailed", ex.getMessage());
            model.addAttribute("activeSection", "foods");
            model.addAttribute("categories", categoryService.findAll());
            model.addAttribute("foods", foodService.findAll());
            return "admin/foods";
        }
    }

    @GetMapping("/foods/{id}/edit")
    public String editFood(@PathVariable Long id, Model model, RedirectAttributes ra) {
        List<Food> foods = foodService.findAll();
        Food target = foods.stream().filter(f -> f.getId().equals(id)).findFirst().orElse(null);
        if (target == null) {
            ra.addAttribute("err", "Food not found");
            return "redirect:/admin/foods";
        }
        FoodForm form = new FoodForm();
        form.setName(target.getName());
        form.setDescription(target.getDescription());
        form.setBasePrice(target.getBasePrice());
        form.setActive(target.isActive());
        form.setImageUrl(target.getImageUrl());
        form.setCategoryId(target.getCategory().getId());
        model.addAttribute("foodForm", form);
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("foodId", id);
        model.addAttribute("activeSection", "foods");
        return "admin/food-edit";
    }

    @PostMapping("/foods/{id}/edit")
    public String updateFood(@PathVariable Long id,
                             @Valid @ModelAttribute("foodForm") FoodForm form,
                             BindingResult result,
                             Model model,
                             RedirectAttributes ra) {
        if (result.hasErrors()) {
            model.addAttribute("categories", categoryService.findAll());
            model.addAttribute("foodId", id);
            model.addAttribute("activeSection", "foods");
            return "admin/food-edit";
        }
        try {
            foodService.updateFood(id, form);
            ra.addAttribute("msg", "Food updated");
            return "redirect:/admin/foods";
        } catch (Exception ex) {
            log.error("Update food failed", ex);
            result.reject("updateFailed", ex.getMessage());
            model.addAttribute("categories", categoryService.findAll());
            model.addAttribute("foodId", id);
            model.addAttribute("activeSection", "foods");
            return "admin/food-edit";
        }
    }
}
