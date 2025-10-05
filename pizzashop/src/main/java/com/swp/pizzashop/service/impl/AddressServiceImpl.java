package com.swp.pizzashop.service.impl;

import com.swp.pizzashop.model.Address;
import com.swp.pizzashop.model.User;
import com.swp.pizzashop.repository.AddressRepository;
import com.swp.pizzashop.service.AddressService;
import com.swp.pizzashop.dto.AddressForm;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {

    private final AddressRepository addressRepository;

    @Value("${pizzashop.address.max-per-user:5}")
    private int maxAddressesPerUser;

    @Override
    @Transactional(readOnly = true)
    public List<Address> findByUser(User user) {
        if (user == null) return List.of();
        return addressRepository.findByUserOrderByDefaultAddressDescIdDesc(user);
    }

    @Override
    @Transactional
    public Address createAddress(User user, AddressForm form) {
        if (user == null) {
            throw new IllegalArgumentException("User is required.");
        }
        long count = addressRepository.countByUserAndIsDeletedFalse(user);
        if (count >= maxAddressesPerUser) {
            throw new IllegalArgumentException("You've reached the maximum number of saved addresses (" + maxAddressesPerUser + "). Please remove one before adding another.");
        }

        Address a = new Address();
        a.setUser(user);

        // --- Validation & normalization ---
        String streetTrim = form.getStreet() != null ? form.getStreet().trim() : "";
        if (streetTrim.isBlank()) {
            throw new IllegalArgumentException("Street name is required.");
        }
        String numberTrim = form.getNumber() != null ? form.getNumber().trim() : "";

        String cityTrim = form.getProvince() != null ? form.getProvince().trim() : "";
        if (cityTrim.isBlank()) {
            throw new IllegalArgumentException("Province/City is required.");
        }
        if (cityTrim.length() > 100) {
            throw new IllegalArgumentException("Province/City must be at most 100 characters.");
        }
        String districtTrim = form.getDistrict() != null ? form.getDistrict().trim() : "";
        if (districtTrim.isBlank()) {
            throw new IllegalArgumentException("District is required.");
        }
        if (districtTrim.length() > 100) {
            throw new IllegalArgumentException("District must be at most 100 characters.");
        }
        String phoneTrim = form.getPhone() != null ? form.getPhone().trim() : null;
        if (phoneTrim != null && !phoneTrim.isBlank()) {
            if (phoneTrim.length() > 20) {
                throw new IllegalArgumentException("Phone number must be at most 20 characters.");
            }
            if (!phoneTrim.matches("^[0-9+()\\s-]+$")) {
                throw new IllegalArgumentException("Phone number contains invalid characters.");
            }
        } else {
            phoneTrim = null; // store as null if empty
        }

        String addressLine = numberTrim.isBlank() ? streetTrim : (numberTrim + ", " + streetTrim);
        if (addressLine.length() > 255) {
            throw new IllegalArgumentException("Address line is too long (maximum 255 characters).");
        }

        a.setAddressLine(addressLine);
        a.setCity(cityTrim);
        a.setDistrict(districtTrim);
        a.setPhone(phoneTrim);

        // if marked as default, clear previous default for this user
        if (form.isSetDefault()) {
            addressRepository.clearDefaultByUser(user);
            a.setDefaultAddress(true);
        } else {
            // If this is the user's first address, make it default automatically
            boolean hasAny = count > 0; // we already counted active addresses
            a.setDefaultAddress(!hasAny);
        }

        return addressRepository.save(a);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Address> findByIdForUser(Long id, User user) {
        if (id == null || user == null) return Optional.empty();
        return addressRepository.findByIdAndUser(id, user);
    }

    @Override
    @Transactional
    public Address updateAddress(User user, Long id, AddressForm form) {
        Address existing = addressRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new IllegalArgumentException("Address not found"));

        // --- Validation & normalization ---
        String streetTrim = form.getStreet() != null ? form.getStreet().trim() : "";
        if (streetTrim.isBlank()) {
            throw new IllegalArgumentException("Street name is required.");
        }
        String numberTrim = form.getNumber() != null ? form.getNumber().trim() : "";

        String cityTrim = form.getProvince() != null ? form.getProvince().trim() : "";
        if (cityTrim.isBlank()) {
            throw new IllegalArgumentException("Province/City is required.");
        }
        if (cityTrim.length() > 100) {
            throw new IllegalArgumentException("Province/City must be at most 100 characters.");
        }
        String districtTrim = form.getDistrict() != null ? form.getDistrict().trim() : "";
        if (districtTrim.isBlank()) {
            throw new IllegalArgumentException("District is required.");
        }
        if (districtTrim.length() > 100) {
            throw new IllegalArgumentException("District must be at most 100 characters.");
        }
        String phoneTrim = form.getPhone() != null ? form.getPhone().trim() : null;
        if (phoneTrim != null && !phoneTrim.isBlank()) {
            if (phoneTrim.length() > 20) {
                throw new IllegalArgumentException("Phone number must be at most 20 characters.");
            }
            if (!phoneTrim.matches("^[0-9+()\\s-]+$")) {
                throw new IllegalArgumentException("Phone number contains invalid characters.");
            }
        } else {
            phoneTrim = null; // store as null if empty
        }

        String addressLine = numberTrim.isBlank() ? streetTrim : (numberTrim + ", " + streetTrim);
        if (addressLine.length() > 255) {
            throw new IllegalArgumentException("Address line is too long (maximum 255 characters).");
        }

        existing.setAddressLine(addressLine);
        existing.setCity(cityTrim);
        existing.setDistrict(districtTrim);
        existing.setPhone(phoneTrim);

        if (form.isSetDefault()) {
            addressRepository.clearDefaultByUser(user);
            existing.setDefaultAddress(true);
        }

        return addressRepository.save(existing);
    }

    @Override
    @Transactional(readOnly = true)
    public long countActiveByUser(User user) {
        if (user == null) return 0;
        return addressRepository.countByUserAndIsDeletedFalse(user);
    }

    @Override
    public int getMaxAddressesPerUser() {
        return maxAddressesPerUser;
    }
}
