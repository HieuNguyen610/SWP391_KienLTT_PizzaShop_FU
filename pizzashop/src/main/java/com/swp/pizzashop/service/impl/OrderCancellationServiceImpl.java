package com.swp.pizzashop.service.impl;

import com.swp.pizzashop.model.Order;
import com.swp.pizzashop.model.Payment;
import com.swp.pizzashop.model.Refund;
import com.swp.pizzashop.repository.OrderRepository;
import com.swp.pizzashop.repository.PaymentRepository;
import com.swp.pizzashop.repository.RefundRepository;
import com.swp.pizzashop.service.OrderCancellationService;
import com.swp.pizzashop.service.RefundService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderCancellationServiceImpl implements OrderCancellationService {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final RefundRepository refundRepository;
    private final RefundService refundService;

    @Override
    @Transactional
    public void cancelOrderByCustomer(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        String current = order.getStatus();
        if (!"PAID".equalsIgnoreCase(current)) {
            throw new IllegalStateException("Order can only be cancelled at PAID stage.");
        }

        List<Payment> payments = paymentRepository.findByOrder_Id(orderId);

        for (Payment p : payments) {
            try {
                if (p.getPaymentType() != null && "STRIPE".equalsIgnoreCase(p.getPaymentType())
                        && p.getStatus() != null && "SUCCESS".equalsIgnoreCase(p.getStatus())) {

                    Refund refund = Refund.builder()
                            .order(order)
                            .payment(p)
                            .amount(p.getAmount())
                            .currency(null)
                            .status("PENDING")
                            .idempotencyKey("order-" + orderId + "-payment-" + p.getId())
                            .build();
                    refund = refundRepository.save(refund);

                    // process refund (may throw)
                    refundService.processRefund(refund, p);
                }
            } catch (Exception ex) {
                log.error("Failed to process refund for orderId={} paymentId={}: {}", orderId, p.getId(), ex.getMessage());
                // continue with other payments; refund record should be marked FAILED by RefundServiceImpl
            }
        }

        order.setStatus("CANCELLED");
        order.setCancelledAt(LocalDateTime.now());
        order.setCancelledBy("CUSTOMER");
        orderRepository.save(order);
    }
}

