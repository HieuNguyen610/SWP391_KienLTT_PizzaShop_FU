package com.swp.pizzashop.repository;

import com.swp.pizzashop.dto.OrderSummaryDTO;
import com.swp.pizzashop.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query("SELECT COUNT(o) FROM Order o WHERE o.isDeleted = false")
    long countActive();

    @Query("SELECT COALESCE(SUM(o.totalPrice), 0) FROM Order o WHERE o.isDeleted = false")
    BigDecimal sumTotalPriceActive();

    @Query("""
        SELECT new com.swp.pizzashop.dto.OrderSummaryDTO(
            o.id,
            o.user.email,
            o.totalPrice,
            o.status,
            COALESCE(p.status, 'UNPAID'),
            COALESCE(p.paymentType, 'CASH'),
            o.orderTime
        )
        FROM Order o
        JOIN o.user u
        LEFT JOIN o.payments p
        WHERE o.isDeleted = false
        ORDER BY o.createdAt DESC
    """)
    Page<OrderSummaryDTO> findAllOrderSummaries(Pageable pageable);

    @Query("""
        SELECT new com.swp.pizzashop.dto.OrderSummaryDTO(
            o.id,
            o.user.email ,
            o.totalPrice,
            o.status,
            COALESCE(p.status, 'UNPAID'),
            COALESCE(p.paymentType, 'CASH'),
            o.orderTime
        )
        FROM Order o
        JOIN o.user u
        LEFT JOIN o.payments p
        WHERE o.isDeleted = false AND o.status = :status
        ORDER BY o.createdAt DESC
    """)
    Page<OrderSummaryDTO> findByStatus(String status, Pageable pageable);

    @Query("""
        SELECT new com.swp.pizzashop.dto.OrderSummaryDTO(
            o.id,
            o.user.email,
            o.totalPrice,
            o.status,
            COALESCE(p.status, 'UNPAID'),
            COALESCE(p.paymentType, 'CASH'),
            o.orderTime
        )
        FROM Order o
        JOIN o.user u
        LEFT JOIN o.payments p
        WHERE o.isDeleted = false
          AND LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))
        ORDER BY o.createdAt DESC
    """)
    Page<OrderSummaryDTO> searchByCustomerName(String keyword, Pageable pageable);

    @Query("""
    SELECT new map(
        FUNCTION('MONTH', o.createdAt) as month,
        COUNT(CASE WHEN o.status = 'COMPLETED' THEN 1 END) as completedTotal,
        COUNT(CASE WHEN o.status = 'CANCELLED' THEN 1 END) as cancelledTotal
    )
    FROM Order o
    WHERE YEAR(o.createdAt) = :year AND o.isDeleted = false
    GROUP BY FUNCTION('MONTH', o.createdAt)
    ORDER BY FUNCTION('MONTH', o.createdAt) ASC
""")
    List<Map<String, Object>> findMonthlySalesSummary(@Param("year") int year);

}
