package com.swp.pizzashop.service;

import com.swp.pizzashop.model.MenuItem;
import com.swp.pizzashop.model.Category;
import java.util.List;
import java.util.Map;

public interface MenuService {
    List<Category> getAllCategories();
    List<MenuItem> getAllMenuItems();
    List<MenuItem> getMenuItemsByCategory(Long categoryId);
    Map<Category, List<MenuItem>> getFullMenu();
}
