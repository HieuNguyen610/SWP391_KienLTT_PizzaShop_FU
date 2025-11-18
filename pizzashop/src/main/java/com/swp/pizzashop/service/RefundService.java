package com.swp.pizzashop.service;

import com.swp.pizzashop.model.Payment;
import com.swp.pizzashop.model.Refund;

public interface RefundService {
    /**
     * Process a refund for the given payment and refund record. Implementation will call Stripe and update DB.
     */
    void processRefund(Refund refund, Payment payment);
}

