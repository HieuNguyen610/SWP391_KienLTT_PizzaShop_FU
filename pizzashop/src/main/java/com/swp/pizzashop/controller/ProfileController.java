package com.swp.pizzashop.controller;

import com.swp.pizzashop.model.User;
import com.swp.pizzashop.service.UserService;
import com.swp.pizzashop.service.ChangePasswordResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ProfileController {

    private final UserService userService;

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

    @PostMapping("/profile/change-password")
    public String changePassword(@ModelAttribute("currentUser") User currentUser,
                                 @RequestParam String oldPassword,
                                 @RequestParam String newPassword,
                                 @RequestParam String confirmPassword,
                                 Model model) {
        if (currentUser == null) {
            return "redirect:/login";
        }
        model.addAttribute("user", currentUser);
        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("pwdError", "New password and confirmation do not match.");
            return "profile";
        }
        ChangePasswordResult result = userService.changePassword(currentUser, oldPassword, newPassword);
        switch (result) {
            case SUCCESS -> model.addAttribute("pwdSuccess", "Password changed successfully.");
            case OLD_PASSWORD_INVALID -> model.addAttribute("pwdError", "Old password is incorrect.");
            case NEW_PASSWORD_SAME_AS_OLD -> model.addAttribute("pwdError", "New password must be different from the old password.");
            case WEAK_NEW_PASSWORD -> model.addAttribute("pwdError", "New password is too weak (minimum 8 characters).");
            default -> model.addAttribute("pwdError", "Unable to change password. Please try again.");
        }
        return "profile";
    }
}
