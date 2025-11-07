package com.swp.pizzashop.controller;

import com.swp.pizzashop.form.EditProfileForm;
import com.swp.pizzashop.model.User;
import com.swp.pizzashop.service.impl.UserServiceImpl;
import jakarta.validation.Valid;
import com.swp.pizzashop.service.UserService;
import com.swp.pizzashop.service.ChangePasswordResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.security.Principal;
import org.springframework.web.bind.annotation.*;

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
        if (currentUser != null) {
            log.debug("Loaded current user (from @ModelAttribute): id={}, email={}", currentUser.getId(), currentUser.getEmail());
        } else {
            log.debug("Loaded current user (from @ModelAttribute): null");
        }
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

    @GetMapping("/profile/edit")
    public String editProfile(@ModelAttribute("currentUser") User currentUser, Model model) {
        if (currentUser == null) {
            return "redirect:/login";
        }

        EditProfileForm form = new EditProfileForm();
        form.setFirstName(currentUser.getFirstname());
        form.setLastName(currentUser.getLastname());
        form.setPhone(currentUser.getPhone());
        form.setEmail(currentUser.getEmail());

        model.addAttribute("editProfileForm", form);
        return "dashboard_info_edit";
    }

    @PostMapping("/profile/edit")
    public String updateProfile(@Valid @ModelAttribute("editProfileForm") EditProfileForm form,
                                BindingResult result,
                                Principal principal,
                                Model model) {
        if (result.hasErrors()) {
            log.warn("Validation errors: {}", result.getAllErrors());
            return "dashboard_info_edit";
        }

        String email = principal.getName();
        User user = userService.findByEmail(email);

        user.setFirstname(form.getFirstName());
        user.setLastname(form.getLastName());
        user.setPhone(form.getPhone());

        userService.updateUser(user);

        return "redirect:/profile?success";
    }
}
