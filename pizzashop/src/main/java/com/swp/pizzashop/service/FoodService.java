package com.swp.pizzashop.service;

import com.swp.pizzashop.form.FoodForm;
import com.swp.pizzashop.model.Food;

import java.util.List;

public interface FoodService {
    // Create a new food from form data
    Food createFood(FoodForm form);

    // Update an existing food
    Food updateFood(Long id, FoodForm form);

    // Fetch all non-deleted foods for admin listing
    List<Food> findAll();
}
