package com.swp.pizzashop.controller.admin;


import com.swp.pizzashop.model.User;
import com.swp.pizzashop.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/admin/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    @GetMapping
    public String listUsers(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "q", required = false) String q,
            Model model) {

        Pageable pageable = PageRequest.of(page, 10);
        Page<User> pageData = userService.findAllOrderedWithSearch(q, pageable);

        model.addAttribute("pageData", pageData);
        model.addAttribute("q", q);
        model.addAttribute("activeSection", "users");
        return "admin/users";
    }

    @PostMapping("/update-status")
    public String toggleStatus(@RequestParam("id") Long id) {
        userService.toggleUserStatus(id);
        return "redirect:/admin/users";
    }
}