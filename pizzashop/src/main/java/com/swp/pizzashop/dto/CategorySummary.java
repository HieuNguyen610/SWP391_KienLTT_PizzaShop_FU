package com.swp.pizzashop.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CategorySummary {
    private Long categoryId;
    private String categoryName;
    private long totalFoods;
    private long activeFoods;
}

