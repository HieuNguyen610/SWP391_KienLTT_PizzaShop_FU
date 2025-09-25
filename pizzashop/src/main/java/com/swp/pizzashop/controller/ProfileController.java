package com.swp.pizzashop.controller;

import com.swp.pizzashop.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ProfileController {

    @GetMapping("/profile")
    public String profile(@ModelAttribute("currentUser") User currentUser, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            log.debug("GET /profile with auth principal={}, authenticated={}, authorities={}",
                    auth.getName(), auth.isAuthenticated(), auth.getAuthorities());
        } else {
            log.debug("GET /profile with no authentication in context");
        }
        log.debug("Loaded current user (from @ModelAttribute): {}", currentUser != null ? currentUser.getEmail() : null);
        model.addAttribute("user", currentUser); // keep existing template variable name
        return "profile";
    }
}
