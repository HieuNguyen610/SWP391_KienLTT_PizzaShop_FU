package com.swp.pizzashop.service.impl;

import com.swp.pizzashop.model.FoodSize;
import com.swp.pizzashop.repository.FoodSizeRepository;
import com.swp.pizzashop.service.FoodSizeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FoodSizeServiceImpl implements FoodSizeService {

    private final FoodSizeRepository foodSizeRepository;

    @Override
    @Transactional(readOnly = true)
    public List<FoodSize> findByFood(Long foodId) {
        return foodSizeRepository.findByFoodIdAndIsDeletedFalseOrderByPriceAsc(foodId);
    }
}


