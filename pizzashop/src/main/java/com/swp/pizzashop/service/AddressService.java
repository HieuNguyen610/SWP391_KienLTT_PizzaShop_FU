package com.swp.pizzashop.service;

import com.swp.pizzashop.model.Address;
import com.swp.pizzashop.model.User;

import java.util.List;

public interface AddressService {

    List<Address> findByUser(User user);

    Address createAddress(User user,
                          String fullName,
                          String province,
                          String district,
                          String street,
                          String number,
                          String phone,
                          boolean setDefault);
}

