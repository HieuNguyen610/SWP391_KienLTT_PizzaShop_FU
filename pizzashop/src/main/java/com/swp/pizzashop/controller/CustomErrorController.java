package com.swp.pizzashop.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        model.addAttribute("status", request.getAttribute("jakarta.servlet.error.status_code"));
        model.addAttribute("message", request.getAttribute("jakarta.servlet.error.message"));
        return "error";
    }
}

