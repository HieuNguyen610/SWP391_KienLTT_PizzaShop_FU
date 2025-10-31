package com.swp.pizzashop.repository;

import com.swp.pizzashop.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    User findByEmailAndPassword(String email, String password);
    User findByEmail(String email);
    User findByPhone(String phone);
    long countByStatusAndIsDeletedFalse(String active);
    long countByIsDeletedFalse();
    Page<User> findAll(Pageable pageable);
    @Query("SELECT u FROM User u WHERE u.isDeleted = false AND " +
            "(LOWER(u.firstname) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(u.lastname) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(u.phone) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<User> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    Page<User> findAllByIsDeletedFalseOrderByIdDesc(Pageable pageable);

}
