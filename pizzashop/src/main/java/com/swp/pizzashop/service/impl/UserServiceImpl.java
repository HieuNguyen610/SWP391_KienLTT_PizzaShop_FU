package com.swp.pizzashop.service.impl;

import com.swp.pizzashop.model.PasswordResetToken;
import com.swp.pizzashop.model.User;
import com.swp.pizzashop.repository.PasswordResetTokenRepository;
import com.swp.pizzashop.repository.UserRepository;
import com.swp.pizzashop.service.UserService;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

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
}
