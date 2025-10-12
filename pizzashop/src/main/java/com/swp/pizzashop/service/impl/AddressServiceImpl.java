package com.swp.pizzashop.service.impl;

import com.swp.pizzashop.form.AddressForm;
import com.swp.pizzashop.model.Address;
import com.swp.pizzashop.model.User;
import com.swp.pizzashop.repository.AddressRepository;
import com.swp.pizzashop.service.AddressService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AddressServiceImpl implements AddressService {

    private final AddressRepository addressRepository;

    @Value("${pizzashop.address.max-per-user:5}")
    private int maxAddressesPerUser;

    @Override
    @Transactional(readOnly = true)
    public List<Address> findByUser(User user) {
        if (user == null) return List.of();
        // return only non-deleted addresses in UX
        return addressRepository.findByUserAndIsDeletedFalseOrderByDefaultAddressDescIdDesc(user);
    }

    @Override
    @Transactional
    public Address createAddress(User user, AddressForm form) {
        if (user == null) {
            log.warn("createAddress called with null user");
            throw new IllegalArgumentException("User is required.");
        }
        long count = addressRepository.countByUserAndIsDeletedFalse(user);
        log.debug("Creating address for user={} (activeCount={}, max={})", safeUser(user), count, maxAddressesPerUser);
        if (count >= maxAddressesPerUser) {
            log.warn("Address cap reached for user={}: {} >= {}", safeUser(user), count, maxAddressesPerUser);
            throw new IllegalArgumentException("You've reached the maximum number of saved addresses (" + maxAddressesPerUser + "). Please remove one before adding another.");
        }

        Address a = new Address();
        a.setUser(user);

        // --- Validation & normalization ---
        String streetTrim = form.getStreet() != null ? form.getStreet().trim() : "";
        if (streetTrim.isBlank()) {
            log.warn("Validation failed: street is blank for user={}", safeUser(user));
            throw new IllegalArgumentException("Street name is required.");
        }
        String numberTrim = form.getNumber() != null ? form.getNumber().trim() : "";

        String cityTrim = form.getProvince() != null ? form.getProvince().trim() : "";
        if (cityTrim.isBlank()) {
            log.warn("Validation failed: province/city is blank for user={}", safeUser(user));
            throw new IllegalArgumentException("Province/City is required.");
        }
        if (cityTrim.length() > 100) {
            log.warn("Validation failed: province length {} > 100 for user={}", cityTrim.length(), safeUser(user));
            throw new IllegalArgumentException("Province/City must be at most 100 characters.");
        }
        String districtTrim = form.getDistrict() != null ? form.getDistrict().trim() : "";
        if (districtTrim.isBlank()) {
            log.warn("Validation failed: district is blank for user={}", safeUser(user));
            throw new IllegalArgumentException("District is required.");
        }
        if (districtTrim.length() > 100) {
            log.warn("Validation failed: district length {} > 100 for user={}", districtTrim.length(), safeUser(user));
            throw new IllegalArgumentException("District must be at most 100 characters.");
        }
        String phoneTrim = form.getPhone() != null ? form.getPhone().trim() : null;
        if (phoneTrim != null && !phoneTrim.isBlank()) {
            if (phoneTrim.length() > 20) {
                log.warn("Validation failed: phone length {} > 20 for user={}", phoneTrim.length(), safeUser(user));
                throw new IllegalArgumentException("Phone number must be at most 20 characters.");
            }
            if (!phoneTrim.matches("^[0-9+()\\s-]+$")) {
                log.warn("Validation failed: phone contains invalid characters for user={}", safeUser(user));
                throw new IllegalArgumentException("Phone number may only contain digits, spaces, plus (+), parentheses and hyphens.");
            }
            int digitCount = phoneTrim.replaceAll("\\D", "").length();
            if (digitCount < 8 || digitCount > 15) {
                log.warn("Validation failed: phone digit count {} outside [8,15] for user={}", digitCount, safeUser(user));
                throw new IllegalArgumentException("Phone number must contain between 8 and 15 digits.");
            }
        } else {
            phoneTrim = null; // store as null if empty
        }

        String addressLine = numberTrim.isBlank() ? streetTrim : (numberTrim + ", " + streetTrim);
        if (addressLine.length() > 255) {
            log.warn("Validation failed: addressLine length {} > 255 for user={}", addressLine.length(), safeUser(user));
            throw new IllegalArgumentException("Address line is too long (maximum 255 characters).");
        }

        a.setAddressLine(addressLine);
        a.setCity(cityTrim);
        a.setDistrict(districtTrim);
        a.setPhone(phoneTrim);

        // if marked as default, clear previous default for this user
        if (form.isSetDefault()) {
            log.debug("New address marked default for user={}, clearing previous default", safeUser(user));
            addressRepository.clearDefaultByUser(user);
            a.setDefaultAddress(true);
        } else {
            // If this is the user's first address, make it default automatically
            boolean hasAny = count > 0; // we already counted active addresses
            a.setDefaultAddress(!hasAny);
        }

        Address saved = addressRepository.save(a);
        log.info("Created address id={} for user={}", saved.getId(), safeUser(user));
        return saved;
    }

    @Override
    public Optional<Address> findByIdForUser(Long id, User user) {
        if (id == null || user == null) return Optional.empty();
        return addressRepository.findByIdAndUser(id, user);
    }

    @Override
    @Transactional
    public Address updateAddress(User user, Long id, AddressForm form) {
        var opt = addressRepository.findByIdAndUser(id, user);
        if (opt.isEmpty()) {
            log.warn("Update failed: address id={} not found for user={}", id, safeUser(user));
            throw new IllegalArgumentException("Address not found");
        }
        Address existing = opt.get();

        // --- Validation & normalization ---
        String streetTrim = form.getStreet() != null ? form.getStreet().trim() : "";
        if (streetTrim.isBlank()) {
            log.warn("Validation failed (update): street is blank for user={}, id={}", safeUser(user), id);
            throw new IllegalArgumentException("Street name is required.");
        }
        String numberTrim = form.getNumber() != null ? form.getNumber().trim() : "";

        String cityTrim = form.getProvince() != null ? form.getProvince().trim() : "";
        if (cityTrim.isBlank()) {
            log.warn("Validation failed (update): province/city is blank for user={}, id={}", safeUser(user), id);
            throw new IllegalArgumentException("Province/City is required.");
        }
        if (cityTrim.length() > 100) {
            log.warn("Validation failed (update): province length {} > 100 for user={}, id={}", cityTrim.length(), safeUser(user), id);
            throw new IllegalArgumentException("Province/City must be at most 100 characters.");
        }
        String districtTrim = form.getDistrict() != null ? form.getDistrict().trim() : "";
        if (districtTrim.isBlank()) {
            log.warn("Validation failed (update): district is blank for user={}, id={}", safeUser(user), id);
            throw new IllegalArgumentException("District is required.");
        }
        if (districtTrim.length() > 100) {
            log.warn("Validation failed (update): district length {} > 100 for user={}, id={}", districtTrim.length(), safeUser(user), id);
            throw new IllegalArgumentException("District must be at most 100 characters.");
        }
        String phoneTrim = form.getPhone() != null ? form.getPhone().trim() : null;
        if (phoneTrim != null && !phoneTrim.isBlank()) {
            if (phoneTrim.length() > 20) {
                log.warn("Validation failed (update): phone length {} > 20 for user={}, id={}", phoneTrim.length(), safeUser(user), id);
                throw new IllegalArgumentException("Phone number must be at most 20 characters.");
            }
            if (!phoneTrim.matches("^[0-9+()\\s-]+$")) {
                log.warn("Validation failed (update): phone contains invalid characters for user={}, id={}", safeUser(user), id);
                throw new IllegalArgumentException("Phone number may only contain digits, spaces, plus (+), parentheses and hyphens.");
            }
            int digitCount = phoneTrim.replaceAll("\\D", "").length();
            if (digitCount < 8 || digitCount > 15) {
                log.warn("Validation failed (update): phone digit count {} outside [8,15] for user={}, id={}", digitCount, safeUser(user), id);
                throw new IllegalArgumentException("Phone number must contain between 8 and 15 digits.");
            }
        } else {
            phoneTrim = null; // store as null if empty
        }

        String addressLine = numberTrim.isBlank() ? streetTrim : (numberTrim + ", " + streetTrim);
        if (addressLine.length() > 255) {
            log.warn("Validation failed (update): addressLine length {} > 255 for user={}, id={}", addressLine.length(), safeUser(user), id);
            throw new IllegalArgumentException("Address line is too long (maximum 255 characters).");
        }

        existing.setAddressLine(addressLine);
        existing.setCity(cityTrim);
        existing.setDistrict(districtTrim);
        existing.setPhone(phoneTrim);

        if (form.isSetDefault()) {
            log.debug("Update: address id={} marked default for user={}, clearing previous default", id, safeUser(user));
            addressRepository.clearDefaultByUser(user);
            existing.setDefaultAddress(true);
        }

        Address saved = addressRepository.save(existing);
        log.info("Updated address id={} for user={}", saved.getId(), safeUser(user));
        return saved;
    }

    @Override
    @Transactional
    public void deleteAddress(User user, Long id) {
        // Find non-deleted address owned by user
        var opt = addressRepository.findByIdAndUserAndIsDeletedFalse(id, user);
        if (opt.isEmpty()) {
            log.warn("Delete failed: address id={} not found or already deleted for user={}", id, safeUser(user));
            throw new IllegalArgumentException("Address not found or already deleted");
        }
        Address addr = opt.get();

        boolean wasDefault = addr.isDefaultAddress();
        addr.setIsDeleted(true);
        addr.setDefaultAddress(false);
        addressRepository.save(addr);
        log.info("Deleted (soft) address id={} for user={} (wasDefault={})", id, safeUser(user), wasDefault);

        // If the deleted one was default, promote another address as default
        if (wasDefault) {
            addressRepository.findFirstByUserAndIsDeletedFalseOrderByIdDesc(user)
                    .ifPresentOrElse(replacement -> {
                        replacement.setDefaultAddress(true);
                        addressRepository.save(replacement);
                        log.info("Promoted address id={} as new default for user={}", replacement.getId(), safeUser(user));
                    }, () -> log.info("No replacement default address available for user={}", safeUser(user)));
        }
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

    private String safeUser(User user) {
        if (user == null) return "<null>";
        try {
            Long id = null;
            String email = null;
            try { id = user.getId(); } catch (Exception ignored) {}
            try { email = user.getEmail(); } catch (Exception ignored) {}
            return "id=" + String.valueOf(id) + ", email=" + String.valueOf(email);
        } catch (Exception e) {
            return "<user>";
        }
    }
}
