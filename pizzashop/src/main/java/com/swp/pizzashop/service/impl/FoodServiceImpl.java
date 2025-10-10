package com.swp.pizzashop.service.impl;

import com.swp.pizzashop.form.FoodForm;
import com.swp.pizzashop.model.Food;
import com.swp.pizzashop.model.FoodCategory;
import com.swp.pizzashop.repository.FoodCategoryRepository;
import com.swp.pizzashop.repository.FoodRepository;
import com.swp.pizzashop.service.FoodService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class FoodServiceImpl implements FoodService {

    private final FoodRepository foodRepository;
    private final FoodCategoryRepository categoryRepository;

    @Override
    @Transactional
    public Food createFood(FoodForm form) {
        FoodCategory category = categoryRepository.findById(form.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid category id: " + form.getCategoryId()));

        String imageUrl = null;
        if (form.getImageFile() != null && !form.getImageFile().isEmpty()) {
            log.info("Image upload provided for food '{}', skipping storage (TODO)", form.getName());
            // TODO: store file and produce a URL
        } else if (form.getImageUrl() != null && !form.getImageUrl().trim().isEmpty()) {
            imageUrl = form.getImageUrl().trim();
        }

        Food food = Food.builder()
                .name(form.getName().trim())
                .description(form.getDescription())
                .basePrice(form.getBasePrice())
                .imageUrl(imageUrl)
                .isActive(form.isActive())
                .isDeleted(false)
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
            log.info("Image upload provided for food update id={}, skipping storage (TODO)", id);
            // TODO: handle upload
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
        return foodRepository.findByDeletedFalseOrderByIdDesc();
    }
}
