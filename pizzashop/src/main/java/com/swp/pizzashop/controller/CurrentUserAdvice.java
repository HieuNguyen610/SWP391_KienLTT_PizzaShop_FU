package com.swp.pizzashop.controller;

import com.swp.pizzashop.model.User;
import com.swp.pizzashop.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice(basePackages = "com.swp.pizzashop.controller")
@RequiredArgsConstructor
@Slf4j
public class CurrentUserAdvice {

    private final UserRepository userRepository;

    @ModelAttribute("currentUser")
    public User currentUser(Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return null;
            }
            String email = authentication.getName();
            return userRepository.findByEmail(email);
        } catch (Exception ex) {
            log.debug("Unable to resolve current user: {}", ex.getMessage());
            return null;
        }
    }
}

