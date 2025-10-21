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

public interface UserService {

    User findByEmail(String email);
    User registerUser(RegisterForm registerForm);
    User updateUser(User user);
    public void activateUser(String email) ;
}
