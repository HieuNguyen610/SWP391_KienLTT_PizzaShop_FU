package com.swp.pizzashop.model.enums;

public enum PaymentType {
    CASH,        // Tiền mặt
    MOMO,        // Ví Momo
    VNPAY,       // Cổng thanh toán VNPAY
    ZALOPAY,     // Ví ZaloPay
    CARD, // Thẻ tín dụng
    OTHER,       // Khác
    STRIPE,      // Thẻ tín dụng qua Stripe
    PAYPAL       // Thẻ tín dụng qua Paypal
}
