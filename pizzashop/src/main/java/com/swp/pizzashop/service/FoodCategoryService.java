package com.swp.pizzashop.service;

import com.swp.pizzashop.model.FoodCategory;
import com.swp.pizzashop.dto.CategorySummary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface FoodCategoryService {

    List<FoodCategory> findAll();

    FoodCategory findByName(String name);

    Optional<FoodCategory> findById(Long id);

    FoodCategory save(FoodCategory category);

    List<CategorySummary> getCategorySummaries();

    Page<FoodCategory> findPage(String q, Pageable pageable);

    FoodCategory update(Long id, String name, String description);

    void softDelete(Long id);

    void restore(Long id);
}
