package com.swp.pizzashop.service.impl;

import com.swp.pizzashop.model.Refund;
import com.swp.pizzashop.repository.RefundRepository;
import com.swp.pizzashop.service.RefundService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefundRetryService {

    private final RefundRepository refundRepository;
    private final RefundService refundService;

    @Value("${refund.retry.max-attempts:5}")
    private int maxAttempts;

    // Retry items not updated in the last X minutes (default 5 minutes)
    @Value("${refund.retry.delay-minutes:5}")
    private int retryDelayMinutes;

    // Runs every minute
    @Scheduled(fixedDelayString = "PT1M")
    public void retryFailedRefunds() {
        try {
            LocalDateTime cutoff = LocalDateTime.now().minusMinutes(retryDelayMinutes);
            List<String> statuses = Arrays.asList("FAILED", "PROCESSING");
            List<Refund> candidates = refundRepository.findByStatusInAndUpdatedAtBefore(statuses, cutoff);
            if (candidates == null || candidates.isEmpty()) return;

            for (Refund r : candidates) {
                if (r.getAttempts() != null && r.getAttempts() >= maxAttempts) {
                    log.warn("Refund id={} reached max attempts={} - skipping", r.getId(), r.getAttempts());
                    continue;
                }

                try {
                    // increment attempts and mark processing
                    r.setAttempts(r.getAttempts() == null ? 1 : r.getAttempts() + 1);
                    r.setStatus("PROCESSING");
                    refundRepository.save(r);

                    // find payment from refund (lazy) - refundService will need Payment too; it expects Payment param in processRefund
                    // We'll reload Payment inside RefundService via repository or assume refund.payment is available via proxy
                    // Call RefundService with a best-effort: if it fails, RefundService will mark FAILED
                    refundService.processRefund(r, r.getPayment());
                } catch (Exception ex) {
                    log.error("Retry attempt failed for refund id={} attempt={} : {}", r.getId(), r.getAttempts(), ex.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("Refund retry scheduler failed: {}", e.getMessage(), e);
        }
    }
}

