package com.swp.pizzashop.controller;

import com.swp.pizzashop.form.CategoryForm;
import com.swp.pizzashop.form.FoodForm;
import com.swp.pizzashop.model.Food;
import com.swp.pizzashop.model.FoodCategory;
import com.swp.pizzashop.service.FoodService;
import com.swp.pizzashop.service.FoodCategoryService;
import com.swp.pizzashop.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminController {

    private final FoodService foodService;
    private final FoodCategoryService categoryService;
    private final UserService userService;

    @GetMapping
    public String dashboard(Model model,
                            @RequestParam(value = "msg", required = false) String msg,
                            @RequestParam(value = "err", required = false) String err) {
        if (msg != null) model.addAttribute("success", msg);
        if (err != null) model.addAttribute("error", err);
        model.addAttribute("activeSection", "dashboard");

        // KPIs
        long totalUsers = userService.countByIsDeletedFalse();
        long activeUsers = userService.countByStatusAndIsDeletedFalse("ACTIVE");
        long totalOrders = 0L;
        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("activeUsers", activeUsers);
        model.addAttribute("totalOrders", totalOrders);

        return "admin/dashboard";
    }

    @GetMapping("/categories")
    public String categoriesPage(Model model) {
        model.addAttribute("activeSection", "categories");
        model.addAttribute("categoryForm", new CategoryForm());
        model.addAttribute("categories", categoryService.findAll());
        return "admin/categories";
    }

    @GetMapping("/foods")
    public String foodsPage(Model model) {
        model.addAttribute("activeSection", "foods");
        model.addAttribute("foodForm", new FoodForm());
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("foods", foodService.findAll());
        return "admin/foods";
    }

    @PostMapping("/categories")
    public String addCategory(@Valid @ModelAttribute("categoryForm") CategoryForm form,
                              BindingResult result,
                              Model model,
                              RedirectAttributes ra) {
        if (categoryService.findByName(form.getName()) != null) {
            result.rejectValue("name", "duplicate", "Category already exists");
        }
        if (result.hasErrors()) {
            model.addAttribute("activeSection", "categories");
            model.addAttribute("categories", categoryService.findAll());
            return "admin/categories";
        }
        FoodCategory cat = new FoodCategory();
        cat.setName(form.getName().trim());
        categoryService.save(cat);
        ra.addAttribute("msg", "Category created");
        return "redirect:/admin/categories";
    }

    @PostMapping("/categories/{id}")
    public String renameCategory(@PathVariable Long id,
                                 @Valid @ModelAttribute("categoryForm") CategoryForm form,
                                 BindingResult result,
                                 RedirectAttributes ra,
                                 Model model) {
        Optional<FoodCategory> opt = categoryService.findById(id);
        if (opt.isEmpty()) {
            ra.addAttribute("err", "Category not found");
            return "redirect:/admin/categories";
        }
        FoodCategory existingByName = categoryService.findByName(form.getName());
        if (existingByName != null && !existingByName.getId().equals(id)) {
            result.rejectValue("name", "duplicate", "Category name already in use");
        }
        if (result.hasErrors()) {
            model.addAttribute("activeSection", "categories");
            model.addAttribute("categories", categoryService.findAll());
            model.addAttribute("renameCategoryId", id);
            return "admin/categories";
        }
        FoodCategory cat = opt.get();
        cat.setName(form.getName().trim());
        categoryService.save(cat);
        ra.addAttribute("msg", "Category renamed");
        return "redirect:/admin/categories";
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
