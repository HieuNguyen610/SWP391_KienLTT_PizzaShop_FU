package com.swp.pizzashop.controller;

import com.swp.pizzashop.repository.FoodRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final FoodRepository foodRepository;

    @GetMapping("/")
    public String home(Model model) {
        // Lấy toàn bộ sản phẩm từ database và đưa vào model để view có thể sử dụng
        model.addAttribute("pizzas", foodRepository.findAll());
        return "index"; // Trả về file templates/index.html
    }
}
