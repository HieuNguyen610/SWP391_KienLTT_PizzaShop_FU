package com.swp.pizzashop.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class BCryptEncodeUtil {
    public static void main(String[] args) {
        PasswordEncoder encoder = new BCryptPasswordEncoder();
        String rawPassword = "1";
        String encoded = encoder.encode(rawPassword);
        System.out.println(encoded);
    }
}

