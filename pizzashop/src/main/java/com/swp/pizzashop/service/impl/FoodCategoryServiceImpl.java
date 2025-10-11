package com.swp.pizzashop.service.impl;

import com.swp.pizzashop.model.FoodCategory;
import com.swp.pizzashop.repository.FoodCategoryRepository;
import com.swp.pizzashop.service.FoodCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FoodCategoryServiceImpl implements FoodCategoryService {

    private final FoodCategoryRepository categoryRepository;

    @Override
    public List<FoodCategory> findAll() {
        return categoryRepository.findAll();
    }

    @Override
    public FoodCategory findByName(String name) {
        return categoryRepository.findByName(name);
    }

    @Override
    public Optional<FoodCategory> findById(Long id) {
        return categoryRepository.findById(id);
    }

    @Override
    public FoodCategory save(FoodCategory category) {
        return categoryRepository.save(category);
    }
}

