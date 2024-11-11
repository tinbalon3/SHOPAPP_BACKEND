package com.project.shopapp.repositories;

import com.project.shopapp.models.User;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long> {

    boolean existsByPhoneNumber(String phoneNumber);

    boolean existsByEmail(String email);
    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.role.id = :id")
    boolean existsByRoleId(@Param("id") Long id);

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

//    @Query("SELECT u FROM User u WHERE u.verificationCode = ?1")
//    User findByVerificationCode(String code);


}
