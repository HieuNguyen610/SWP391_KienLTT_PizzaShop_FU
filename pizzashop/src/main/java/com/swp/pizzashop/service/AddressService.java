package com.swp.pizzashop.service;

import com.swp.pizzashop.model.Address;
import com.swp.pizzashop.model.User;
import com.swp.pizzashop.dto.AddressForm;

import java.util.List;
import java.util.Optional;

public interface AddressService {

    List<Address> findByUser(User user);

    // Use only the form-based overload for creates
    Address createAddress(User user, AddressForm form);

    Optional<Address> findByIdForUser(Long id, User user);

    // Use only the form-based overload for updates
    Address updateAddress(User user, Long id, AddressForm form);

    // Delete (soft delete) an address owned by the user
    void deleteAddress(User user, Long id);

    // Additional helpers for caps/UX
    long countActiveByUser(User user);

    int getMaxAddressesPerUser();
}
