package com.swp.pizzashop.service;

import com.swp.pizzashop.form.FoodForm;
import com.swp.pizzashop.model.Food;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface FoodService {
    // Create a new food from form data
    Food createFood(FoodForm form);

    // Update an existing food
    Food updateFood(Long id, FoodForm form);

    // Fetch all non-deleted foods for admin listing (legacy)
    List<Food> findAll();

    // Paged list with optional search by name
    Page<Food> findPage(String q, Pageable pageable);

    // Paged list with optional search by name and optional category filter
    Page<Food> findPage(String q, Long categoryId, Pageable pageable);

    Optional<Food> findById(Long id);

    void softDelete(Long id);
}
