package com.swp.pizzashop.controller;

import com.swp.pizzashop.form.PasswordResetRequestForm;
import com.swp.pizzashop.form.ResetPasswordForm;
import com.swp.pizzashop.messages.MessageService;
import com.swp.pizzashop.messages.SystemMessageCode;
import com.swp.pizzashop.service.EmailService;
import com.swp.pizzashop.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@Slf4j
public class PasswordResetController {

    private final UserService userService;
    private final EmailService emailService;
    private final MessageService messageService;

    @Value("${app.frontend.base-url:}")
    private String configuredBaseUrl;

    // Step 1/2: Display request form
    @GetMapping("/forgot-password")
    public String forgotPasswordForm(Model model) {
        if (!model.containsAttribute("passwordResetRequestForm")) {
            model.addAttribute("passwordResetRequestForm", new PasswordResetRequestForm());
        }
        return "forgot-password";
    }

    // Step 3-8: Handle submit, generate token, send email, always show success (avoid enumeration)
    @PostMapping("/forgot-password")
    public String handleForgot(@Valid @ModelAttribute("passwordResetRequestForm") PasswordResetRequestForm form,
                               BindingResult bindingResult,
                               HttpServletRequest request,
                               Model model) {
        if (bindingResult.hasErrors()) {
            return "forgot-password";
        }
        String email = form.getEmail();
        var tokenOpt = userService.initiatePasswordReset(email);
        if (tokenOpt.isEmpty()) {
            // Email not found: show explicit error per new requirement (MSG17)
            model.addAttribute("error", messageService.get(SystemMessageCode.MSG17, email));
            return "forgot-password";
        }
        String token = tokenOpt.get();
        String link = buildAbsoluteLink(request, "/reset-password?token=" + token);
        log.debug("Generated password reset link for {} -> {}", email, link);
        var user = userService.findByEmail(email);
        if (user != null) {
            emailService.sendPasswordResetEmail(user, link);
        }
        model.addAttribute("success", messageService.get(SystemMessageCode.MSG11, email));
        model.addAttribute("passwordResetRequestForm", new PasswordResetRequestForm());
        return "forgot-password";
    }

    // Step: Open link & validate token
    @GetMapping("/reset-password")
    public String resetForm(@RequestParam(name = "token", required = false) String token,
                            Model model) {
        if (token == null || token.isBlank()) {
            model.addAttribute("error", messageService.get(SystemMessageCode.MSG13));
            return "reset-password-invalid";
        }
        // Service already filters out used/expired tokens; presence == valid
        if (userService.findByResetPasswordToken(token).isEmpty()) {
            model.addAttribute("error", messageService.get(SystemMessageCode.MSG13));
            return "reset-password-invalid";
        }
        ResetPasswordForm form = new ResetPasswordForm();
        form.setToken(token);
        model.addAttribute("resetPasswordForm", form);
        return "reset-password";
    }

    // Step: Submit new password
    @PostMapping("/reset-password")
    public String handleReset(@Valid @ModelAttribute("resetPasswordForm") ResetPasswordForm form,
                              BindingResult bindingResult,
                              Model model) {
        if (bindingResult.hasErrors()) {
            return "reset-password";
        }
        if (!form.getPassword().equals(form.getConfirmPassword())) {
            model.addAttribute("error", messageService.get(SystemMessageCode.MSG16));
            return "reset-password";
        }
        boolean ok = userService.resetPassword(form.getToken(), form.getPassword());
        if (!ok) {
            model.addAttribute("error", messageService.get(SystemMessageCode.MSG13));
            return "reset-password-invalid";
        }
        // Redirect triggers login page success message (?reset handled in AuthenticationController)
        return "redirect:/login?reset";
    }

    private String buildAbsoluteLink(HttpServletRequest request, String relativePath) {
        if (relativePath.startsWith("http")) return relativePath;
        String base = configuredBaseUrl;
        if (base == null || base.isBlank()) {
            String scheme = request.getScheme();
            String host = request.getServerName();
            int port = request.getServerPort();
            boolean standard = (scheme.equals("http") && port == 80) || (scheme.equals("https") && port == 443);
            base = scheme + "://" + host + (standard ? "" : ":" + port);
        }
        return base.replaceAll("/$", "") + relativePath;
    }
}
