package com.swp.pizzashop.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CategorySummary {
    private Long categoryId;
    private String categoryName;
    private long foodCount;
    private long activeFoodCount;
}

