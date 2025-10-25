package com.swp.pizzashop.repository;

import com.swp.pizzashop.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    Optional<CartItem> findByCartIdAndFoodIdAndSizeId(Long cartId, Long foodId, Long sizeId);
    List<CartItem> findByCartId(Long cartId);
}

