package com.swp.pizzashop.service.impl;

import com.swp.pizzashop.model.Topping;
import com.swp.pizzashop.repository.ToppingRepository;
import com.swp.pizzashop.service.ToppingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ToppingServiceImpl implements ToppingService {

    private final ToppingRepository toppingRepository;

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
}



