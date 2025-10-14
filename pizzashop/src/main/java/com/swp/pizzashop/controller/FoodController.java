package com.swp.pizzashop.controller;

import com.swp.pizzashop.form.FoodForm;
import com.swp.pizzashop.model.Food;
import com.swp.pizzashop.service.FoodCategoryService;
import com.swp.pizzashop.service.FoodService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
@RequestMapping("/admin/foods")
@RequiredArgsConstructor
@Slf4j
public class FoodController {

    private final FoodService foodService;
    private final FoodCategoryService categoryService;

    @GetMapping
    public String list(@RequestParam(value = "page", defaultValue = "0") int page,
                       @RequestParam(value = "q", required = false) String q,
                       @RequestParam(value = "sort", defaultValue = "name") String sort,
                       @RequestParam(value = "dir", defaultValue = "asc") String dir,
                       @RequestParam(value = "msg", required = false) String msg,
                       @RequestParam(value = "err", required = false) String err,
                       Model model) {
        model.addAttribute("activeSection", "foods");
        if (msg != null) model.addAttribute("success", msg);
        if (err != null) model.addAttribute("error", err);

        int pageIndex = Math.max(page, 0);
        // map UI sort keys to entity properties
        String sortField = switch (sort) {
            case "id" -> "id";
            case "price" -> "basePrice";
            case "active" -> "isActive";
            default -> "name";
        };
        String safeDir = (dir != null && dir.equalsIgnoreCase("desc")) ? "desc" : "asc";
        Sort.Direction direction = safeDir.equals("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(pageIndex, 10, Sort.by(direction, sortField));

        Page<Food> pageData = foodService.findPage(q, pageable);
        model.addAttribute("q", q);
        model.addAttribute("sort", sort);
        model.addAttribute("dir", safeDir);
        model.addAttribute("pageData", pageData);
        model.addAttribute("foods", pageData.getContent());
        return "admin/foods";
    }

    @GetMapping("/create")
    public String showCreate(Model model, @RequestParam(value = "err", required = false) String err) {
        model.addAttribute("activeSection", "foods");
        model.addAttribute("foodForm", new FoodForm());
        model.addAttribute("categories", categoryService.findAll());
        if (err != null) model.addAttribute("error", err);
        return "admin/food-create";
    }

    @PostMapping("/create")
    public String handleCreate(@Valid @ModelAttribute("foodForm") FoodForm form,
                               BindingResult result,
                               Model model,
                               RedirectAttributes ra) {
        if (result.hasErrors()) {
            model.addAttribute("activeSection", "foods");
            model.addAttribute("categories", categoryService.findAll());
            return "admin/food-create";
        }
        try {
            foodService.createFood(form);
            ra.addAttribute("msg", "Food created");
        } catch (Exception ex) {
            log.error("Create food failed", ex);
            ra.addAttribute("err", ex.getMessage());
        }
        return "redirect:/admin/foods";
    }

    @GetMapping("/{id}/edit")
    public String edit(@PathVariable Long id, Model model, RedirectAttributes ra) {
        Optional<Food> opt = foodService.findById(id);
        if (opt.isEmpty()) {
            ra.addAttribute("err", "Food not found");
            return "redirect:/admin/foods";
        }
        Food f = opt.get();
        FoodForm form = new FoodForm();
        form.setCategoryId(f.getCategory() != null ? f.getCategory().getId() : null);
        form.setName(f.getName());
        form.setDescription(f.getDescription());
        form.setBasePrice(f.getBasePrice());
        form.setImageUrl(f.getImageUrl());
        form.setActive(f.isActive());
        model.addAttribute("foodForm", form);
        model.addAttribute("foodId", id);
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("activeSection", "foods");
        return "admin/food-edit";
    }

    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("foodForm") FoodForm form,
                         BindingResult result,
                         Model model,
                         RedirectAttributes ra) {
        if (result.hasErrors()) {
            model.addAttribute("foodId", id);
            model.addAttribute("categories", categoryService.findAll());
            model.addAttribute("activeSection", "foods");
            return "admin/food-edit";
        }
        try {
            foodService.updateFood(id, form);
            ra.addAttribute("msg", "Food updated");
        } catch (Exception ex) {
            log.error("Update food failed", ex);
            ra.addAttribute("err", ex.getMessage());
        }
        return "redirect:/admin/foods";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        try {
            foodService.softDelete(id);
            ra.addAttribute("msg", "Food deleted");
        } catch (Exception ex) {
            log.error("Delete food failed", ex);
            ra.addAttribute("err", ex.getMessage());
        }
        return "redirect:/admin/foods";
    }
}

