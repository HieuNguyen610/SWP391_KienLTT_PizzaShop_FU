package com.swp.pizzashop.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Form backing object for submitting a new password using a reset token.
 * Validation:
 *  - token must be present (hidden field)
 *  - password min length 8 (matches MSG07 wording in messages.properties)
 *  - confirmPassword required (matching is enforced in controller/service layer)
 */
@Getter
@Setter
@NoArgsConstructor
public class ResetPasswordForm {

    @NotBlank(message = "Missing reset token")
    private String token;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    @NotBlank(message = "Confirm Password is required")
    private String confirmPassword;
}

