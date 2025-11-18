package com.swp.pizzashop.repository;

import com.swp.pizzashop.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByOrder_Id(Long orderId);

    Optional<Payment> findByTransactionId(String transactionId);
}
