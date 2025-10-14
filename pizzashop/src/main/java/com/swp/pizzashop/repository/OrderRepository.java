package com.swp.pizzashop.repository;

import com.swp.pizzashop.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query("SELECT COUNT(o) FROM Order o WHERE o.isDeleted = false")
    long countActive();

    @Query("SELECT COALESCE(SUM(o.totalPrice), 0) FROM Order o WHERE o.isDeleted = false")
    BigDecimal sumTotalPriceActive();
}
