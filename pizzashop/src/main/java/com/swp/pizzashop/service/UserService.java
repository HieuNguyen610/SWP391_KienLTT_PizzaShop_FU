package com.swp.pizzashop.service;

import com.swp.pizzashop.model.User;

public interface UserService {

    User findByEmail(String email);
}
