package com.swp.pizzashop.service.impl;

import com.swp.pizzashop.form.AddToCartForm;
import com.swp.pizzashop.model.Cart;
import com.swp.pizzashop.model.CartItem;
import com.swp.pizzashop.model.Food;
import com.swp.pizzashop.model.User;
import com.swp.pizzashop.repository.CartItemRepository;
import com.swp.pizzashop.repository.CartRepository;
import com.swp.pizzashop.repository.FoodRepository;
import com.swp.pizzashop.repository.UserRepository;
import com.swp.pizzashop.service.CartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final FoodRepository foodRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public Cart getOrCreateActiveCart(Long userId) {
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User is required");
        }
        return cartRepository.findByUserIdAndIsDeletedFalse(userId)
                .orElseGet(() -> {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
                    Cart cart = Cart.builder().user(user).build();
                    Cart saved = cartRepository.save(cart);
                    log.info("Created new cart id={} for user {}", saved.getId(), userId);
                    return saved;
                });
    }

    @Override
    @Transactional
    public CartItem addToCart(Long userId, AddToCartForm form) {
        if (form == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid request");
        }
        if (form.getQuantity() == null || form.getQuantity() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quantity must be at least 1");
        }

        Cart cart = getOrCreateActiveCart(userId);

        Food food = foodRepository.findById(form.getFoodId())
                .filter(f -> f.getIsDeleted() == null || !f.getIsDeleted())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Food not found"));
        if (!food.isActive()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Food is not available");
        }

        Long sizeId = form.getSizeId();
        if (sizeId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Size is required");
        }

        // TODO: incorporate FoodSize price; for now use base price
        BigDecimal unitPrice = food.getBasePrice();

        CartItem item = cartItemRepository
                .findByCartIdAndFoodIdAndSizeId(cart.getId(), food.getId(), sizeId)
                .map(existing -> {
                    int newQty = existing.getQuantity() + form.getQuantity();
                    existing.setQuantity(newQty);
                    // keep unit price; total is qty * unit when needed
                    existing.setNotes(form.getNotes());
                    return existing;
                })
                .orElseGet(() -> CartItem.builder()
                        .cart(cart)
                        .food(food)
                        .sizeId(sizeId)
                        .quantity(form.getQuantity())
                        .price(unitPrice)
                        .notes(form.getNotes())
                        .build());

        CartItem saved = cartItemRepository.save(item);
        log.info("Added to cartId={} itemId={} foodId={} sizeId={} qty={}", cart.getId(), saved.getId(), food.getId(), sizeId, saved.getQuantity());
        return saved;
    }
}

