package com.swp.pizzashop.controller.admin;

import com.swp.pizzashop.form.ToppingForm;
import com.swp.pizzashop.model.Topping;
import com.swp.pizzashop.service.ToppingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
@RequestMapping("/admin/toppings")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('Admin')")
public class ToppingAdminController {

    private final ToppingService toppingService;

    @GetMapping
    public String list(@RequestParam(value = "page", defaultValue = "0") int page,
                       @RequestParam(value = "q", required = false) String q,
                       @RequestParam(value = "sort", defaultValue = "name") String sort,
                       @RequestParam(value = "dir", defaultValue = "asc") String dir,
                       @RequestParam(value = "msg", required = false) String msg,
                       @RequestParam(value = "err", required = false) String err,
                       Model model) {
        model.addAttribute("activeSection", "toppings");
        if (msg != null) model.addAttribute("success", msg);
        if (err != null) model.addAttribute("error", err);

        int pageIndex = Math.max(page, 0);
        String sortField = switch (sort) {
            case "id" -> "id";
            case "price" -> "price";
            case "active" -> "isActive";
            default -> "name";
        };
        String safeDir = (dir != null && dir.equalsIgnoreCase("desc")) ? "desc" : "asc";
        Sort.Direction direction = safeDir.equals("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(pageIndex, 10, Sort.by(direction, sortField));

        Page<Topping> pageData = toppingService.findPage(q, pageable);
        model.addAttribute("q", q);
        model.addAttribute("sort", sort);
        model.addAttribute("dir", safeDir);
        model.addAttribute("pageData", pageData);
        model.addAttribute("toppings", pageData.getContent());
        return "admin/toppings";
    }

    @GetMapping("/create")
    public String showCreate(Model model, @RequestParam(value = "err", required = false) String err) {
        model.addAttribute("activeSection", "toppings");
        model.addAttribute("toppingForm", new ToppingForm());
        if (err != null) model.addAttribute("error", err);
        return "admin/topping-create";
    }

    @PostMapping("/create")
    public String handleCreate(@Valid @ModelAttribute("toppingForm") ToppingForm form,
                               BindingResult result,
                               Model model,
                               RedirectAttributes ra) {
        Topping existing = toppingService.findByNameIgnoreCaseAndIsDeletedFalse(form.getName());
        if (existing != null) {
            result.rejectValue("name", "duplicate", "Topping already exists");
        }
        if (result.hasErrors()) {
            model.addAttribute("activeSection", "toppings");
            return "admin/topping-create";
        }
        try {
            toppingService.create(form);
            ra.addAttribute("msg", "Topping created");
        } catch (Exception ex) {
            log.error("Create topping failed", ex);
            ra.addAttribute("err", ex.getMessage());
        }
        return "redirect:/admin/toppings";
    }

    @GetMapping("/{id}/edit")
    public String edit(@PathVariable Long id, Model model, RedirectAttributes ra) {
        Optional<Topping> opt = toppingService.findById(id);
        if (opt.isEmpty()) {
            ra.addAttribute("err", "Topping not found");
            return "redirect:/admin/toppings";
        }
        Topping t = opt.get();
        ToppingForm form = new ToppingForm();
        form.setName(t.getName());
        form.setDescription(t.getDescription());
        form.setImageUrl(t.getImageUrl());
        form.setPrice(t.getPrice());
        form.setActive(t.isActive());
        model.addAttribute("toppingForm", form);
        model.addAttribute("toppingId", id);
        model.addAttribute("topping", t);
        model.addAttribute("activeSection", "toppings");
        return "admin/topping-edit";
    }

    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("toppingForm") ToppingForm form,
                         BindingResult result,
                         RedirectAttributes ra,
                         Model model) {
        Topping existingByName = toppingService.findByNameIgnoreCaseAndIsDeletedFalse(form.getName());
        if (existingByName != null && !existingByName.getId().equals(id)) {
            result.rejectValue("name", "duplicate", "Topping name already in use");
        }
        if (result.hasErrors()) {
            model.addAttribute("toppingId", id);
            model.addAttribute("activeSection", "toppings");
            return "admin/topping-edit";
        }
        try {
            toppingService.update(id, form);
            ra.addAttribute("msg", "Topping updated");
        } catch (Exception ex) {
            log.error("Update topping failed", ex);
            ra.addAttribute("err", ex.getMessage());
        }
        return "redirect:/admin/toppings";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        try {
            toppingService.softDelete(id);
            ra.addAttribute("msg", "Topping deleted");
        } catch (Exception ex) {
            log.error("Delete topping failed", ex);
            ra.addAttribute("err", ex.getMessage());
        }
        return "redirect:/admin/toppings";
    }
}
