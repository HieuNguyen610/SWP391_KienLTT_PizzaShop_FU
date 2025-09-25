package com.swp.pizzashop.service.impl;

import com.swp.pizzashop.model.User;
import com.swp.pizzashop.repository.UserRepository;
import com.swp.pizzashop.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public User findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
}
