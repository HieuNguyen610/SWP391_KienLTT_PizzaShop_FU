package com.swp.pizzashop.repository;

import com.swp.pizzashop.model.Food;
import com.swp.pizzashop.model.FoodCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FoodRepository extends JpaRepository<Food, Long> {

    @EntityGraph(attributePaths = "category")
    List<Food> findByIsDeletedFalseOrderByIdDesc();

    long countByCategoryAndIsDeletedFalse(FoodCategory category);

    long countByCategoryAndIsActiveTrueAndIsDeletedFalse(FoodCategory category);

    @EntityGraph(attributePaths = "category")
    Page<Food> findByIsDeletedFalse(Pageable pageable);

    @EntityGraph(attributePaths = "category")
    Page<Food> findByIsActiveTrueAndIsDeletedFalseAndNameContainingIgnoreCase(String name, Pageable pageable);

    @EntityGraph(attributePaths = "category")
    Page<Food> findByIsActiveTrueAndIsDeletedFalseAndCategoryId(Long categoryId, Pageable pageable);

    @EntityGraph(attributePaths = "category")
    Page<Food> findByIsActiveTrueAndIsDeletedFalseAndCategoryIdAndNameContainingIgnoreCase(Long categoryId, String name, Pageable pageable);

    List<Food> findByCategoryAndIsDeletedFalse(FoodCategory category);

    List<Food> findByCategoryAndIsDeletedTrue(FoodCategory cat);

    Optional<Food> findByIdAndIsActiveTrueAndIsDeletedFalse(Long id);

    @EntityGraph(attributePaths = "category")
    Page<Food> findByCategoryIdAndNameContainingIgnoreCase(Long categoryId, String query, Pageable sortedPageable);

    @EntityGraph(attributePaths = "category")
    Page<Food> findByCategoryId(Long categoryId, Pageable sortedPageable);

    @EntityGraph(attributePaths = "category")
    Page<Food> findByNameContainingIgnoreCase(String query, Pageable sortedPageable);
}

