package com.swp.pizzashop.controller;

import com.swp.pizzashop.model.User;
import com.swp.pizzashop.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ProfileController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/profile")
    public String profile(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            log.debug("GET /profile with auth principal={}, authenticated={}, authorities={}",
                    auth.getName(), auth.isAuthenticated(), auth.getAuthorities());
        } else {
            log.debug("GET /profile with no authentication in context");
        }
        User user = getCurrentUser();
        log.debug("Loaded current user: {}", user != null ? user.getEmail() : null);
        model.addAttribute("user", user);
        return "profile";
    }

    private User getCurrentUser() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || auth.getName() == null) return null;
            String email = auth.getName();
            log.debug("Resolving user by email={} from repository", email);
            return userRepository.findByEmail(email);
        } catch (Exception e) {
            log.error("Failed to load current user", e);
            return null;
        }
    }
}
