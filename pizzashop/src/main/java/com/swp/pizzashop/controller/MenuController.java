package com.swp.pizzashop.controller;

import com.swp.pizzashop.model.Food;
import com.swp.pizzashop.service.FoodCategoryService;
import com.swp.pizzashop.service.FoodService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class MenuController {

    private final FoodService foodService;
    private final FoodCategoryService categoryService;

    @GetMapping("/")
    public String home(Model model) {
        // Lấy toàn bộ sản phẩm từ database và đưa vào model để view có thể sử dụng
        model.addAttribute("pizzas", foodService.findAll());
        return "index"; // Trả về file templates/index.html
    }

    @GetMapping("/menu")
    public String menu(
            @RequestParam(value = "q", required = false) String q,
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "8") int size,
            Model model
    ) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 50);
        Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "id"));

        Page<Food> pageData = foodService.findPage(q, categoryId, pageable);

        // list for UI rendering
        model.addAttribute("pizzas", pageData.getContent());
        // page meta for pagination UI
        model.addAttribute("page", pageData);

        // filters state
        model.addAttribute("q", q);
        model.addAttribute("categoryId", categoryId);

        // categories for filter select/tabs
        model.addAttribute("categories", categoryService.findByIsDeletedFalse());
//        model.addAttribute("categorySummaries", categoryService.getCategorySummaries());
        return "view-menu";
    }

    @GetMapping("/menu/food/{id}")
    public String foodDetail(@PathVariable("id") Long id, Model model) {
        return foodService.findById(id)
                .map(food -> {
                    model.addAttribute("food", food);
                    model.addAttribute("categories", categoryService.findAll());
                    return "food-detail";
                })
                .orElse("error");
    }
}
