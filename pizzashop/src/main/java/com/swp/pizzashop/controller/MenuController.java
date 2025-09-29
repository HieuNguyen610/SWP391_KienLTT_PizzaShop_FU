package com.swp.pizzashop.controller;

import com.swp.pizzashop.model.Category;
import com.swp.pizzashop.model.MenuItem;
import com.swp.pizzashop.service.MenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@Controller
public class MenuController {

    @Autowired
    private MenuService menuService;

    @GetMapping("/menu")
    public String viewMenu(Model model) {
        Map<Category, List<MenuItem>> menuByCategory = menuService.getFullMenu();
        List<Category> categories = menuService.getAllCategories();

        model.addAttribute("menuByCategory", menuByCategory);
        model.addAttribute("categories", categories);
        return "menu";
    }

    @GetMapping("/menu/category")
    public String viewMenuByCategory(@RequestParam Long categoryId, Model model) {
        List<MenuItem> items = menuService.getMenuItemsByCategory(categoryId);
        List<Category> categories = menuService.getAllCategories();

        model.addAttribute("menuItems", items);
        model.addAttribute("categories", categories);
        model.addAttribute("selectedCategoryId", categoryId);
        return "menu";
    }
}
