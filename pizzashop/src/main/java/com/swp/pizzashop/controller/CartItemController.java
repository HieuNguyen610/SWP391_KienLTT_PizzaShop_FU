package com.swp.pizzashop.controller;

import com.swp.pizzashop.model.Cart;
import com.swp.pizzashop.model.CartItem;
import com.swp.pizzashop.model.User;
import com.swp.pizzashop.repository.CartItemRepository;
import com.swp.pizzashop.repository.FoodSizeRepository;
import com.swp.pizzashop.repository.UserRepository;
import com.swp.pizzashop.service.CartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Optional;

@Controller
@RequiredArgsConstructor
@Slf4j
public class CartItemController {

    private final CartService cartService;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final FoodSizeRepository foodSizeRepository;

    @PostMapping("/cart/item/{id}/update-qty")
    public String updateQuantity(@PathVariable("id") Long itemId,
                                 @RequestParam("quantity") Integer quantity,
                                 Authentication authentication,
                                 RedirectAttributes ra) {
        if (authentication == null || !authentication.isAuthenticated()) {
            ra.addFlashAttribute("error", "Vui lòng đăng nhập");
            return "redirect:/login";
        }
        if (quantity == null) quantity = 1;
        quantity = Math.max(0, quantity);

        String email = authentication.getName();
        User user = userRepository.findByEmail(email);
        if (user == null) {
            ra.addFlashAttribute("error", "Không tìm thấy người dùng");
            return "redirect:/login";
        }
        Cart cart = cartService.getOrCreateActiveCart(user.getId());

        Optional<CartItem> opt = cartItemRepository.findById(itemId);
        if (opt.isEmpty() || opt.get().getCart() == null || !opt.get().getCart().getId().equals(cart.getId())) {
            ra.addFlashAttribute("error", "Mục giỏ hàng không hợp lệ");
            return "redirect:/cart";
        }
        CartItem item = opt.get();
        if (quantity <= 0) {
            cartItemRepository.delete(item);
            ra.addFlashAttribute("success", "Đã xóa mặt hàng khỏi giỏ");
        } else {
            item.setQuantity(quantity);
            cartItemRepository.save(item);
            ra.addFlashAttribute("success", "Đã cập nhật số lượng");
        }
        return "redirect:/cart";
    }

    @PostMapping("/cart/item/{id}/remove")
    public String removeItem(@PathVariable("id") Long itemId,
                             Authentication authentication,
                             RedirectAttributes ra) {
        if (authentication == null || !authentication.isAuthenticated()) {
            ra.addFlashAttribute("error", "Vui lòng đăng nhập");
            return "redirect:/login";
        }
        String email = authentication.getName();
        User user = userRepository.findByEmail(email);
        if (user == null) {
            ra.addFlashAttribute("error", "Không tìm thấy người dùng");
            return "redirect:/login";
        }
        Cart cart = cartService.getOrCreateActiveCart(user.getId());
        Optional<CartItem> opt = cartItemRepository.findById(itemId);
        if (opt.isPresent() && opt.get().getCart() != null && opt.get().getCart().getId().equals(cart.getId())) {
            cartItemRepository.delete(opt.get());
            ra.addFlashAttribute("success", "Đã xóa mặt hàng khỏi giỏ");
        } else {
            ra.addFlashAttribute("error", "Mục giỏ hàng không hợp lệ");
        }
        return "redirect:/cart";
    }

    @PostMapping("/cart/item/{id}/edit")
    public String editItem(@PathVariable("id") Long itemId,
                           @RequestParam(value = "quantity", required = false) Integer quantity,
                           @RequestParam(value = "sizeId", required = false) Long sizeId,
                           @RequestParam(value = "notes", required = false) String notes,
                           Authentication authentication,
                           RedirectAttributes ra) {
        if (authentication == null || !authentication.isAuthenticated()) {
            ra.addFlashAttribute("error", "Vui lòng đăng nhập");
            return "redirect:/login";
        }
        String email = authentication.getName();
        User user = userRepository.findByEmail(email);
        if (user == null) {
            ra.addFlashAttribute("error", "Không tìm thấy người dùng");
            return "redirect:/login";
        }
        Cart cart = cartService.getOrCreateActiveCart(user.getId());
        Optional<CartItem> opt = cartItemRepository.findById(itemId);
        if (opt.isEmpty() || opt.get().getCart() == null || !opt.get().getCart().getId().equals(cart.getId())) {
            ra.addFlashAttribute("error", "Mục giỏ hàng không hợp lệ");
            return "redirect:/cart";
        }
        CartItem item = opt.get();

        int newQty = (quantity == null ? item.getQuantity() : quantity);
        if (newQty <= 0) {
            cartItemRepository.delete(item);
            ra.addFlashAttribute("success", "Đã xóa mặt hàng khỏi giỏ");
            return "redirect:/cart";
        }

        Long newSizeId = (sizeId == null ? item.getSizeId() : sizeId);
        if (!newSizeId.equals(item.getSizeId())) {
            var sizeOpt = foodSizeRepository.findById(newSizeId);
            if (sizeOpt.isEmpty() || sizeOpt.get().getFood() == null || !sizeOpt.get().getFood().getId().equals(item.getFood().getId())) {
                ra.addFlashAttribute("error", "Kích cỡ không hợp lệ cho món này");
                return "redirect:/cart";
            }
            item.setSizeId(newSizeId);
            item.setPrice(sizeOpt.get().getPrice());
        }

        item.setQuantity(newQty);
        item.setNotes(notes);
        cartItemRepository.save(item);
        ra.addFlashAttribute("success", "Đã cập nhật mặt hàng trong giỏ");
        return "redirect:/cart";
    }

    @GetMapping({"/cart/item/{id}/edit", "/cart/{id}/edit"})
    public String editItemForm(@PathVariable("id") Long itemId,
                               Authentication authentication,
                               RedirectAttributes ra,
                               Model model) {
        if (authentication == null || !authentication.isAuthenticated()) {
            ra.addFlashAttribute("error", "Vui lòng đăng nhập");
            return "redirect:/login";
        }
        String email = authentication.getName();
        User user = userRepository.findByEmail(email);
        if (user == null) {
            ra.addFlashAttribute("error", "Không tìm thấy người dùng");
            return "redirect:/login";
        }
        Cart cart = cartService.getOrCreateActiveCart(user.getId());
        Optional<CartItem> opt = cartItemRepository.findById(itemId);
        if (opt.isEmpty() || opt.get().getCart() == null || !opt.get().getCart().getId().equals(cart.getId())) {
            ra.addFlashAttribute("error", "Mục giỏ hàng không hợp lệ");
            return "redirect:/cart";
        }
        CartItem item = opt.get();
        model.addAttribute("item", item);
        model.addAttribute("sizes", foodSizeRepository.findByFoodIdAndIsDeletedFalseOrderByPriceAsc(item.getFood().getId()));
        return "cart-item-edit";
    }
}
