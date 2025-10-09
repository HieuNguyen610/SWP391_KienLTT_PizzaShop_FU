package com.swp.pizzashop.form;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class FoodForm {
    @NotNull(message = "Category is required")
    private Long categoryId;

    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name must be at most 100 characters")
    private String name;

    @Size(max = 5000, message = "Description is too long")
    private String description;

    @NotNull(message = "Base price is required")
    @DecimalMin(value = "0.01", message = "Base price must be greater than 0")
    @Digits(integer = 8, fraction = 2, message = "Base price format is invalid")
    private BigDecimal basePrice;

    // Optional: allow either upload or external URL
    private MultipartFile imageFile;

    @Size(max = 255, message = "Image URL is too long")
    private String imageUrl;

    private boolean active = true;
}
