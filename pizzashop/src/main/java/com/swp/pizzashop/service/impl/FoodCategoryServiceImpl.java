package com.swp.pizzashop.service.impl;

import com.swp.pizzashop.model.Food;
import com.swp.pizzashop.model.FoodCategory;
import com.swp.pizzashop.repository.FoodCategoryRepository;
import com.swp.pizzashop.repository.FoodRepository;
import com.swp.pizzashop.service.FoodCategoryService;
import com.swp.pizzashop.dto.CategorySummary;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class FoodCategoryServiceImpl implements FoodCategoryService {
    private final FoodCategoryRepository categoryRepository;
    private final FoodRepository foodRepository;
    @PersistenceContext
    private EntityManager em;

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
    @Transactional
    public void softDelete(Long id) {
        FoodCategory cat = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));

        if (Boolean.TRUE.equals(cat.getIsDeleted())) {
            log.info("Category id={} already soft-deleted", id);
            return;
        }

        // Mark category as deleted
        cat.setIsDeleted(true);
        categoryRepository.save(cat);

        // Repository-based update for foods: load, mutate flags, and save
        List<Food> foods = foodRepository.findByCategoryAndIsDeletedFalse(cat);
        int affected = 0;
        for (Food f : foods) {
            // Only touch non-deleted foods
            if (f.getIsDeleted() == null || !f.getIsDeleted()) {
                f.setIsDeleted(true);      // soft delete food
                affected++;
            }
        }
        if (!foods.isEmpty()) {
            foodRepository.saveAll(foods);
        }
        log.info("Soft-deleted and deactivated {} foods under category id={}", affected, id);
    }

    @Override
    public void restore(Long id) {
        FoodCategory cat = categoryRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Category not found"));
        if (Boolean.TRUE.equals(cat.getIsDeleted())) {
            cat.setIsDeleted(false);
            categoryRepository.save(cat);
        }

        // Repository-based update for foods: load, mutate flags, and save
        List<Food> foods = foodRepository.findByCategoryAndIsDeletedTrue(cat);
        int affected = 0;
        for (Food f : foods) {
            // Only touch deleted foods
            if (f.getIsDeleted()) {
                f.setIsDeleted(false);      // remove soft delete food
                affected++;
            }
        }
        if (!foods.isEmpty()) {
            foodRepository.saveAll(foods);
        }
        log.info("Activated {} foods under category id={}", affected, id);
    }
}
