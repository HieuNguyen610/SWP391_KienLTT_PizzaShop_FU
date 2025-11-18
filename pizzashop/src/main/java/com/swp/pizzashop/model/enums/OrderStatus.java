package com.swp.pizzashop.model.enums;

public enum OrderStatus {
    PENDING, // Đang chờ xử lý
    ACCEPTED,      // Đã chấp nhận ( nhan đơn )
    COOKING,        // Đã nấu
    DELIVERING,     // Đang giao hàng
    COMPLETED,      // Hoàn thành
    CANCELLED,      // Đã hủy
}
