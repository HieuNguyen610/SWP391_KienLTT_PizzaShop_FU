package com.swp.pizzashop.repository;

import com.swp.pizzashop.model.Food;
import com.swp.pizzashop.model.FoodCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FoodRepository extends JpaRepository<Food, Long> {

    List<Food> findByIsDeletedFalseOrderByIdDesc();

    long countByCategoryAndIsDeletedFalse(FoodCategory category);

    long countByCategoryAndIsActiveTrueAndIsDeletedFalse(FoodCategory category);

    Page<Food> findByIsDeletedFalse(Pageable pageable);

    Page<Food> findByIsDeletedFalseAndNameContainingIgnoreCase(String name, Pageable pageable);

    Page<Food> findByIsDeletedFalseAndCategoryId(Long categoryId, Pageable pageable);

    Page<Food> findByIsDeletedFalseAndCategoryIdAndNameContainingIgnoreCase(Long categoryId, String name, Pageable pageable);
}

