package com.swp.pizzashop.service.impl;

import com.swp.pizzashop.model.User;
import com.swp.pizzashop.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from:noreply@pizzashop.local}")
    private String fromAddress;

    @Value("${app.frontend.base-url:http://localhost:8080}")
    private String baseUrl;

    @Override
    public void sendPasswordResetEmail(User user, String resetLink) {
        if (user == null || user.getEmail() == null) {
            log.warn("Attempted to send password reset email with null user or email");
            return;
        }
        // Normalize link: if relative (starts with /) prefix baseUrl
        String link = resetLink.startsWith("http") ? resetLink : baseUrl.replaceAll("/$", "") + resetLink;
        String subject = "Password Reset Request";
        String body = "Hello " + user.getFirstname() + ",\n\n" +
                "We received a request to reset your password. If you made this request, click the link below:\n\n" +
                link + "\n\n" +
                "This link will expire in 30 minutes. If you did NOT request a password reset, you can ignore this email." +
                "\n\nRegards,\nPizza Shop Support";
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(user.getEmail());
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            log.info("Password reset email queued to {}", user.getEmail());
        } catch (Exception ex) {
            // Log but do not propagate to avoid breaking the flow (user still sees success to prevent enumeration)
            log.error("Failed to send password reset email to {}: {}", user.getEmail(), ex.getMessage());
        }
        if (log.isDebugEnabled()) {
            log.debug("[EMAIL DEBUG]\nTO: {}\nSUBJECT: {}\nBODY:\n{}", user.getEmail(), subject, body);
        }
    }
}

