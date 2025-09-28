package com.swp.pizzashop.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    private final AuthenticationManager authenticationManager;
    private final MessageService messageService;

    @GetMapping("/login")
    public String showLoginForm(Model model, @RequestParam(value = "error", required = false) String error,
                               @RequestParam(value = "logout", required = false) String logout,
                               @RequestParam(value = "reset", required = false) String reset) {
        log.debug("GET /login called, error={}, logout={}, reset={}", error, logout, reset);
        model.addAttribute("loginForm", new LoginForm());
        if (error != null) {
            model.addAttribute("error", messageService.get(SystemMessageCode.MSG08));
        }
        if (logout != null) {
            model.addAttribute("success", messageService.get(SystemMessageCode.MSG14));
        }
        if (reset != null) {
            model.addAttribute("success", messageService.get(SystemMessageCode.MSG12));
        }
        return "login";
    }


    @PostMapping("/do-login")
    public String doLogin(@Valid @ModelAttribute("loginForm") LoginForm loginForm,
                         BindingResult bindingResult,
                         Model model,
                         HttpServletRequest request,
                         HttpServletResponse response) {
        log.debug("POST /do-login attempt for email={}", loginForm != null ? loginForm.getEmail() : null);
        if (bindingResult.hasErrors()) {
            log.debug("Login validation errors: {}", bindingResult.getAllErrors());
            model.addAttribute("errors", bindingResult.getAllErrors());
            return "login";
        }
        try {
            UsernamePasswordAuthenticationToken authRequest = null;
            if (loginForm != null) {
                authRequest = new UsernamePasswordAuthenticationToken(loginForm.getEmail(), loginForm.getPassword());
                log.debug("Authenticating UsernamePasswordAuthenticationToken for {}", loginForm.getEmail());
            }
            Authentication authentication = authenticationManager.authenticate(authRequest);

            // Persist the authentication into SecurityContext and HTTP session explicitly
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);
            new HttpSessionSecurityContextRepository().saveContext(context, request, response);
            log.info("Login success for {} with authorities={}, sessionId={}",
                    authentication.getName(), authentication.getAuthorities(),
                    request.getSession(false) != null ? request.getSession(false).getId() : null);

            return "redirect:/profile";
        } catch (DisabledException de) {
            log.warn("Login disabled for {}: {}", loginForm.getEmail(), de.getMessage());
            model.addAttribute("error", messageService.get(SystemMessageCode.MSG15));
            return "login";
        } catch (AuthenticationException ex) {
            log.warn("Login failed for {}: {}", loginForm.getEmail(), ex.getMessage());
            model.addAttribute("error", messageService.get(SystemMessageCode.MSG08));
            return "login";
        }
    }
}
