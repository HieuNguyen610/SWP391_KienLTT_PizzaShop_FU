package com.swp.pizzashop.service;

import com.swp.pizzashop.model.CartItem;
import com.swp.pizzashop.model.Food;
import com.swp.pizzashop.repository.FoodRepository;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Map;

@Service
public class CartService {

    private final FoodRepository foodRepository;

    public CartService(FoodRepository foodRepository) {
        this.foodRepository = foodRepository;
    }

    // Add item to cart
    public void addToCart(Map<Long, CartItem> cart, Long foodId, int quantity) {
        if (quantity <= 0) return;

        Food food = foodRepository.findById(foodId).orElse(null);
        if (food != null) {
            cart.merge(foodId, new CartItem(food, quantity), (existing, newItem) -> {
                existing.setQuantity(existing.getQuantity() + newItem.getQuantity());
                return existing;
            });
        }
    }

    // Remove item from cart
    public void removeFromCart(Map<Long, CartItem> cart, Long foodId) {
        cart.remove(foodId);
    }

    // Clear cart
    public void clearCart(Map<Long, CartItem> cart) {
        cart.clear();
    }

    // Get cart items
    public Collection<CartItem> getCartItems(Map<Long, CartItem> cart) {
        return cart.values();
    }

    // Get total price
    public double getTotalPrice(Map<Long, CartItem> cart) {
        return cart.values().stream().mapToDouble(CartItem::getTotalPrice).sum();
    }
}
