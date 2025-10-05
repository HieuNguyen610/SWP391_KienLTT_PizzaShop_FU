package com.swp.pizzashop.controller;

import com.swp.pizzashop.dto.AddressForm;
import com.swp.pizzashop.model.User;
import com.swp.pizzashop.service.AddressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@Slf4j
public class AddressController {

    private final AddressService addressService;

    // Delivery Address Book page
    @GetMapping({"/address", "/address-book"})
    public String addressBook(@ModelAttribute("currentUser") User currentUser, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            log.debug("GET /address with auth principal={}, authenticated={}, authorities={}",
                    auth.getName(), auth.isAuthenticated(), auth.getAuthorities());
        }
        model.addAttribute("user", currentUser);
        model.addAttribute("addresses", addressService.findByUser(currentUser));
        // Add usage counters for UX
        long used = addressService.countActiveByUser(currentUser);
        int max = addressService.getMaxAddressesPerUser();
        model.addAttribute("addrUsed", used);
        model.addAttribute("addrMax", max);
        return "profile-address";
    }

    // Create new delivery address
    @PostMapping("/create-address")
    public String createAddress(@ModelAttribute("currentUser") User currentUser,
                                @Valid @ModelAttribute AddressForm form,
                                BindingResult binding,
                                RedirectAttributes ra) {
        if (currentUser == null) {
            return "redirect:/login";
        }
        if (binding.hasErrors()) {
            String msg = binding.getFieldErrors().stream().findFirst()
                    .map(e -> e.getDefaultMessage()).orElse("Invalid address data.");
            ra.addFlashAttribute("addrError", msg);
            return "redirect:/address";
        }
        try {
            addressService.createAddress(currentUser, form);
            ra.addFlashAttribute("addrSuccess", "Address added successfully.");
        } catch (IllegalArgumentException ex) {
            ra.addFlashAttribute("addrError", ex.getMessage());
        } catch (Exception ex) {
            log.warn("Failed to create address for user {}: {}", currentUser.getEmail(), ex.getMessage());
            ra.addFlashAttribute("addrError", "Unable to add address. Please check inputs and try again.");
        }
        return "redirect:/address";
    }

    // Edit address form
    @GetMapping("/address/{id}/edit")
    public String editAddressForm(@ModelAttribute("currentUser") User currentUser,
                                  @PathVariable Long id,
                                  Model model,
                                  RedirectAttributes ra) {
        if (currentUser == null) return "redirect:/login";
        var opt = addressService.findByIdForUser(id, currentUser);
        if (opt.isEmpty()) {
            ra.addFlashAttribute("addrError", "Address not found or access denied.");
            return "redirect:/address";
        }
        var addr = opt.get();
        // Parse addressLine => number + street (best-effort)
        String number = "";
        String street = addr.getAddressLine() != null ? addr.getAddressLine() : "";
        int idx = street.indexOf(",");
        if (idx > -1) {
            number = street.substring(0, idx).trim();
            street = street.substring(idx + 1).trim();
        }
        model.addAttribute("user", currentUser);
        model.addAttribute("addr", addr);
        model.addAttribute("prefill_number", number);
        model.addAttribute("prefill_street", street);
        return "profile-address-edit";
    }

    // Update address
    @PostMapping("/address/{id}/edit")
    public String updateAddress(@ModelAttribute("currentUser") User currentUser,
                                @PathVariable Long id,
                                @Valid @ModelAttribute AddressForm form,
                                BindingResult binding,
                                RedirectAttributes ra) {
        if (currentUser == null) return "redirect:/login";
        if (binding.hasErrors()) {
            String msg = binding.getFieldErrors().stream().findFirst()
                    .map(e -> e.getDefaultMessage()).orElse("Invalid address data.");
            ra.addFlashAttribute("addrError", msg);
            return "redirect:/address/" + id + "/edit";
        }
        try {
            addressService.updateAddress(currentUser, id, form);
            ra.addFlashAttribute("addrSuccess", "Address updated successfully.");
            return "redirect:/address";
        } catch (IllegalArgumentException ex) {
            // Show specific validation or not-found message
            ra.addFlashAttribute("addrError", ex.getMessage());
            return "redirect:/address/" + id + "/edit";
        } catch (Exception ex) {
            log.warn("Failed to update address {} for user {}: {}", id, currentUser.getEmail(), ex.getMessage());
            ra.addFlashAttribute("addrError", "Unable to update address. Please try again.");
            return "redirect:/address/" + id + "/edit";
        }
    }
}
