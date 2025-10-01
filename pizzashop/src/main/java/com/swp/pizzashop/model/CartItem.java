package com.swp.pizzashop.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartItem {
    private Food food;
    private int quantity;

    public double getTotalPrice() {
        return food.getBasePrice().doubleValue() * quantity;
    }
}
