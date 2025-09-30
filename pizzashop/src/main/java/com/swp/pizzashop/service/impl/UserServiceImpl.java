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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    @Override
    @Transactional
    public Optional<String> initiatePasswordReset(String email) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            log.debug("Password reset requested for non-existing email: {}", email);
            return Optional.empty();
        }
        // Invalidate existing tokens for this user (optional cleanup policy)
        passwordResetTokenRepository.deleteByUserId(user.getId());

        String rawToken = generateToken();
        PasswordResetToken tokenEntity = PasswordResetToken.builder()
                .user(user)
                .token(rawToken)
                .expiresAt(LocalDateTime.now().plusMinutes(EXPIRY_MINUTES))
                .used(false)
                .build();
        passwordResetTokenRepository.save(tokenEntity);
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
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        prt.setUsed(true);
        passwordResetTokenRepository.save(prt);
        // Optionally delete other tokens for user
        return true;
    }
}
