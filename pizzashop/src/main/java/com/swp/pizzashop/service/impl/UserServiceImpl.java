package com.swp.pizzashop.service.impl;

import com.swp.pizzashop.form.RegisterForm;
import com.swp.pizzashop.model.PasswordResetToken;
import com.swp.pizzashop.model.Role;
import com.swp.pizzashop.model.User;
import com.swp.pizzashop.repository.PasswordResetTokenRepository;
import com.swp.pizzashop.repository.RoleRepository;
import com.swp.pizzashop.repository.UserRepository;
import com.swp.pizzashop.service.EmailService;
import com.swp.pizzashop.service.UserService;
import com.swp.pizzashop.service.ChangePasswordResult;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.*;

import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    @Autowired
    private RoleRepository roleRepository;
    private final EmailService emailService;
    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int TOKEN_BYTES = 24; // ~32 char
    private static final int EXPIRY_MINUTES = 30;

    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public User save(User user) {
        return userRepository.save(user);
    }

    @Override
    public Optional<User> findByResetPasswordToken(String token) {
        return passwordResetTokenRepository.findByToken(token)
                .filter(t -> !t.isUsed() && !t.isExpired())
                .map(PasswordResetToken::getUser);
    }

    private String generateToken() {
        byte[] bytes = new byte[TOKEN_BYTES];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String generateUniqueToken() {
        for (int attempts = 0; attempts < 5; attempts++) {
            String token = generateToken();
            if (!passwordResetTokenRepository.existsByToken(token)) {
                return token;
            }
            log.warn("Password reset token collision detected (attempt {})", attempts + 1);
        }
        throw new IllegalStateException("Unable to generate unique password reset token after 5 attempts");
    }

    @Override
    @Transactional
    public Optional<String> initiatePasswordReset(String email) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            log.debug("Password reset requested for non-existing email: {}", email);
            return Optional.empty();
        }
        // Invalidate existing tokens for this user (optional cleanup policy)
        try {
            passwordResetTokenRepository.deleteByUserId(user.getId());
        } catch (Exception e) {
            log.warn("Failed to delete existing password reset tokens for user {}: {}", user.getEmail(), e.getMessage());
        }

        String rawToken = generateUniqueToken();
        PasswordResetToken tokenEntity = PasswordResetToken.builder()
                .user(user)
                .token(rawToken)
                .expiresAt(LocalDateTime.now().plusMinutes(EXPIRY_MINUTES))
                .used(false)
                .build();
        passwordResetTokenRepository.saveAndFlush(tokenEntity);
        log.info("Generated password reset token id={} for {} expiring at {}", tokenEntity.getId(), email, tokenEntity.getExpiresAt());
        return Optional.of(rawToken);
    }

    @Override
    @Transactional
    public boolean resetPassword(String token, String newPassword) {
        Optional<PasswordResetToken> tokenOpt = passwordResetTokenRepository.findByToken(token);
        if (tokenOpt.isEmpty()) {
            log.debug("Reset attempt with invalid token");
            return false;
        }
        PasswordResetToken prt = tokenOpt.get();
        if (prt.isUsed() || prt.isExpired()) {
            log.debug("Reset attempt with used/expired token id={} for user {}", prt.getId(), prt.getUser().getEmail());
            return false;
        }
        User user = prt.getUser();
        try {
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);
            prt.setUsed(true);
            passwordResetTokenRepository.save(prt);
            log.info("Password reset successful for user {} (token id={})", user.getEmail(), prt.getId());
            return true;
        } catch (DataIntegrityViolationException e) {
            log.error("Data integrity violation while resetting password for user {} token id={}: {}", user.getEmail(), prt.getId(), e.getMessage(), e);
            return false;
        } catch (ConstraintViolationException e) {
            e.getConstraintViolations().forEach(v ->
                    log.error("Constraint violation resetting password: {} {} -> {}", v.getPropertyPath(), v.getInvalidValue(), v.getMessage())
            );
            return false;
        } catch (Exception e) {
            log.error("Unexpected error during password reset for user {} token id={}", user.getEmail(), prt.getId(), e);
            throw e;
        }
    }

    @Override
    @Transactional
    public ChangePasswordResult changePassword(User user, String oldPassword, String newPassword) {
        try {
            if (user == null) return ChangePasswordResult.ERROR;
            if (oldPassword == null || newPassword == null) return ChangePasswordResult.ERROR;
            newPassword = newPassword.trim();
            if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
                return ChangePasswordResult.OLD_PASSWORD_INVALID;
            }
            // If new password equals old (matches current hash)
            if (passwordEncoder.matches(newPassword, user.getPassword())) {
                return ChangePasswordResult.NEW_PASSWORD_SAME_AS_OLD;
            }
            if (newPassword.length() < 8) {
                return ChangePasswordResult.WEAK_NEW_PASSWORD;
            }
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);
            log.info("Changed password for user {}", user.getEmail());
            return ChangePasswordResult.SUCCESS;
        } catch (Exception e) {
            log.error("Failed to change password for user {}: {}", user != null ? user.getEmail() : null, e.getMessage(), e);
            return ChangePasswordResult.ERROR;
        }
    }

    @Override
    public long countByIsDeletedFalse() {
        return userRepository.countByIsDeletedFalse();
    }

    @Override
    public long countByStatusAndIsDeletedFalse(String active) {
        return userRepository.countByStatusAndIsDeletedFalse(active);
    }

    @Override
    public User registerUser(RegisterForm registerForm) {
        if (userRepository.findByEmail(registerForm.getEmail()) != null) {
            throw new IllegalArgumentException("Email is already registered");
        }

        String hashedPassword = passwordEncoder.encode(registerForm.getPassword());

        User user = new User();
        user.setFirstname(registerForm.getFirstname());
        user.setLastname(registerForm.getLastname());
        user.setEmail(registerForm.getEmail());
        user.setPassword(hashedPassword);
        user.setPhone(registerForm.getPhone());
        user.setStatus("ACTIVE");
        user.setProvider(null);
        user.setProviderId(null);
        user.setVerificationToken(null);
        user.setVerificationExpiresAt(null);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        Role customerRole = roleRepository.findByName("Customer");
        if (customerRole != null) {
            Set<Role> roles = new HashSet<>();
            roles.add(customerRole);
            user.setRoles(roles);

        }
        User savedUser = userRepository.save(user);


        return savedUser;
    }

    @Override
    public User updateUser(User user) {
        return userRepository.save(user);
    }

    @Override
    public void activateUser(String email) {

        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }
        user.setStatus("ACTIVE");
        user.setVerified(true);
        userRepository.save(user);
    }

    public Page<User> findAllOrderedWithSearch(String keyword, Pageable pageable) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return userRepository.findAllByIsDeletedFalseOrderByIdDesc(pageable);
        }
        return userRepository.searchByKeyword(keyword.trim(), pageable);
    }

    @Override
    public void toggleUserStatus(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng"));
        if ("ACTIVE".equalsIgnoreCase(user.getStatus())) {
            user.setStatus("INACTIVE");
        } else {
            user.setStatus("ACTIVE");
        }
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void createUserWithRole(User user) {
        if (userRepository.findByEmail(user.getEmail()) != null) {
            throw new IllegalArgumentException("Email đã được sử dụng");
        }

        Role role = roleRepository.findByName(user.getRoles().iterator().next().getName());
        if (role == null || !List.of("Admin", "Cashier", "Chef", "Manager").contains(role.getName())) {
            throw new IllegalArgumentException("Không được phép tạo người dùng với vai trò này");
        }

        String defaultPassword = "Staff1234@";
        user.setPassword(passwordEncoder.encode(defaultPassword));
        user.setStatus("ACTIVE");
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
user.setVerified(true);
        Set<Role> roles = new HashSet<>();
        roles.add(role);
        user.setRoles(roles);

        userRepository.save(user);

        // Gửi email thông báo
        try {
            String subject = "Thông tin tài khoản PizzaShop";
            String body = String.format("""
                    Xin chào %s %s,

                    Tài khoản của bạn đã được tạo thành công trên hệ thống PizzaShop.

                    📨 Email đăng nhập: %s
                    🔑 Mật khẩu tạm thời: Staff1234@

                    ⚠️ Vui lòng đổi mật khẩu ngay sau khi đăng nhập và KHÔNG chia sẻ thông tin này cho bất kỳ ai.

                    Trân trọng,
                    PizzaShop Team
                    """, user.getFirstname(), user.getLastname(), user.getEmail());

            emailService.sendSimpleMessage(user.getEmail(), subject, body);
            log.info("Đã gửi email tạo tài khoản cho {}", user.getEmail());
        } catch (Exception e) {
            log.error("Không thể gửi email đến {}: {}", user.getEmail(), e.getMessage());
        }
    }
}
