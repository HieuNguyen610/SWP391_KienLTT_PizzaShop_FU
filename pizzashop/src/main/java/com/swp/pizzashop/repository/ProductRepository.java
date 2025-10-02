package com.swp.pizzashop.repository;

import com.swp.pizzashop.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
