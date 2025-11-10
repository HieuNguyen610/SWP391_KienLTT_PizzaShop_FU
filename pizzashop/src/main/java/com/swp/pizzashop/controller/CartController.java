package com.swp.pizzashop.controller;

import com.swp.pizzashop.form.AddToCartForm;
import com.swp.pizzashop.model.Cart;
import com.swp.pizzashop.model.CartItem;
import com.swp.pizzashop.model.User;
import com.swp.pizzashop.repository.CartItemRepository;
import com.swp.pizzashop.repository.UserRepository;
import com.swp.pizzashop.service.CartService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
public class CartController {

    private final CartService cartService;
    private final UserRepository userRepository;
    private final CartItemRepository cartItemRepository;

    @GetMapping("/cart")
    public String viewCart(Authentication authentication,
                           RedirectAttributes ra,
                           Model model) {
        if (authentication == null || !authentication.isAuthenticated()) {
            ra.addFlashAttribute("error", "Please log in");
            return "redirect:/login";
        }
        String email = authentication.getName();
        User user = userRepository.findByEmail(email);
        if (user == null) {
            ra.addFlashAttribute("error", "User not found. Please log in");
            return "redirect:/login";
        }

        Cart cart = cartService.getOrCreateActiveCart(user.getId());
        List<CartItem> items = cartItemRepository.findByCartId(cart.getId());

        BigDecimal subtotal = items.stream()
                .map(i -> i.getPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("cart", cart);
        model.addAttribute("items", items);
        model.addAttribute("subtotal", subtotal);
        return "cart";
    }

    @GetMapping("/cart/add")
    public String addToCartGet(
            @RequestParam("foodId") Long foodId,
            @RequestParam(value = "sizeId", required = false) Long sizeId,
            @RequestParam(value = "quantity", required = false, defaultValue = "1") Integer quantity,
            @RequestParam(value = "notes", required = false) String notes,
            Authentication authentication,
            HttpServletRequest request,
            RedirectAttributes ra
    ) {
        String redirectTo = "/cart";

        if (authentication == null || !authentication.isAuthenticated()) {
            ra.addFlashAttribute("error", "Please log in");
            return "redirect:/login";
        }
        try {
            String email = authentication.getName();
            User user = userRepository.findByEmail(email);
            if (user == null) {
                ra.addFlashAttribute("error", "User not found. Please log in");
                return "redirect:/login";
            }
            AddToCartForm form = new AddToCartForm();
            form.setFoodId(foodId);
            form.setSizeId(sizeId != null ? sizeId : 1L);
            form.setQuantity(quantity != null && quantity > 0 ? quantity : 1);
            form.setNotes(notes);
            cartService.addToCart(user.getId(), form);
            ra.addFlashAttribute("success", "Added to cart");
        } catch (ResponseStatusException ex) {
            log.debug("Add to cart (GET) failed: status={}, reason={}", ex.getStatusCode(), ex.getReason());
            ra.addFlashAttribute("error", ex.getReason() != null ? ex.getReason() : "Cannot add to cart");
        } catch (Exception ex) {
            log.error("Unexpected error adding to cart (GET)", ex);
            ra.addFlashAttribute("error", "An error occurred while adding to cart");
        }
        return "redirect:" + redirectTo;
    }

    @PostMapping("/cart/add")
    public String addToCart(
            @Valid AddToCartForm form,
            BindingResult bindingResult,
            Authentication authentication,
            HttpServletRequest request,
            RedirectAttributes ra
    ) {
        String redirectTo = "/cart";

        if (authentication == null || !authentication.isAuthenticated()) {
            ra.addFlashAttribute("error", "Please log in");
            return "redirect:/login";
        }

        if (bindingResult.hasErrors()) {
            ra.addFlashAttribute("error", bindingResult.getAllErrors().stream()
                    .findFirst().map(e -> e.getDefaultMessage() != null ? e.getDefaultMessage() : "Invalid request")
                    .orElse("Invalid request"));
            return "redirect:" + redirectTo;
        }

        try {
            String email = authentication.getName();
            User user = userRepository.findByEmail(email);
            if (user == null) {
                ra.addFlashAttribute("error", "User not found. Please log in");
                return "redirect:/login";
            }

            cartService.addToCart(user.getId(), form);
            ra.addFlashAttribute("success", "Added to cart");
        } catch (ResponseStatusException ex) {
            HttpStatus status = (HttpStatus) ex.getStatusCode();
            log.debug("Add to cart failed: status={}, reason={}", status, ex.getReason());
            ra.addFlashAttribute("error", ex.getReason() != null ? ex.getReason() : "Cannot add to cart");
        } catch (Exception ex) {
            log.error("Unexpected error adding to cart", ex);
            ra.addFlashAttribute("error", "An error occurred while adding to cart");
        }

        return "redirect:" + redirectTo;
    }
}

