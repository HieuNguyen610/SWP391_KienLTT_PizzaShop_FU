package com.swp.pizzashop.controller;


import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@PreAuthorize("hasRole('Admin')")
public class AccountManagementController {

    @GetMapping("/admin/account-management")
    public String showAccountList(){
        return "chefs_details";
    }
}
