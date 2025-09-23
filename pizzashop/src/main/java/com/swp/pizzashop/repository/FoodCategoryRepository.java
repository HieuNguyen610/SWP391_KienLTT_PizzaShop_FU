package com.swp.pizzashop.repository;
import com.swp.pizzashop.model.FoodCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FoodCategoryRepository extends JpaRepository<FoodCategory, Long> {
    FoodCategory findByName(String name);
}
