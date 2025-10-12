package com.swp.pizzashop.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.swp.pizzashop.form.LoginForm;
import com.swp.pizzashop.messages.MessageService;
import com.swp.pizzashop.messages.SystemMessageCode;

@Controller
@Slf4j
@RequiredArgsConstructor
public class AuthenticationController {

    private final MessageService messageService; // AuthenticationManager removed (handled by Spring Security filter chain)

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

    // POST /do-login handled entirely by Spring Security (formLogin + custom failureHandler)
}
