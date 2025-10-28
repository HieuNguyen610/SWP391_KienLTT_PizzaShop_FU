package com.swp.pizzashop.service.impl;

import com.swp.pizzashop.model.Topping;
import com.swp.pizzashop.repository.ToppingRepository;
import com.swp.pizzashop.service.ToppingService;
import com.swp.pizzashop.service.ImageStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.swp.pizzashop.form.ToppingForm;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ToppingServiceImpl implements ToppingService {

    private final ToppingRepository toppingRepository;
    private final ImageStorageService imageStorageService;

    @Override
    @Transactional(readOnly = true)
    public List<Topping> findAllActive() {
        return toppingRepository.findByIsDeletedFalseAndIsActiveTrueOrderByNameAsc();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Topping> findDefaultsForFood(Long foodId) {
        return toppingRepository.findDefaultsForFood(foodId);
    }

    @Override
    @Transactional(readOnly = true)
    public Set<Long> getDefaultIdsForFood(Long foodId) {
        return new HashSet<>(toppingRepository.findDefaultIdsForFood(foodId));
    }

    // admin list & CRUD
    @Override
    @Transactional(readOnly = true)
    public Page<Topping> findPage(String q, Pageable pageable) {
        String query = (q == null) ? null : q.trim();
        boolean hasQ = query != null && !query.isEmpty();
        if (hasQ) {
            return toppingRepository.findByIsDeletedFalseAndNameContainingIgnoreCase(query, pageable);
        }
        return toppingRepository.findByIsDeletedFalse(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Topping> findById(Long id) {
        return toppingRepository.findById(id)
                .filter(t -> t.getIsDeleted() == null || !t.getIsDeleted());
    }

    @Override
    @Transactional
    public Topping create(ToppingForm form) {
        String imageUrl = null;
        if (form.getImageFile() != null && !form.getImageFile().isEmpty()) {
            imageUrl = imageStorageService.storeFoodImage(form.getImageFile());
        } else if (form.getImageUrl() != null && !form.getImageUrl().trim().isEmpty()) {
            imageUrl = form.getImageUrl().trim();
        }

        Topping topping = Topping.builder()
                .name(form.getName().trim())
                .description(form.getDescription())
                .imageUrl(imageUrl)
                .price(form.getPrice())
                .isActive(form.isActive())
                .build();
        Topping saved = toppingRepository.save(topping);
        log.info("Created topping id={} name='{}'", saved.getId(), saved.getName());
        return saved;
    }

    @Override
    @Transactional
    public Topping update(Long id, ToppingForm form) {
        Topping topping = toppingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Topping not found: id=" + id));

        topping.setName(form.getName().trim());
        topping.setDescription(form.getDescription());
        topping.setPrice(form.getPrice());
        topping.setActive(form.isActive());

        if (form.getImageFile() != null && !form.getImageFile().isEmpty()) {
            String old = topping.getImageUrl();
            String stored = imageStorageService.storeFoodImage(form.getImageFile());
            topping.setImageUrl(stored);
            if (old != null) {
                imageStorageService.deleteByPublicPath(old);
            }
        } else if (form.getImageUrl() != null) {
            String url = form.getImageUrl().trim();
            topping.setImageUrl(url.isEmpty() ? null : url);
        }

        Topping saved = toppingRepository.save(topping);
        log.info("Updated topping id={}", saved.getId());
        return saved;
    }

    @Override
    @Transactional
    public void softDelete(Long id) {
        Topping topping = toppingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Topping not found: id=" + id));
        if (Boolean.TRUE.equals(topping.getIsDeleted())) {
            return;
        }
        topping.setIsDeleted(true);
        toppingRepository.save(topping);
    }

    @Override
    @Transactional(readOnly = true)
    public Topping findByNameIgnoreCaseAndIsDeletedFalse(String name) {
        return toppingRepository.findByNameIgnoreCaseAndIsDeletedFalse(name);
    }
}
