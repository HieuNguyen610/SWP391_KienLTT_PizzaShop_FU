package com.swp.pizzashop.controller;

import com.swp.pizzashop.repository.FoodRepository;
import com.swp.pizzashop.repository.FoodCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final FoodRepository foodRepository;
    private final FoodCategoryRepository categoryRepository;

    @GetMapping("/")
    public String home(Model model) {
        // Lấy toàn bộ sản phẩm từ database và đưa vào model để view có thể sử dụng
        model.addAttribute("pizzas", foodRepository.findByIsActiveTrue());
        return "index"; // Trả về file templates/index.html
    }

    @GetMapping("/foods/category/{categoryName}")
    public String viewFoodsByCategory(@PathVariable String categoryName, Model model) {
        // Foods filtered by category (active)
        var foods = foodRepository.findActiveFoodsByCategory(categoryName);

        if (foods.isEmpty()) {
            model.addAttribute("message", "Không tìm thấy món ăn cho category này!");
        }

        // Categories (for sidebar)
        var categories = categoryRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(c -> c.getName().toLowerCase()))
                .collect(Collectors.toList());

        // Counts per category computed from all active, non-deleted foods
        var allActiveFoods = foodRepository.findByIsActiveAndIsDeleted(true, false);
        Map<Long, Long> counts = allActiveFoods.stream()
                .filter(f -> f.getCategory() != null && f.getCategory().getId() != null)
                .collect(Collectors.groupingBy(f -> f.getCategory().getId(), Collectors.counting()));

        // Old price map for currently displayed foods (+15%)
        Map<Long, BigDecimal> oldPrices = foods.stream()
                .collect(Collectors.toMap(
                        com.swp.pizzashop.model.Food::getId,
                        f -> (f.getBasePrice() == null ? BigDecimal.ZERO
                                : f.getBasePrice().multiply(new BigDecimal("1.15")).setScale(2, RoundingMode.HALF_UP))
                ));

        model.addAttribute("foods", foods);
        model.addAttribute("categoryName", categoryName);
        model.addAttribute("categories", categories);
        model.addAttribute("counts", counts);
        model.addAttribute("oldPrices", oldPrices);
        return "menu_04";
    }
}
