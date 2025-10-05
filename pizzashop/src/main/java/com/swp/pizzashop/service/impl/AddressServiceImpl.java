package com.swp.pizzashop.service.impl;

import com.swp.pizzashop.model.Address;
import com.swp.pizzashop.model.User;
import com.swp.pizzashop.repository.AddressRepository;
import com.swp.pizzashop.service.AddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {

    private final AddressRepository addressRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Address> findByUser(User user) {
        if (user == null) return List.of();
        return addressRepository.findByUserOrderByDefaultAddressDescIdDesc(user);
    }

    @Override
    @Transactional
    public Address createAddress(User user,
                                 String fullName,
                                 String province,
                                 String district,
                                 String street,
                                 String number,
                                 String phone,
                                 boolean setDefault) {
        Address a = new Address();
        a.setUser(user);

        String streetTrim = street != null ? street.trim() : "";
        String numberTrim = number != null ? number.trim() : "";
        String addressLine = streetTrim;
        if (!numberTrim.isBlank()) {
            addressLine = numberTrim + ", " + streetTrim;
        }
        a.setAddressLine(addressLine);

        a.setCity(province != null ? province.trim() : null);
        a.setDistrict(district != null ? district.trim() : null);
        a.setPhone(phone != null ? phone.trim() : null);

        // if marked as default, clear previous default for this user
        if (setDefault) {
            addressRepository.clearDefaultByUser(user);
            a.setDefaultAddress(true);
        } else {
            // If this is the user's first address, make it default automatically
            boolean hasAny = !addressRepository.findByUserOrderByDefaultAddressDescIdDesc(user).isEmpty();
            a.setDefaultAddress(!hasAny);
        }

        return addressRepository.save(a);
    }
}
