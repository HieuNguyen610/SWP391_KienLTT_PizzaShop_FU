package com.swp.pizzashop.service.impl;

import com.swp.pizzashop.model.FoodCategory;
import com.swp.pizzashop.repository.FoodCategoryRepository;
import com.swp.pizzashop.repository.FoodRepository;
import com.swp.pizzashop.service.FoodCategoryService;
import com.swp.pizzashop.dto.CategorySummary;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    public List<FoodCategory> findByIsDeletedFalse() {
        return categoryRepository.findByIsDeletedFalse();
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

    @Override
    public Page<FoodCategory> findPage(String q, Pageable pageable) {
        if (q == null || q.trim().isEmpty()) {
            return categoryRepository.findAll(pageable);
        }
        return categoryRepository.findByNameContainingIgnoreCase(q.trim(), pageable);
    }

    @Override
    public FoodCategory update(Long id, String name, String description) {
        FoodCategory cat = categoryRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Category not found"));
        cat.setName(name);
        cat.setDescription(description);
        return categoryRepository.save(cat);
    }

    @Override
    public void softDelete(Long id) {
        FoodCategory cat = categoryRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Category not found"));
        boolean deleted = cat.getIsDeleted() != null ? cat.getIsDeleted() : false;
        if (!deleted) {
            cat.setIsDeleted(true);
            categoryRepository.save(cat);
        }
    }

    @Override
    public void restore(Long id) {
        FoodCategory cat = categoryRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Category not found"));
        if (Boolean.TRUE.equals(cat.getIsDeleted())) {
            cat.setIsDeleted(false);
            categoryRepository.save(cat);
        }
    }
}
