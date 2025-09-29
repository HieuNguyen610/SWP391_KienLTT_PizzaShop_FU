package com.swp.pizzashop.service;

import com.swp.pizzashop.form.RegisterForm;
import com.swp.pizzashop.model.Role;
import com.swp.pizzashop.model.User;
import com.swp.pizzashop.repository.RoleRepository;
import com.swp.pizzashop.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RoleRepository roleRepository;

    public User registerUser(RegisterForm registerForm) {
        if (userRepository.findByEmail(registerForm.getEmail()) != null) {
            throw new IllegalArgumentException("Email is already registered");
        }

        String hashedPassword = passwordEncoder.encode(registerForm.getPassword());

        User user = new User();
        user.setFirstname(registerForm.getFirstname());
        user.setLastname(registerForm.getLastname());
        user.setEmail(registerForm.getEmail());
        user.setPassword(hashedPassword);
        user.setPhone(registerForm.getPhone());
        user.setStatus("ACTIVE");
        user.setProvider(null);
        user.setProviderId(null);
        user.setVerificationToken(null);
        user.setVerificationExpiresAt(null);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        Role customerRole = roleRepository.findByName("Customer");
        if (customerRole != null) {
            Set<Role> roles = new HashSet<>();
            roles.add(customerRole);
            user.setRoles(roles);

        }
        User savedUser = userRepository.save(user);


        return savedUser;
    }
}
