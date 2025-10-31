package com.swp.pizzashop.controller.admin;


import com.swp.pizzashop.model.User;
import com.swp.pizzashop.repository.RoleRepository;
import com.swp.pizzashop.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    private final RoleRepository roleRepository;
    @GetMapping
    public String listUsers(@RequestParam(value = "page", defaultValue = "0") int page,
                            @RequestParam(value = "q", required = false) String keyword,
                            Model model) {
        int pageSize = 8; // số lượng user mỗi trang
        Page<User> pageData = userService.findAllOrderedWithSearch(keyword, PageRequest.of(page, pageSize));

        model.addAttribute("pageData", pageData);
        model.addAttribute("q", keyword);
        return "admin/users";
    }


    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("roles", roleRepository.findAll());
        model.addAttribute("activeSection", "users");
        return "admin/user-create";
    }

    @PostMapping("/create")
    public String createUser(@ModelAttribute User user, RedirectAttributes redirectAttributes) {

        try {
            if (userService.findByEmail(user.getEmail()) != null) {
                redirectAttributes.addFlashAttribute("errorMessage",
                        "Email này đã được đăng ký cho một tài khoản khác!");
                return "redirect:/admin/users/create";
            }
            userService.createUserWithRole(user);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Tạo người dùng thành công! Email thông tin tài khoản đã được gửi.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/update-status")
    public String toggleStatus(@RequestParam("id") Long id) {
        userService.toggleUserStatus(id);
        return "redirect:/admin/users";
    }


}