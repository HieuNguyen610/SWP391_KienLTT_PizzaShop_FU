package com.swp.pizzashop.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Form object for creating/updating a delivery address.
 * Intended to be used with Spring MVC as a @ModelAttribute / @Valid parameter.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddressForm {

    @NotBlank(message = "Province/City is required.")
    @Size(max = 100, message = "Province/City must be at most 100 characters.")
    private String province;

    @NotBlank(message = "District is required.")
    @Size(max = 100, message = "District must be at most 100 characters.")
    private String district;

    @NotBlank(message = "Street name is required.")
    @Size(max = 255, message = "Street name is too long.")
    private String street;

    @Size(max = 64, message = "House number must be at most 64 characters.")
    private String number;

    @Size(max = 20, message = "Phone number must be at most 20 characters.")
    @Pattern(regexp = "^$|[0-9+()\\-\\s]+", message = "Phone number contains invalid characters.")
    private String phone;

    private boolean setDefault;
}

