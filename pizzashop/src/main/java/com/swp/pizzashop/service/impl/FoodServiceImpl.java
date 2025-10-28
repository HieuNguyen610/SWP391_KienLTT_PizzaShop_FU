package com.swp.pizzashop.service.impl;

import com.swp.pizzashop.form.FoodForm;
import com.swp.pizzashop.model.Food;
import com.swp.pizzashop.model.FoodCategory;
import com.swp.pizzashop.repository.FoodCategoryRepository;
import com.swp.pizzashop.repository.FoodRepository;
import com.swp.pizzashop.service.FoodService;
import com.swp.pizzashop.service.ImageStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class FoodServiceImpl implements FoodService {

    private final FoodRepository foodRepository;
    private final FoodCategoryRepository categoryRepository;
    private final ImageStorageService imageStorageService;

    @Override
    @Transactional
    public Food createFood(FoodForm form) {
        FoodCategory category = categoryRepository.findById(form.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid category id: " + form.getCategoryId()));

        String imageUrl = null;
        if (form.getImageFile() != null && !form.getImageFile().isEmpty()) {
            // Store uploaded file and get public URL
            imageUrl = imageStorageService.storeFoodImage(form.getImageFile());
        } else if (form.getImageUrl() != null && !form.getImageUrl().trim().isEmpty()) {
            imageUrl = form.getImageUrl().trim();
        }

        Food food = Food.builder()
                .name(form.getName().trim())
                .description(form.getDescription())
                .basePrice(form.getBasePrice())
                .imageUrl(imageUrl)
                .isActive(form.isActive())
                .category(category)
                .build();
        Food saved = foodRepository.save(food);
        log.info("Created food id={} name='{}'", saved.getId(), saved.getName());
        return saved;
    }

    @Override
    @Transactional
    public Food updateFood(Long id, FoodForm form) {
        Food food = foodRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Food not found: id=" + id));

        FoodCategory category = categoryRepository.findById(form.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid category id: " + form.getCategoryId()));

        food.setName(form.getName().trim());
        food.setDescription(form.getDescription());
        food.setBasePrice(form.getBasePrice());
        food.setActive(form.isActive());
        food.setCategory(category);

        if (form.getImageFile() != null && !form.getImageFile().isEmpty()) {
            String old = food.getImageUrl();
            String stored = imageStorageService.storeFoodImage(form.getImageFile());
            food.setImageUrl(stored);
            // best-effort cleanup of old stored file (supports /uploads/** and /images/upload/**)
            if (old != null) {
                imageStorageService.deleteByPublicPath(old);
            }
        } else if (form.getImageUrl() != null) {
            String url = form.getImageUrl().trim();
            food.setImageUrl(url.isEmpty() ? null : url);
        }

        Food saved = foodRepository.save(food);
        log.info("Updated food id={}", saved.getId());
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Food> findAll() {
        return foodRepository.findByIsDeletedFalseOrderByIdDesc();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Food> findPage(String q, Pageable pageable) {
        // Delegate to the overloaded method without category filter
        return findPage(q, null, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Food> findPage(String q, Long categoryId, Pageable pageable) {
        // Ensure primary sort by category id for consistent grouping
        Pageable sortedPageable = ensureCategorySort(pageable);

        String query = (q == null) ? null : q.trim();
        boolean hasQ = query != null && !query.isEmpty();
        boolean hasCat = categoryId != null;

        if (hasCat && hasQ) {
            return foodRepository.findByIsDeletedFalseAndCategoryIdAndNameContainingIgnoreCase(categoryId, query, sortedPageable);
        }
        if (hasCat) {
            return foodRepository.findByIsDeletedFalseAndCategoryId(categoryId, sortedPageable);
        }
        if (hasQ) {
            return foodRepository.findByIsDeletedFalseAndNameContainingIgnoreCase(query, sortedPageable);
        }
        return foodRepository.findByIsDeletedFalse(sortedPageable);
    }

    private Pageable ensureCategorySort(Pageable pageable) {
        if (pageable == null) return Pageable.unpaged();
        var existing = pageable.getSort();
        var primary = org.springframework.data.domain.Sort.by("category.id");
        org.springframework.data.domain.Sort combined;
        if (existing == null || existing.isUnsorted()) {
            combined = primary;
        } else {
            // If category.id already present, keep existing; otherwise prefix with category.id
            boolean hasCategoryId = existing.stream().anyMatch(o -> "category.id".equalsIgnoreCase(o.getProperty()));
            combined = hasCategoryId ? existing : primary.and(existing);
        }
        return org.springframework.data.domain.PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), combined);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Food> findById(Long id) {
        return foodRepository.findById(id)
                .filter(f -> f.getIsDeleted() == null || !f.getIsDeleted());
    }

    @Override
    @Transactional
    public void softDelete(Long id) {
        Food food = foodRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Food not found: id=" + id));
        if (Boolean.TRUE.equals(food.getIsDeleted())) {
            log.info("Food id={} already soft-deleted", id);
            return;
        }
        food.setIsDeleted(true);
        foodRepository.save(food);
        log.info("Soft-deleted food id={}", id);
    }
}
