package com.swp.pizzashop.form;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EditProfileForm {
    @NotBlank(message = "First name can not be blank")
    @Size(min = 2, max = 10, message = "First name must be between 2 and 10 characters")
    private String firstName;

    @NotBlank(message = "Last name can not be blank")
    @Size(min = 2, max = 10, message = "Last name must be between 2 and 10 characters")
    private String lastName;

    @NotBlank(message = "Phone number can not be blank")
    @Pattern(regexp = "^(\\+84|0)\\d{9,10}$", message = "Phone number is not valid")
    private String phone;

    private String email;
}
