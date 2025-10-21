package com.swp.pizzashop.controller;


import com.swp.pizzashop.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Random;

@Controller
public class EmailController {

    @Autowired
    private UserService userService;

    @GetMapping("/verify-otp")
    public String showOtpPage() {
        return "verify_otp"; // Tên file .html trong templates
    }

    @PostMapping("/verify-otp")
    public String verifyOtp(@RequestParam("otp") String userOtp, HttpServletRequest request, Model model) {
        String sessionOtp = (String) request.getSession().getAttribute("otp");
        String email = (String) request.getSession().getAttribute("email");

        if (sessionOtp == null || !sessionOtp.equals(userOtp)) {
            model.addAttribute("error", "Mã OTP không hợp lệ, vui lòng thử lại!");
            return "verify_otp";
        }

        // Xác thực thành công -> cập nhật trạng thái user (active = true)
        userService.activateUser(email);

        // Xóa OTP khỏi session
        request.getSession().removeAttribute("otp");
        request.getSession().removeAttribute("email");

        model.addAttribute("success", "Xác thực thành công! Vui lòng đăng nhập.");
        return "redirect:/login";
    }
}
