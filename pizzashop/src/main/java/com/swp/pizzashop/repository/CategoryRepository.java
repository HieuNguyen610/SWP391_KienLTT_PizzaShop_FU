package com.swp.pizzashop.repository;

import com.swp.pizzashop.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}
