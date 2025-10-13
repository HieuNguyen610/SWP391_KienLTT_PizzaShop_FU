package com.swp.pizzashop.repository;

import com.swp.pizzashop.model.Food;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FoodRepository extends JpaRepository<Food, Long> {
    List<Food> findByIsActiveTrue();

    List<Food> findByIsActiveAndIsDeleted(boolean isActive, boolean isDeleted);

    Page<Food> findByIsActiveAndIsDeleted(boolean isActive, boolean isDeleted, Pageable pageable);

    @Query("SELECT f FROM Food f WHERE f.isActive = true AND LOWER(f.category.name) = LOWER(:categoryName)")
    List<Food> findActiveFoodsByCategory(String categoryName);
}
