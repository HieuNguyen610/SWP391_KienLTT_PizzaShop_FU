package com.swp.pizzashop.controller;


import com.swp.pizzashop.form.LoginForm;
import com.swp.pizzashop.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Random;

@Controller
public class EmailController {

    @Autowired
    private UserService userService;

    @GetMapping("/verify-otp")
    public String showOtpPage(HttpServletRequest request, Model model,
                              @ModelAttribute("otp") String otp,
                              @ModelAttribute("email") String email) {
        request.getSession().setAttribute("otp", otp);
        request.getSession().setAttribute("email", email);
        return "verify_otp";

    }

    @PostMapping("/verify-otp")
    public String verifyOtp(@RequestParam("otp") String inputOtp,
                            HttpServletRequest request,
                            Model model) {
        String sessionOtp = (String) request.getSession().getAttribute("otp");
        String email = (String) request.getSession().getAttribute("email");

        if (sessionOtp == null || email == null) {
            model.addAttribute("error", "Session expired. Please register again.");
            return "verify_otp";
        }

        if (!sessionOtp.equals(inputOtp)) {
            model.addAttribute("error", "Invalid OTP. Please try again.");
            return "verify_otp";
        }

        userService.activateUser(email);
        request.getSession().removeAttribute("otp");
        request.getSession().removeAttribute("email");

        model.addAttribute("loginForm", new LoginForm());
        model.addAttribute("success", "Account verified successfully! You can now log in.");
        return "login";
    }
}
