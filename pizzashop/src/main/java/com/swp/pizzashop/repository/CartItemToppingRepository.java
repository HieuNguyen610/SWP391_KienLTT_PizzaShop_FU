package com.swp.pizzashop.repository;

import com.swp.pizzashop.model.CartItemTopping;
import com.swp.pizzashop.model.CartItemTopping.CartItemToppingId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CartItemToppingRepository extends JpaRepository<CartItemTopping, CartItemToppingId> {
    List<CartItemTopping> findById_CartItemId(Long cartItemId);
}
