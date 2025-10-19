package com.swp.pizzashop.repository;

import com.swp.pizzashop.model.Topping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ToppingRepository extends JpaRepository<Topping, Long> {

    List<Topping> findByIsDeletedFalseAndIsActiveTrueOrderByNameAsc();

    @Query("select t from Topping t join t.foods f where f.id = :foodId and (t.isDeleted is null or t.isDeleted = false) and t.isActive = true order by t.name asc")
    List<Topping> findDefaultsForFood(@Param("foodId") Long foodId);

    @Query("select t.id from Topping t join t.foods f where f.id = :foodId and (t.isDeleted is null or t.isDeleted = false) and t.isActive = true")
    List<Long> findDefaultIdsForFood(@Param("foodId") Long foodId);
}