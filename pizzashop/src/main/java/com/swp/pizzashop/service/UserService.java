package com.swp.pizzashop.service;

import com.swp.pizzashop.model.User;
import java.util.Optional;

public interface UserService {
    User findByEmail(String email);
    User save(User user);
    // Returns associated User only if token exists, not used, and not expired
    Optional<User> findByResetPasswordToken(String token);
    // Issue a new reset token (persisted in password_reset_tokens table) if user exists.
    Optional<String> initiatePasswordReset(String email);
    // Resets password if token valid; marks token used.
    boolean resetPassword(String token, String newPassword);
    // Change password for an authenticated user; validates old password, strength, and difference.
    ChangePasswordResult changePassword(User user, String oldPassword, String newPassword);

    long countByIsDeletedFalse();

    long countByStatusAndIsDeletedFalse(String active);
}
