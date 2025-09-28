package com.swp.pizzashop.repository;

import com.swp.pizzashop.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    User findByEmailAndPassword(String email, String password);
    User findByEmail(String email);
    User findByPhone(String phone);
    Optional<User> findByResetPasswordToken(String resetPasswordToken);
}
