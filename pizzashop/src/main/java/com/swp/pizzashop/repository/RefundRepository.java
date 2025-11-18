package com.swp.pizzashop.repository;

import com.swp.pizzashop.model.Refund;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RefundRepository extends JpaRepository<Refund, Long> {
    Optional<Refund> findByProviderRefundId(String providerRefundId);

    // Find refunds by status older than a threshold (used by retry scheduler)
    List<Refund> findByStatusAndUpdatedAtBefore(String status, LocalDateTime before);

    // Find refunds with FAILED status older than threshold
    List<Refund> findByStatusInAndUpdatedAtBefore(List<String> statuses, LocalDateTime before);
}
