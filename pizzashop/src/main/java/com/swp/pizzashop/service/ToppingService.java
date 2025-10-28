package com.swp.pizzashop.service;

import com.swp.pizzashop.form.ToppingForm;
import com.swp.pizzashop.model.Topping;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface ToppingService {
    List<Topping> findAllActive();
    List<Topping> findDefaultsForFood(Long foodId);
    Set<Long> getDefaultIdsForFood(Long foodId);

    Page<Topping> findPage(String q, Pageable pageable);
    Optional<Topping> findById(Long id);
    Topping create(ToppingForm form);
    Topping update(Long id, ToppingForm form);
    void softDelete(Long id);

    Topping findByNameIgnoreCaseAndIsDeletedFalse(String name);
}