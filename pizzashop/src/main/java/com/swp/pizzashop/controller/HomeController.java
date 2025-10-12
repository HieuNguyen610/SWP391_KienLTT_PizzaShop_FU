package com.swp.pizzashop.controller;

import com.swp.pizzashop.repository.FoodRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final FoodRepository foodRepository;

    @GetMapping("/")
    public String home(Model model) {
        // Lấy toàn bộ sản phẩm từ database và đưa vào model để view có thể sử dụng
        model.addAttribute("pizzas", foodRepository.findByIsActiveTrue());
        return "index"; // Trả về file templates/index.html
    }

    @GetMapping("/foods/category/{categoryName}")
    public String viewFoodsByCategory(@PathVariable String categoryName, Model model) {
        var foods = foodRepository.findActiveFoodsByCategory(categoryName);

        if (foods.isEmpty()) {
            model.addAttribute("message", "Không tìm thấy món ăn cho category này!");
        }
        model.addAttribute("foods", foods);
        model.addAttribute("categoryName", categoryName);
        return "menu_03";
    }
}
