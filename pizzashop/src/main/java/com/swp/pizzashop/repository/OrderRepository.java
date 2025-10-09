package com.swp.pizzashop.repository;

import com.swp.pizzashop.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByStripeSessionId(String stripeSessionId);
}

