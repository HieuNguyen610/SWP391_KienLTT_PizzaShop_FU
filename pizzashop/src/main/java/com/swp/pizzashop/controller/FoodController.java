package com.swp.pizzashop.controller;

import com.swp.pizzashop.form.FoodForm;
import com.swp.pizzashop.repository.FoodCategoryRepository;
import com.swp.pizzashop.service.FoodService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.Collections;

@Controller
@RequiredArgsConstructor
@Slf4j
public class FoodController {

    private final FoodService foodService;
    private final FoodCategoryRepository foodCategoryRepository;

    @GetMapping("/food/create")
    public String showCreateFoodForm(Model model) {
        model.addAttribute("foodForm", new FoodForm());
        try {
            model.addAttribute("categories", foodCategoryRepository.findAll());
        } catch (Exception ex) {
            log.error("Failed to load categories for create food form", ex);
            model.addAttribute("categories", Collections.emptyList());
            model.addAttribute("loadCategoriesError", "Could not load categories right now. Please try again later.");
        }
        return "create-food";
    }

    @PostMapping("/food/create")
    public String handleCreateFood(
            @Valid @ModelAttribute("foodForm") FoodForm foodForm,
            BindingResult bindingResult,
            Model model
    ) {
        // Always reload categories for the form
        try {
            model.addAttribute("categories", foodCategoryRepository.findAll());
        } catch (Exception ex) {
            log.error("Failed to load categories for create food POST", ex);
            model.addAttribute("categories", Collections.emptyList());
            model.addAttribute("loadCategoriesError", "Could not load categories right now. Please try again later.");
        }

        if (bindingResult.hasErrors()) {
            return "create-food";
        }

        // TODO: implement actual save via foodService; for now, just show success feedback
        model.addAttribute("success", "Food created successfully (preview mode)");
        return "create-food";
    }
}