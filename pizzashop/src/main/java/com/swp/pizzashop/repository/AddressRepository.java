package com.swp.pizzashop.repository;

import com.swp.pizzashop.model.Address;
import com.swp.pizzashop.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AddressRepository extends JpaRepository<Address, Long> {

    List<Address> findByUserOrderByDefaultAddressDescIdDesc(User user);

    Address findFirstByUserAndDefaultAddressTrue(User user);

    Optional<Address> findByIdAndUser(Long id, User user);

    @Modifying
    @Query("update Address a set a.defaultAddress = false where a.user = :user and a.defaultAddress = true")
    int clearDefaultByUser(@Param("user") User user);

    // Count active (non-deleted) addresses for a user
    long countByUserAndIsDeletedFalse(User user);
}
