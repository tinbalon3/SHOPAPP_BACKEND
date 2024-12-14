package com.project.shopapp.repositories;

import com.project.shopapp.models.User;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface UserRepository extends JpaRepository<User,Long> {
    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM User u JOIN u.role r WHERE u.phoneNumber = :phoneNumber AND r.id != 2")
    boolean existsUserWithPhoneNumberAndRoleNotAdmin(@Param("phoneNumber") String phoneNumber);

    boolean existsByEmail(String email);
    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.role.id = :id")
    boolean existsByRoleId(@Param("id") double id);

    @Query("SELECT u FROM User u join fetch u.role WHERE u.phoneNumber = :phoneNumber")
    Optional<User> findByPhoneNumber(String phoneNumber);
    @Query("SELECT u FROM User u join fetch u.role WHERE u.email = :email")
    Optional<User> findByEmail(String email);
    @Query("SELECT o FROM User o WHERE (:keyword IS NULL OR :keyword = '' OR " +
            "o.fullName LIKE %:keyword% " +
            "OR o.address LIKE %:keyword% " +
            "OR o.phoneNumber LIKE %:keyword% ) " +
            "AND lower(o.role.name) = 'user'")
    Page<User> findAll(@Param("keyword") String keyword, Pageable pageable);


}
