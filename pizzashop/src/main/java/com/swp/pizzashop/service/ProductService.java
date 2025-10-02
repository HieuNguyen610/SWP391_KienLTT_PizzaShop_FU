package com.swp.pizzashop.service;

import com.swp.pizzashop.model.Product;
import com.swp.pizzashop.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {
    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public void addSampleData() {
        if (productRepository.count() == 0) {
            productRepository.save(new Product("Margherita", 5.99, "Classic cheese pizza", "/images/pizza1.jpg"));
            productRepository.save(new Product("Pepperoni", 7.99, "Pepperoni and cheese", "/images/pizza2.jpg"));
            productRepository.save(new Product("BBQ Chicken", 8.99, "Chicken with BBQ sauce", "/images/pizza3.jpg"));
        }
    }
}
