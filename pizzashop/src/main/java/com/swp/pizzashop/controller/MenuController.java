package com.swp.pizzashop.controller;

import com.swp.pizzashop.model.Food;
import com.swp.pizzashop.model.FoodCategory;
import com.swp.pizzashop.repository.FoodCategoryRepository;
import com.swp.pizzashop.repository.FoodRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

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
}