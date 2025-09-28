package com.swp.pizzashop.service;

import com.swp.pizzashop.model.User;
import java.util.Optional;

public interface UserService {
    User findByEmail(String email);
    User save(User user);
    Optional<User> findByResetPasswordToken(String token);
}
