package com.swp.pizzashop.service;

import com.swp.pizzashop.model.User;

public interface EmailService {
    void sendPasswordResetEmail(User user, String resetLink);
}

