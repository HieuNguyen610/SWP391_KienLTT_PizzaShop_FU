package com.swp.pizzashop.messages;

/**
 * System message codes. Keep synchronized with messages.properties.
 */
public enum SystemMessageCode {
    MSG01, // Email confirmation sent
    MSG02, // Email confirmed success
    MSG03, // Please enter your {0}
    MSG04, // Duplicate email
    MSG05, // Invalid {0} format
    MSG06, // Email confirmation failed
    MSG07, // Password length requirement
    MSG08, // Invalid username/password
    MSG09, // Email not verified
    MSG10, // Account blocked
    MSG11, // Password reset link sent
    MSG12, // Reset password successfully
    MSG13, // Reset link invalid/expired
    MSG14, // Logged out successfully
    MSG15, // Account inactive or deleted
    MSG16, // Passwords do not match
    MSG17; // Email not found for password reset

    public String code() { return name(); }
}
