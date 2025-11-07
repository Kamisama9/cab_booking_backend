package com.cts.user_service.repository;

import com.cts.user_service.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for User entity.
 * Provides database access methods using Spring Data JPA.
 */
@Repository
public interface UserRepository extends JpaRepository<User, String> {

    /**
     * Find user by email address
     * @param email User's email
     * @return Optional containing user if found
     */
    Optional<User> findByEmail(String email);

    /**
     * Find user by phone number
     * @param phoneNumber User's phone number
     * @return Optional containing user if found
     */
    Optional<User> findByPhoneNumber(String phoneNumber);

    /**
     * Find all users with specific role
     * @param role User role
     * @return List of users with the role
     */
    List<User> findByRole(User.Role role);

    /**
     * Find all users with specific status
     * @param status User status
     * @return List of users with the status
     */
    List<User> findByStatus(User.Status status);

    /**
     * Check if user exists with given email
     * @param email Email to check
     * @return true if exists, false otherwise
     */
    boolean existsByEmail(String email);

    /**
     * Check if user exists with given phone number
     * @param phoneNumber Phone number to check
     * @return true if exists, false otherwise
     */
    boolean existsByPhoneNumber(String phoneNumber);

    /**
     * Count users by role
     * @param role User role
     * @return Count of users
     */
    long countByRole(User.Role role);

    /**
     * Count users by status
     * @param status User status
     * @return Count of users
     */
    long countByStatus(User.Status status);
}
