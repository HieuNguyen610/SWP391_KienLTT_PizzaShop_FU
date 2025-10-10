package com.swp.pizzashop.controller;

import com.swp.pizzashop.form.CategoryForm;
import com.swp.pizzashop.form.FoodForm;
import com.swp.pizzashop.model.Food;
import com.swp.pizzashop.model.FoodCategory;
import com.swp.pizzashop.repository.FoodCategoryRepository;
import com.swp.pizzashop.service.FoodService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminController {

    private final FoodService foodService;
    private final FoodCategoryRepository categoryRepository;

    @GetMapping
    public String dashboard(Model model,
                            @RequestParam(value = "msg", required = false) String msg,
                            @RequestParam(value = "err", required = false) String err) {
        loadLists(model);
        if (!model.containsAttribute("categoryForm")) {
            model.addAttribute("categoryForm", new CategoryForm());
        }
        if (!model.containsAttribute("foodForm")) {
            model.addAttribute("foodForm", new FoodForm());
        }
        if (msg != null) model.addAttribute("success", msg);
        if (err != null) model.addAttribute("error", err);
        return "admin/dashboard";
    }

    @PostMapping("/categories")
    public String addCategory(@Valid @ModelAttribute("categoryForm") CategoryForm form,
                              BindingResult result,
                              Model model,
                              RedirectAttributes ra) {
        if (categoryRepository.findByName(form.getName()) != null) {
            result.rejectValue("name", "duplicate", "Category already exists");
        }
        if (result.hasErrors()) {
            loadLists(model);
            return "admin/dashboard";
        }
        FoodCategory cat = new FoodCategory();
        cat.setName(form.getName().trim());
        categoryRepository.save(cat);
        ra.addAttribute("msg", "Category created");
        return "redirect:/admin";
    }

    @PostMapping("/categories/{id}")
    public String renameCategory(@PathVariable Long id,
                                 @Valid @ModelAttribute("categoryForm") CategoryForm form,
                                 BindingResult result,
                                 RedirectAttributes ra,
                                 Model model) {
        Optional<FoodCategory> opt = categoryRepository.findById(id);
        if (opt.isEmpty()) {
            ra.addAttribute("err", "Category not found");
            return "redirect:/admin";
        }
        FoodCategory existingByName = categoryRepository.findByName(form.getName());
        if (existingByName != null && !existingByName.getId().equals(id)) {
            result.rejectValue("name", "duplicate", "Category name already in use");
        }
        if (result.hasErrors()) {
            loadLists(model);
            model.addAttribute("renameCategoryId", id);
            return "admin/dashboard";
        }
        FoodCategory cat = opt.get();
        cat.setName(form.getName().trim());
        categoryRepository.save(cat);
        ra.addAttribute("msg", "Category renamed");
        return "redirect:/admin";
    }

    @PostMapping("/foods")
    public String addFood(@Valid @ModelAttribute("foodForm") FoodForm form,
                          BindingResult result,
                          Model model,
                          RedirectAttributes ra) {
        if (result.hasErrors()) {
            loadLists(model);
            return "admin/dashboard";
        }
        try {
            Food saved = foodService.createFood(form);
            ra.addAttribute("msg", "Food created: " + saved.getName());
            return "redirect:/admin";
        } catch (Exception ex) {
            log.error("Create food failed", ex);
            result.reject("createFailed", ex.getMessage());
            loadLists(model);
            return "admin/dashboard";
        }
    }

    @GetMapping("/foods/{id}/edit")
    public String editFood(@PathVariable Long id, Model model, RedirectAttributes ra) {
        // We will reuse FoodService to fetch and map. For simplicity, load entity via lists
        List<Food> foods = foodService.findAll();
        Food target = foods.stream().filter(f -> f.getId().equals(id)).findFirst().orElse(null);
        if (target == null) {
            ra.addAttribute("err", "Food not found");
            return "redirect:/admin";
        }
        FoodForm form = new FoodForm();
        form.setName(target.getName());
        form.setDescription(target.getDescription());
        form.setBasePrice(target.getBasePrice());
        form.setActive(target.isActive());
        form.setImageUrl(target.getImageUrl());
        form.setCategoryId(target.getCategory().getId());
        model.addAttribute("foodForm", form);
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("foodId", id);
        return "admin/food-edit";
    }

    @PostMapping("/foods/{id}/edit")
    public String updateFood(@PathVariable Long id,
                             @Valid @ModelAttribute("foodForm") FoodForm form,
                             BindingResult result,
                             Model model,
                             RedirectAttributes ra) {
        if (result.hasErrors()) {
            model.addAttribute("categories", categoryRepository.findAll());
            model.addAttribute("foodId", id);
            return "admin/food-edit";
        }
        try {
            foodService.updateFood(id, form);
            ra.addAttribute("msg", "Food updated");
            return "redirect:/admin";
        } catch (Exception ex) {
            log.error("Update food failed", ex);
            result.reject("updateFailed", ex.getMessage());
            model.addAttribute("categories", categoryRepository.findAll());
            model.addAttribute("foodId", id);
            return "admin/food-edit";
        }
    }

    private void loadLists(Model model) {
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("foods", foodService.findAll());
    }
}

