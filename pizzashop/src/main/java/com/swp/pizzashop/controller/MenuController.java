package com.swp.pizzashop.controller;

import com.swp.pizzashop.model.Food;
import com.swp.pizzashop.model.FoodCategory;
import com.swp.pizzashop.repository.FoodCategoryRepository;
import com.swp.pizzashop.repository.FoodRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/menu")
@RequiredArgsConstructor
@Slf4j
public class MenuController {

    private final FoodRepository foodRepository;
    private final FoodCategoryRepository foodCategoryRepository;

    @GetMapping
    public String showMenu(Model model) {
        List<Food> foods = foodRepository.findByIsActiveAndIsDeleted(true, false);
        List<FoodCategory> categories = foodCategoryRepository.findAll();

        log.info("Found {} foods", foods.size());
        log.info("Found {} categories", categories.size());
        for (Food food : foods) {
            log.debug("Food: {}, Price: {}, Category: {}",
                    food.getName(),
                    food.getBasePrice(),
                    food.getCategory() != null ? food.getCategory().getName() : "No category");
        }

        model.addAttribute("foods", foods);
        model.addAttribute("categories", categories);
        model.addAttribute("debug", true);

        return "menu_03";
    }
}