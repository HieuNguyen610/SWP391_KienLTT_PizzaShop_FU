package com.swp.pizzashop.service.impl;

import com.swp.pizzashop.model.Category;
import com.swp.pizzashop.model.MenuItem;
import com.swp.pizzashop.repository.CategoryRepository;
import com.swp.pizzashop.repository.MenuItemRepository;
import com.swp.pizzashop.service.MenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MenuServiceImpl implements MenuService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private MenuItemRepository menuItemRepository;

    @Override
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    @Override
    public List<MenuItem> getAllMenuItems() {
        return menuItemRepository.findByAvailableTrue();
    }

    @Override
    public List<MenuItem> getMenuItemsByCategory(Long categoryId) {
        return menuItemRepository.findByCategoryId(categoryId);
    }

    @Override
    public Map<Category, List<MenuItem>> getFullMenu() {
        List<Category> categories = getAllCategories();
        List<MenuItem> items = getAllMenuItems();

        return items.stream()
            .collect(Collectors.groupingBy(MenuItem::getCategory));
    }
}
