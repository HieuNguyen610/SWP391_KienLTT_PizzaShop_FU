package com.swp.pizzashop.service;

import com.swp.pizzashop.form.RegisterForm;
import com.swp.pizzashop.model.Role;
import com.swp.pizzashop.model.User;
import java.util.Optional;
import com.swp.pizzashop.repository.RoleRepository;
import com.swp.pizzashop.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

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
    User registerUser(RegisterForm registerForm);
    User updateUser(User user);
    public void activateUser(String email) ;
    Page<User> findAllOrderedWithSearch(String keyword, Pageable pageable);


    void toggleUserStatus(Long id, String status);

    void createUserWithRole(User user);

}
