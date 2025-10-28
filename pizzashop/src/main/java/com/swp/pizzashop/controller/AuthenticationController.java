package com.swp.pizzashop.controller;

import com.swp.pizzashop.service.EmailService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.swp.pizzashop.form.RegisterForm;
import com.swp.pizzashop.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.swp.pizzashop.form.LoginForm;
import com.swp.pizzashop.messages.MessageService;
import com.swp.pizzashop.messages.SystemMessageCode;

@Controller
@Slf4j
@RequiredArgsConstructor
public class AuthenticationController {

    @Autowired
    private EmailService emailService;

    private final AuthenticationManager authenticationManager;

    private final MessageService messageService;
    private final MessageService messageService; // AuthenticationManager removed (handled by Spring Security filter chain)

    @Autowired
    private UserService userService;

    @GetMapping("/login")
    public String showLoginForm(Model model,
                               @RequestParam(value = "errorCode", required = false) String errorCode,
                               @RequestParam(value = "logout", required = false) String logout,
                               @RequestParam(value = "reset", required = false) String reset) {
        log.debug("GET /login called, errorCode={}, logout={}, reset={}", errorCode, logout, reset);
        if (!model.containsAttribute("loginForm")) {
            model.addAttribute("loginForm", new LoginForm());
        }
        if (errorCode != null) {
            try {
                SystemMessageCode code = SystemMessageCode.valueOf(errorCode);
                model.addAttribute("error", messageService.get(code));
            } catch (IllegalArgumentException ex) {
                model.addAttribute("error", messageService.get(SystemMessageCode.MSG08));
            }
        }
        if (logout != null) {
            model.addAttribute("success", messageService.get(SystemMessageCode.MSG14));
        }
        if (reset != null) {
            model.addAttribute("success", messageService.get(SystemMessageCode.MSG12));
        }
        return "login";
    }

    @GetMapping("/register")
    public String showRegisterPage(Model model) {
        model.addAttribute("registerForm", new RegisterForm());
        return "sign_up";
    }

    @PostMapping("/do-register")
    public String doRegister(@Valid @ModelAttribute("registerForm") RegisterForm registerForm,
                             BindingResult bindingResult,
                             Model model,
                             HttpServletRequest request) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("errors", bindingResult.getAllErrors());
            return "sign_up";
        }

        if (!registerForm.getPassword().equals(registerForm.getConfirmPassword())) {
            model.addAttribute("errorEqualPassWord", "Password and Confirm Password do not match");
            return "sign_up";
        }

        try {
            userService.registerUser(registerForm);
            // Gửi OTP qua email
            String otp = emailService.sendOtpEmail(registerForm.getEmail());

            // Lưu OTP tạm vào session
            request.getSession().setAttribute("otp", otp);
            request.getSession().setAttribute("email", registerForm.getEmail());
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "sign_up";
        }

        return "redirect:/verify-otp";
    }

}
