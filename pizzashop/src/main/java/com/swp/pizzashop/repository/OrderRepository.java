package com.swp.pizzashop.repository;

import com.swp.pizzashop.dto.OrderDetailDTO;
import com.swp.pizzashop.dto.OrderSummaryDTO;
import com.swp.pizzashop.model.Order;
import org.antlr.v4.runtime.atn.SemanticContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
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

    @Query("""
            SELECT new com.swp.pizzashop.dto.OrderDetailDTO(
                   o.id,
                   o.user.email,
                   o.orderTime,
                   o.status,
                   o.totalPrice,
                   o.paymentMethod,
                   CONCAT(
                       COALESCE(a.addressLine, ''), ' ',
                       COALESCE(a.district, ''), ' ',
                       COALESCE(a.city, '')
                   ),
                   a.phone,
                   o.discountId,
                   o.createdAt
               )
               FROM Order o
               LEFT JOIN o.deliveryAddress a
               WHERE o.user.id = :userId
               ORDER BY o.createdAt DESC
            """)
    Page<OrderDetailDTO> findAllOrderByUserId(Long userId, Pageable pageable);

    @Query("""
       SELECT o FROM Order o
       WHERE DATE(o.createdAt) = CURRENT_DATE
       ORDER BY o.createdAt DESC
       """)
    Page<Order> findTodayOrders(Pageable pageable);

    @Query("""
       SELECT o FROM Order o
       WHERE DATE(o.createdAt) = CURRENT_DATE
       AND o.paymentMethod = :paymentMethod
       ORDER BY o.createdAt DESC
       """)
    Page<Order> findTodayOrdersByPayment(
            @Param("paymentMethod") String paymentMethod,
            Pageable pageable);

    @Query("""
       SELECT o FROM Order o
       WHERE DATE(o.createdAt) = CURRENT_DATE
       AND o.id = :orderId
       ORDER BY o.createdAt DESC
       """)
    List<Order> findTodayOrdersById(@Param("orderId") Long orderId);

    @Query("""
    SELECT new com.swp.pizzashop.dto.OrderSummaryDTO(
        o.id, o.user.email, o.totalPrice, o.status,
        COALESCE(p.status, 'UNPAID'),
        COALESCE(p.paymentType, 'CASH'),
        o.orderTime
    )
    FROM Order o
    LEFT JOIN o.payments p
    WHERE o.isDeleted = false
      AND DATE(o.createdAt) = :date
    ORDER BY o.createdAt DESC
""")
    Page<OrderSummaryDTO> findOrdersByDate(@Param("date") LocalDate date, Pageable pageable);

    @Query("""
    SELECT new com.swp.pizzashop.dto.OrderSummaryDTO(
        o.id, o.user.email, o.totalPrice, o.status,
        COALESCE(p.status, 'UNPAID'),
        COALESCE(p.paymentType, 'CASH'),
        o.orderTime
    )
    FROM Order o
    LEFT JOIN o.payments p
    WHERE o.isDeleted = false
      AND DATE(o.createdAt) = :date
      AND o.paymentMethod = :paymentMethod
    ORDER BY o.createdAt DESC
""")
    Page<OrderSummaryDTO> findOrdersByPaymentAndDate(
            @Param("paymentMethod") String paymentMethod,
            @Param("date") LocalDate date,
            Pageable pageable);

    @Query("""
    SELECT new com.swp.pizzashop.dto.OrderSummaryDTO(
        o.id, o.user.email, o.totalPrice, o.status,
        COALESCE(p.status, 'UNPAID'),
        COALESCE(p.paymentType, 'CASH'),
        o.orderTime
    )
    FROM Order o
    LEFT JOIN o.payments p
    WHERE o.isDeleted = false
      AND DATE(o.createdAt) = :date
      AND o.id = :orderId
      AND (:status IS NULL OR :status = '' OR o.status = :status)
      
    ORDER BY o.createdAt DESC
""")
    List<OrderSummaryDTO> findOrdersByIdAndDate(
            @Param("orderId") Long orderId,
            @Param("date") LocalDate date);



    @Query("""
    SELECT new map(
        FUNCTION('HOUR', o.createdAt) as hour,
        COUNT(CASE WHEN o.status = 'COMPLETED' THEN 1 END) as completed,
        COUNT(CASE WHEN o.status = 'CANCELLED' THEN 1 END) as cancelled
    )
    FROM Order o
    WHERE DATE(o.createdAt) = :targetDate
      AND o.isDeleted = false
    GROUP BY FUNCTION('HOUR', o.createdAt)
    ORDER BY FUNCTION('HOUR', o.createdAt)
""")
    List<Map<String, Object>> findHourlySummary(@Param("targetDate") LocalDate targetDate);

    @Query("""
SELECT new com.swp.pizzashop.dto.OrderSummaryDTO(
    o.id, o.user.email, o.totalPrice, o.status,
    COALESCE(p.status, 'UNPAID'),
    COALESCE(p.paymentType, 'CASH'),
    o.orderTime
)
FROM Order o
LEFT JOIN o.payments p
WHERE o.isDeleted = false
  AND DATE(o.createdAt) = :date
  AND o.id = :orderId
  AND (:status IS NULL OR :status = '' OR o.status = :status)
ORDER BY o.createdAt DESC
""")
    List<OrderSummaryDTO> findOrdersByIdAndDateAndStatus(
            @Param("orderId") Long orderId,
            @Param("date") LocalDate date,
            @Param("status") String status);


    @Query("""
SELECT new com.swp.pizzashop.dto.OrderSummaryDTO(
    o.id, o.user.email, o.totalPrice, o.status,
    COALESCE(p.status, 'UNPAID'),
    COALESCE(p.paymentType, 'CASH'),
    o.orderTime
)
FROM Order o
LEFT JOIN o.payments p
WHERE o.isDeleted = false
  AND DATE(o.createdAt) = :date
  AND o.paymentMethod = :payment
  AND (:status IS NULL OR :status = '' OR o.status = :status)
                                             
ORDER BY o.createdAt DESC
""")
    Page<OrderSummaryDTO> findOrdersByPaymentDateStatus(
            @Param("payment") String payment,
            @Param("date") LocalDate date,
            @Param("status") String status,
            Pageable pageable);


    @Query("""
SELECT new com.swp.pizzashop.dto.OrderSummaryDTO(
    o.id, o.user.email, o.totalPrice, o.status,
    COALESCE(p.status, 'UNPAID'),
    COALESCE(p.paymentType, 'CASH'),
    o.orderTime
)
FROM Order o
LEFT JOIN o.payments p
WHERE o.isDeleted = false
  AND DATE(o.createdAt) = :date
  AND (:status IS NULL OR :status = '' OR o.status = :status)
                                             
ORDER BY o.createdAt DESC
""")
    Page<OrderSummaryDTO> findOrdersByDateAndStatus(
            @Param("date") LocalDate date,
            @Param("status") String status,
            Pageable pageable);

}
