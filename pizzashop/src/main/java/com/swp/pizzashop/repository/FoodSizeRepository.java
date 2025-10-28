package com.swp.pizzashop.repository;

import com.swp.pizzashop.model.FoodSize;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FoodSizeRepository extends JpaRepository<FoodSize, Long> {
    List<FoodSize> findByFoodIdAndIsDeletedFalseOrderByPriceAsc(Long foodId);
}

