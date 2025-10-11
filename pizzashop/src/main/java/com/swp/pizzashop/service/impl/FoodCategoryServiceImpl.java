package com.swp.pizzashop.service.impl;

import com.swp.pizzashop.model.FoodCategory;
import com.swp.pizzashop.repository.FoodCategoryRepository;
import com.swp.pizzashop.repository.FoodRepository;
import com.swp.pizzashop.service.FoodCategoryService;
import com.swp.pizzashop.dto.CategorySummary;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FoodCategoryServiceImpl implements FoodCategoryService {
    private final FoodCategoryRepository categoryRepository;
    private final FoodRepository foodRepository;

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

    @Override
    public List<CategorySummary> getCategorySummaries() {
        return categoryRepository.findAll().stream().map(c -> new CategorySummary(
                c.getId(),
                c.getName(),
                foodRepository.countByCategoryAndIsDeletedFalse(c),
                foodRepository.countByCategoryAndIsActiveTrueAndIsDeletedFalse(c)
        )).collect(Collectors.toList());
    }
}
