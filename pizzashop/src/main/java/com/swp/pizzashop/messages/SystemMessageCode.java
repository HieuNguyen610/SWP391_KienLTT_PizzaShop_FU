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
    MSG11, // Logged out successfully
    MSG12, // Account inactive or deleted
    MSG13, // Reset password link invalid or expired
    MSG14, // Logged out successfully
    MSG15; // Account inactive or deleted

    public String code() { return name(); }
}
