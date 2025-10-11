package com.swp.pizzashop.service;

import com.swp.pizzashop.model.FoodCategory;
import java.util.List;
import java.util.Optional;

public interface FoodCategoryService  {
    List<FoodCategory> findAll();
    FoodCategory findByName(String name);
    Optional<FoodCategory> findById(Long id);
    FoodCategory save(FoodCategory category);
}

