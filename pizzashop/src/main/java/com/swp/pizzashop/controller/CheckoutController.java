package com.swp.pizzashop.controller;

import com.swp.pizzashop.dto.CartItemDto;
import com.swp.pizzashop.model.Address;
import com.swp.pizzashop.model.Cart;
import com.swp.pizzashop.model.CartItem;
import com.swp.pizzashop.model.User;
import com.swp.pizzashop.repository.UserRepository;
import com.swp.pizzashop.service.AddressService;
import com.swp.pizzashop.service.CartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@Slf4j
public class CheckoutController {

    private final CartService cartService;
    private final UserRepository userRepository;
    private final AddressService addressService;

    @Value("${stripe.publishable-key}")
    private String publishableKey;

    @GetMapping("/checkout")
    public String checkout(Authentication authentication, Model model, RedirectAttributes ra) {
        if (authentication == null || !authentication.isAuthenticated()) {
            ra.addFlashAttribute("error", "Vui lòng đăng nhập để tiếp tục thanh toán");
            return "redirect:/login";
        }
        String email = authentication.getName();
        User user = userRepository.findByEmail(email);
        if (user == null) {
            ra.addFlashAttribute("error", "User not found");
            return "redirect:/login";
        }
        Address address = addressService.findDefaultByUser(user);

        Cart cart = cartService.getOrCreateActiveCart(user.getId());
        if (cart == null || cart.getItems() == null || cart.getItems().isEmpty()) {
            ra.addFlashAttribute("error", "The cart is empty");
            return "redirect:/cart";
        }

        BigDecimal subtotal = cart.getItems().stream()
                .map(i -> i.getPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal vat = subtotal.divide(BigDecimal.valueOf(10), 2, RoundingMode.HALF_UP);   // placeholder for shipping
        BigDecimal total = subtotal.add(vat);

        model.addAttribute("cart", cart);
        model.addAttribute("items", cart.getItems());
        model.addAttribute("subtotal", subtotal);
        model.addAttribute("vat", vat);
        model.addAttribute("total", total);
        model.addAttribute("user", user);
        model.addAttribute("address", address);

        return "checkout";
    }

    @GetMapping("/checkout/pay")
    public String paygate(Authentication authentication, Model model, RedirectAttributes ra) {
        if (authentication == null || !authentication.isAuthenticated()) {
            ra.addFlashAttribute("error", "Please login to proceed to payment");
            return "redirect:/login";
        }
        String email = authentication.getName();
        User user = userRepository.findByEmail(email);
        if (user == null) {
            ra.addFlashAttribute("error", "User not found");
            return "redirect:/login";
        }
        Cart cart = cartService.getOrCreateActiveCart(user.getId());
        if (cart == null || cart.getItems() == null || cart.getItems().isEmpty()) {
            ra.addFlashAttribute("error", "The cart is empty");
            return "redirect:/cart";
        }
        BigDecimal subtotal = cart.getItems().stream()
                .map(i -> i.getPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Convert CartItem -> CartItemDto for JSON serialization in Thymeleaf inline JS
        List<CartItemDto> cartDto = cart.getItems().stream().map(i -> {
            CartItemDto dto = new CartItemDto();
            CartItem ci = i;
            // Prefer food name if available, otherwise fallback
            dto.setName(ci.getFood() != null ? ci.getFood().getName() : ("Item-" + ci.getId()));
            dto.setUnitPrice(ci.getPrice());
            dto.setQuantity(ci.getQuantity());
            // default currency; change if you support multi-currency
            dto.setCurrency("usd");
            return dto;
        }).collect(Collectors.toList());

        BigDecimal vat = subtotal.divide(BigDecimal.valueOf(10), 2, RoundingMode.HALF_UP);   // placeholder for shipping
        BigDecimal total = subtotal.add(vat);

        model.addAttribute("cart", cartDto);
        model.addAttribute("items", cart.getItems());
        model.addAttribute("subtotal", subtotal);
        model.addAttribute("vat", vat);
        model.addAttribute("total", total);
        model.addAttribute("user", user);
        model.addAttribute("address", addressService.findDefaultByUser(user));

        model.addAttribute("merchantName", "PIZZA SHOP");
        model.addAttribute("orderRef", UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase());
        model.addAttribute("stripePublishableKey", publishableKey);
        return "paygate";
    }
}

