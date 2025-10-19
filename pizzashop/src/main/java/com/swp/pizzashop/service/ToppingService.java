package com.swp.pizzashop.service;

import com.swp.pizzashop.model.Topping;

import java.util.List;
import java.util.Set;

public interface ToppingService {
    List<Topping> findAllActive();
    List<Topping> findDefaultsForFood(Long foodId);
    Set<Long> getDefaultIdsForFood(Long foodId);
}