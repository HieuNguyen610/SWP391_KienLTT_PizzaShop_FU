package com.swp.pizzashop.repository;

import com.swp.pizzashop.model.Food;
import com.swp.pizzashop.model.FoodCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FoodRepository extends JpaRepository<Food, Long> {
    List<Food> findByIsDeletedFalseOrderByIdDesc();

    long countByCategoryAndIsDeletedFalse(FoodCategory category);

    long countByCategoryAndIsActiveTrueAndIsDeletedFalse(FoodCategory category);
}
