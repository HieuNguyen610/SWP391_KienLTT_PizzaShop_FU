package com.swp.pizzashop.controller;

import com.swp.pizzashop.model.User;
import com.swp.pizzashop.service.impl.UserServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ProfileController {

    private final UserServiceImpl userServiceImpl;

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

    @GetMapping("/profile/edit")
    public String editProfile(@ModelAttribute("currentUser") User currentUser, Model model) {
        log.debug("GET /profile/edit for user={}", currentUser != null ? currentUser.getEmail() : null);

        model.addAttribute("user", currentUser);
        return "dashboard_info_edit";
    }

    @PostMapping("/profile/edit")
    public String updateProfile(@ModelAttribute("user") User userForm, Principal principal) {
        String email = principal.getName();
        User user = userServiceImpl.findByEmail(email);

        user.setFirstname(userForm.getFirstname());
        user.setLastname(userForm.getLastname());
        user.setPhone(userForm.getPhone());

        userServiceImpl.updateUser(user);
        return "redirect:/profile?success";
    }
}
