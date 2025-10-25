package com.swp.pizzashop.service;

import com.swp.pizzashop.model.FoodSize;

import java.util.List;

public interface FoodSizeService {
    List<FoodSize> findByFood(Long foodId);
}