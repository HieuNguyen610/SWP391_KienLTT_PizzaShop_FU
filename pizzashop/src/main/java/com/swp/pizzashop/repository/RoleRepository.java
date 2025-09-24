package com.swp.pizzashop.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.swp.pizzashop.model.Role;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Role findByName(String name);
}


