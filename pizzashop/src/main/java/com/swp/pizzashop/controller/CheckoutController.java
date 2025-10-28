package com.swp.pizzashop.controller;

import com.swp.pizzashop.model.Address;
import com.swp.pizzashop.model.Cart;
import com.swp.pizzashop.model.User;
import com.swp.pizzashop.repository.UserRepository;
import com.swp.pizzashop.service.AddressService;
import com.swp.pizzashop.service.CartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
@Slf4j
public class CheckoutController {

    private final CartService cartService;
    private final UserRepository userRepository;
    private final AddressService addressService;

    @GetMapping("/checkout")
    public String checkout(Authentication authentication, Model model, RedirectAttributes ra) {
        if (authentication == null || !authentication.isAuthenticated()) {
            ra.addFlashAttribute("error", "Vui lòng đăng nhập để tiếp tục thanh toán");
            return "redirect:/login";
        }
        String email = authentication.getName();
        User user = userRepository.findByEmail(email);
        if (user == null) {
            ra.addFlashAttribute("error", "Không tìm thấy người dùng");
            return "redirect:/login";
        }
        Address address = addressService.findDefaultByUser(user);

        Cart cart = cartService.getOrCreateActiveCart(user.getId());
        if (cart == null || cart.getItems() == null || cart.getItems().isEmpty()) {
            ra.addFlashAttribute("error", "Giỏ hàng trống");
            return "redirect:/cart";
        }

        BigDecimal subtotal = cart.getItems().stream()
                .map(i -> i.getPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal memberDiscount = BigDecimal.ZERO; // placeholder for membership discounts
        BigDecimal shippingFee = BigDecimal.ZERO;     // placeholder for shipping
        BigDecimal total = subtotal.subtract(memberDiscount).add(shippingFee);

        model.addAttribute("cart", cart);
        model.addAttribute("items", cart.getItems());
        model.addAttribute("subtotal", subtotal);
        model.addAttribute("memberDiscount", memberDiscount);
        model.addAttribute("shippingFee", shippingFee);
        model.addAttribute("total", total);
        model.addAttribute("user", user);
        model.addAttribute("address", address);

        return "checkout";
    }

    @GetMapping("/checkout/pay")
    public String paygate(Authentication authentication, Model model, RedirectAttributes ra) {
        if (authentication == null || !authentication.isAuthenticated()) {
            ra.addFlashAttribute("error", "Vui lòng đăng nhập để thanh toán");
            return "redirect:/login";
        }
        String email = authentication.getName();
        User user = userRepository.findByEmail(email);
        if (user == null) {
            ra.addFlashAttribute("error", "Không tìm thấy người dùng");
            return "redirect:/login";
        }
        Cart cart = cartService.getOrCreateActiveCart(user.getId());
        if (cart == null || cart.getItems() == null || cart.getItems().isEmpty()) {
            ra.addFlashAttribute("error", "Giỏ hàng trống");
            return "redirect:/cart";
        }
        BigDecimal subtotal = cart.getItems().stream()
                .map(i -> i.getPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("merchantName", "PIZZA HUT");
        model.addAttribute("orderRef", UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase());
        model.addAttribute("total", subtotal);
        model.addAttribute("userEmail", user.getEmail());
        return "paygate";
    }
}
