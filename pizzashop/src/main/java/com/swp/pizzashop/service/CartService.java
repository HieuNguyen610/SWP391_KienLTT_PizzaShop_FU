package com.swp.pizzashop.service;

import com.swp.pizzashop.form.AddToCartForm;
import com.swp.pizzashop.model.Cart;
import com.swp.pizzashop.model.CartItem;

public interface CartService {

    Cart getOrCreateActiveCart(Long userId);

    CartItem addToCart(Long userId, AddToCartForm form);
}

