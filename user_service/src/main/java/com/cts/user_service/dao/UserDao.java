package com.cts.user_service.dao;

import com.cts.user_service.entity.User;

import java.util.List;
import java.util.Optional;

/**
 * Data Access Object interface for User entity.
 * Provides abstraction layer between service and repository.
 */
public interface UserDao {

    /**
     * Save a user to the database
     * @param user User entity to save
     * @return Saved user with generated ID
     */
    User save(User user);

    /**
     * Find user by unique ID
     * @param id User ID
     * @return Optional containing user if found
     */
    Optional<User> findById(String id);

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
     * Find all users with a specific role
     * @param role User role (RIDER, DRIVER, ADMIN)
     * @return List of users with the specified role
     */
    List<User> findByRole(User.Role role);

    /**
     * Find all users with a specific status
     * @param status User status
     * @return List of users with the specified status
     */
    List<User> findByStatus(User.Status status);

    /**
     * Find all users
     * @return List of all users
     */
    List<User> findAll();

    /**
     * Check if user exists by email
     * @param email Email to check
     * @return true if exists, false otherwise
     */
    boolean existsByEmail(String email);

    /**
     * Check if user exists by phone number
     * @param phoneNumber Phone number to check
     * @return true if exists, false otherwise
     */
    boolean existsByPhoneNumber(String phoneNumber);

    /**
     * Delete user by ID
     * @param id User ID to delete
     */
    void deleteById(String id);

    /**
     * Count total number of users
     * @return Total user count
     */
    long count();

    /**
     * Count users by role
     * @param role User role
     * @return Count of users with specified role
     */
    long countByRole(User.Role role);

    /**
     * Count users by status
     * @param status User status
     * @return Count of users with specified status
     */
    long countByStatus(User.Status status);
}

