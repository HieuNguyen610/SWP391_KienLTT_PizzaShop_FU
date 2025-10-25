package com.swp.pizzashop.form;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AddToCartForm {

    @NotNull(message = "Food is required")
    private Long foodId;

    @NotNull(message = "Size is required")
    private Long sizeId;

    @NotNull
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    private String notes;

    private List<Long> toppingIds;
}
