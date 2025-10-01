package com.swp.pizzashop.controller;

import com.swp.pizzashop.model.CartItem;
import com.swp.pizzashop.service.CartService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    // Get cart from session or create new
    @SuppressWarnings("unchecked")
    private Map<Long, CartItem> getCart(HttpSession session) {
        Object cartObj = session.getAttribute("cart");
        if (cartObj == null) {
            Map<Long, CartItem> cart = new HashMap<>();
            session.setAttribute("cart", cart);
            return cart;
        } else {
            return (Map<Long, CartItem>) cartObj;
        }
    }

    // Add item to cart
    @PostMapping("/add")
    public String addToCart(@RequestParam Long foodId,
                            @RequestParam int quantity,
                            HttpSession session) {
        Map<Long, CartItem> cart = getCart(session);
        cartService.addToCart(cart, foodId, quantity);
        return "redirect:/cart/view";
    }

    // View cart
    @GetMapping("/view")
    public String viewCart(HttpSession session, Model model) {
        Map<Long, CartItem> cart = getCart(session);
        model.addAttribute("cartItems", cartService.getCartItems(cart));
        model.addAttribute("totalPrice", cartService.getTotalPrice(cart));
        return "cart"; // cart.html
    }

    // Remove item from cart
    @PostMapping("/remove")
    public String removeItem(@RequestParam Long foodId, HttpSession session) {
        Map<Long, CartItem> cart = getCart(session);
        cartService.removeFromCart(cart, foodId);
        return "redirect:/cart/view";
    }

    // Clear cart
    @PostMapping("/clear")
    public String clearCart(HttpSession session) {
        Map<Long, CartItem> cart = getCart(session);
        cartService.clearCart(cart);
        return "redirect:/cart/view";
    }
}
