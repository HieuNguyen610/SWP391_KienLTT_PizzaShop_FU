package com.swp.pizzashop.controller;

import com.swp.pizzashop.model.CartItem;
import com.swp.pizzashop.model.Food;
import com.swp.pizzashop.repository.FoodRepository;
import com.swp.pizzashop.service.CartService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Controller
public class HomeController {

    private final FoodRepository foodRepository;
    private final CartService cartService;

    public HomeController(FoodRepository foodRepository, CartService cartService) {
        this.foodRepository = foodRepository;
        this.cartService = cartService;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("foods", foodRepository.findAll());
        return "home"; // home.html
    }

    // Add item to cart from home page
    @PostMapping("/add-to-cart")
    public String addToCart(@RequestParam Long foodId,
                            @RequestParam(defaultValue = "1") int quantity,
                            HttpSession session) {
        @SuppressWarnings("unchecked")
        Map<Long, CartItem> cart = (Map<Long, CartItem>) session.getAttribute("cart");
        if (cart == null) {
            cart = new HashMap<>();
            session.setAttribute("cart", cart);
        }

        cartService.addToCart(cart, foodId, quantity);
        return "redirect:/cart/view";
    }
}
