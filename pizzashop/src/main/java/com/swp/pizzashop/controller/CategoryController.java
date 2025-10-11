package com.swp.pizzashop.controller;

import com.swp.pizzashop.form.CategoryForm;
import com.swp.pizzashop.model.FoodCategory;
import com.swp.pizzashop.service.FoodCategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
@RequestMapping("/admin/categories")
@RequiredArgsConstructor
@Slf4j
public class CategoryController {

    private final FoodCategoryService categoryService;

    @GetMapping
    public String listCategories(@RequestParam(value = "page", defaultValue = "0") int page,
                                 @RequestParam(value = "size", defaultValue = "10") int size,
                                 @RequestParam(value = "q", required = false) String q,
                                 @RequestParam(value = "msg", required = false) String msg,
                                 @RequestParam(value = "err", required = false) String err,
                                 Model model) {
        model.addAttribute("activeSection", "categories");
        if (msg != null) model.addAttribute("success", msg);
        if (err != null) model.addAttribute("error", err);

        int pageIndex = Math.max(page, 0);
        Pageable pageable = PageRequest.of(pageIndex, 10);
        Page<FoodCategory> pageData = categoryService.findPage(q, pageable);
        model.addAttribute("q", q);
        model.addAttribute("pageData", pageData);
        model.addAttribute("categories", pageData.getContent());
        model.addAttribute("categoryForm", new CategoryForm());
        return "admin/categories";
    }

    @GetMapping("/{id}/edit")
    public String editCategory(@PathVariable Long id, Model model, RedirectAttributes ra) {
        Optional<FoodCategory> opt = categoryService.findById(id);
        if (opt.isEmpty()) {
            ra.addAttribute("err", "Category not found");
            return "redirect:/admin/categories";
        }
        FoodCategory c = opt.get();
        CategoryForm form = new CategoryForm();
        form.setName(c.getName());
        form.setDescription(c.getDescription());
        model.addAttribute("categoryForm", form);
        model.addAttribute("categoryId", id);
        model.addAttribute("category", c);
        model.addAttribute("activeSection", "categories");
        return "admin/category-edit";
    }

    @PostMapping("/{id}/edit")
    public String updateCategory(@PathVariable Long id,
                                 @Valid @ModelAttribute("categoryForm") CategoryForm form,
                                 BindingResult result,
                                 RedirectAttributes ra,
                                 Model model) {
        FoodCategory existingByName = categoryService.findByName(form.getName());
        if (existingByName != null && !existingByName.getId().equals(id)) {
            result.rejectValue("name", "duplicate", "Category name already in use");
        }
        if (result.hasErrors()) {
            model.addAttribute("categoryId", id);
            model.addAttribute("activeSection", "categories");
            return "admin/category-edit";
        }
        try {
            categoryService.update(id, form.getName().trim(), form.getDescription());
            ra.addAttribute("msg", "Category updated");
        } catch (Exception ex) {
            log.error("Update category failed", ex);
            ra.addAttribute("err", ex.getMessage());
        }
        return "redirect:/admin/categories";
    }

    @PostMapping
    public String createCategory(@Valid @ModelAttribute("categoryForm") CategoryForm form,
                                 BindingResult result,
                                 RedirectAttributes ra) {
        FoodCategory existing = categoryService.findByName(form.getName());
        if (existing != null) {
            result.rejectValue("name", "duplicate", "Category already exists");
        }
        if (result.hasErrors()) {
            ra.addAttribute("err", "Invalid category: " + (result.getFieldError() != null ? result.getFieldError().getDefaultMessage() : ""));
            return "redirect:/admin/categories";
        }
        FoodCategory cat = new FoodCategory();
        cat.setName(form.getName().trim());
        cat.setDescription(form.getDescription());
        categoryService.save(cat);
        ra.addAttribute("msg", "Category created");
        return "redirect:/admin/categories";
    }

    @PostMapping("/{id}/restore")
    public String restoreCategory(@PathVariable Long id, RedirectAttributes ra) {
        try {
            categoryService.restore(id);
            ra.addAttribute("msg", "Category enabled");
        } catch (Exception ex) {
            ra.addAttribute("err", ex.getMessage());
        }
        return "redirect:/admin/categories/" + id + "/edit";
    }

    @PostMapping("/{id}/delete")
    public String deleteCategory(@PathVariable Long id, RedirectAttributes ra) {
        try {
            categoryService.softDelete(id);
            ra.addAttribute("msg", "Category deleted");
        } catch (Exception ex) {
            log.error("Delete category failed", ex);
            ra.addAttribute("err", ex.getMessage());
        }
        return "redirect:/admin/categories";
    }
}
