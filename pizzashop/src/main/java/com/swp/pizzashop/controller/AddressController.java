package com.swp.pizzashop.controller;

import com.swp.pizzashop.form.AddressForm;
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
        if (currentUser != null) {
            log.debug("Address book: currentUser id={}, email={}", currentUser.getId(), currentUser.getEmail());
        } else {
            log.debug("Address book: currentUser is null");
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
        log.debug("Create address: user id={}, email={}", currentUser.getId(), currentUser.getEmail());
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
    @GetMapping("/address/{addressId}/edit")
    public String editAddressForm(@ModelAttribute("currentUser") User currentUser,
                                  @PathVariable("addressId") Long addressId,
                                  Model model,
                                  RedirectAttributes ra) {
        if (currentUser == null) return "redirect:/login";
        log.debug("Edit address form: user id={}, email={}, addressId={}", currentUser.getId(), currentUser.getEmail(), addressId);
        var opt = addressService.findByIdForUser(addressId, currentUser);
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
    @PostMapping("/address/{addressId}/edit")
    public String updateAddress(@ModelAttribute("currentUser") User currentUser,
                                @PathVariable("addressId") Long addressId,
                                @Valid @ModelAttribute AddressForm form,
                                BindingResult binding,
                                RedirectAttributes ra) {
        if (currentUser == null) return "redirect:/login";
        log.debug("Update address: user id={}, email={}, addressId={}", currentUser.getId(), currentUser.getEmail(), addressId);
        if (binding.hasErrors()) {
            String msg = binding.getFieldErrors().stream().findFirst()
                    .map(e -> e.getDefaultMessage()).orElse("Invalid address data.");
            ra.addFlashAttribute("addrError", msg);
            return "redirect:/address/" + addressId + "/edit";
        }
        try {
            addressService.updateAddress(currentUser, addressId, form);
            ra.addFlashAttribute("addrSuccess", "Address updated successfully.");
            return "redirect:/address";
        } catch (IllegalArgumentException ex) {
            // Show specific validation or not-found message
            ra.addFlashAttribute("addrError", ex.getMessage());
            return "redirect:/address/" + addressId + "/edit";
        } catch (Exception ex) {
            log.warn("Failed to update address {} for user {}: {}", addressId, currentUser.getEmail(), ex.getMessage());
            ra.addFlashAttribute("addrError", "Unable to update address. Please try again.");
            return "redirect:/address/" + addressId + "/edit";
        }
    }

    // Delete address (soft delete)
    @PostMapping("/address/{addressId}/delete")
    public String deleteAddress(@ModelAttribute("currentUser") User currentUser,
                                @PathVariable("addressId") Long addressId,
                                RedirectAttributes ra) {
        if (currentUser == null) return "redirect:/login";
        log.debug("Delete address: user id={}, email={}, addressId={}", currentUser.getId(), currentUser.getEmail(), addressId);
        try {
            addressService.deleteAddress(currentUser, addressId);
            ra.addFlashAttribute("addrSuccess", "Address deleted.");
        } catch (IllegalArgumentException ex) {
            ra.addFlashAttribute("addrError", ex.getMessage());
        } catch (Exception ex) {
            log.warn("Failed to delete address {} for user {}: {}", addressId, currentUser != null ? currentUser.getEmail() : "<null>", ex.getMessage());
            ra.addFlashAttribute("addrError", "Unable to delete address. Please try again.");
        }
        return "redirect:/address";
    }
}
