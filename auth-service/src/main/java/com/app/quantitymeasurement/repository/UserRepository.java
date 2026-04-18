package com.app.quantitymeasurement.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.app.quantitymeasurement.entity.User;

/**
 * Repository interface for UserEntity.
 * Extends JpaRepository to provide standard CRUD operations.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /** Find a user by their unique email. */
    Optional<User> findByEmail(String email);

    /** Check if a user with a specific email exists. */
    Boolean existsByEmail(String email);

    /** Find a user by reset password token. */
    Optional<User> findByResetPasswordToken(String resetPasswordToken);
}
