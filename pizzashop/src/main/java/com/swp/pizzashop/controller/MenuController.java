package com.swp.pizzashop.controller;

import com.swp.pizzashop.model.Food;
import com.swp.pizzashop.model.FoodCategory;
import com.swp.pizzashop.repository.FoodCategoryRepository;
import com.swp.pizzashop.repository.FoodRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class MenuController {

    private final FoodRepository foodRepository;
    private final FoodCategoryRepository categoryRepository;

    @GetMapping("/menu_04")
    public String menu04(Model model) {
        List<Food> foods = foodRepository.findByIsActiveAndIsDeleted(true, false);
        List<FoodCategory> categories = categoryRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(FoodCategory::getName, String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());

        // Count foods per category (active, not deleted)
        Map<Long, Long> counts = foods.stream()
                .filter(f -> f.getCategory() != null && f.getCategory().getId() != null)
                .collect(Collectors.groupingBy(f -> f.getCategory().getId(), Collectors.counting()));

        // Old price as +15% for display
        Map<Long, BigDecimal> oldPrices = foods.stream()
                .collect(Collectors.toMap(Food::getId,
                        f -> (f.getBasePrice() == null ? BigDecimal.ZERO
                                : f.getBasePrice().multiply(new BigDecimal("1.15")).setScale(2, RoundingMode.HALF_UP))));

        model.addAttribute("foods", foods);
        model.addAttribute("categories", categories);
        model.addAttribute("counts", counts);
        model.addAttribute("oldPrices", oldPrices);
        return "menu_04";
    }

    @GetMapping("/menu/details/{id}")
    public String viewFoodDetail(@PathVariable Long id, Model model) {
        Food food = foodRepository.findById(id).orElse(null);
        BigDecimal oldPrice = food.getBasePrice() != null ? food.getBasePrice().multiply(new BigDecimal("1.15")).setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        model.addAttribute("food", food);
        model.addAttribute("oldPrice", oldPrice);
        return "menu_details";
    }

    @GetMapping("/menu_03")
    public String menu03(Model model,
                         @RequestParam(required = false) Long categoryId,
                         @RequestParam(required = false) BigDecimal minPrice,
                         @RequestParam(required = false) BigDecimal maxPrice,
                         @RequestParam(required = false) String search) {
        List<Food> allFoods = foodRepository.findByIsActiveAndIsDeleted(true, false);
        List<Food> foods = allFoods.stream()
                .filter(f -> categoryId == null || (f.getCategory() != null && f.getCategory().getId().equals(categoryId)))
                .filter(f -> minPrice == null || (f.getBasePrice() != null && f.getBasePrice().compareTo(minPrice) >= 0))
                .filter(f -> maxPrice == null || (f.getBasePrice() != null && f.getBasePrice().compareTo(maxPrice) <= 0))
                .filter(f -> search == null || search.trim().isEmpty() || f.getName().toLowerCase().contains(search.toLowerCase()) || f.getDescription().toLowerCase().contains(search.toLowerCase()))
                .collect(Collectors.toList());

        List<FoodCategory> categories = categoryRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(FoodCategory::getName, String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());

        // Count foods per category (active, not deleted)
        Map<Long, Long> counts = allFoods.stream()
                .filter(f -> f.getCategory() != null && f.getCategory().getId() != null)
                .collect(Collectors.groupingBy(f -> f.getCategory().getId(), Collectors.counting()));

        // Old price as +15% for display
        Map<Long, BigDecimal> oldPrices = foods.stream()
                .collect(Collectors.toMap(Food::getId,
                        f -> (f.getBasePrice() == null ? BigDecimal.ZERO
                                : f.getBasePrice().multiply(new BigDecimal("1.15")).setScale(2, RoundingMode.HALF_UP))));

        model.addAttribute("foods", foods);
        model.addAttribute("categories", categories);
        model.addAttribute("counts", counts);
        model.addAttribute("oldPrices", oldPrices);
        model.addAttribute("selectedCategoryId", categoryId);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        model.addAttribute("search", search);
        return "menu_03";
    }
}