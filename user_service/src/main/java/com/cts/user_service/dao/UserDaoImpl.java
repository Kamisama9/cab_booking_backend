package com.cts.user_service.dao;

import com.cts.user_service.entity.User;
import com.cts.user_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserDaoImpl implements UserDao {

    private final UserRepository userRepository;

    @Override
    public User save(User user) {
        log.debug("Saving user with email: {}", user.getEmail());
        User savedUser = userRepository.save(user);
        log.info("User saved successfully with ID: {}", savedUser.getId());
        return savedUser;
    }

    @Override
    public Optional<User> findById(String id) {
        log.debug("Finding user by ID: {}", id);
        return userRepository.findById(id);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        log.debug("Finding user by email: {}", email);
        return userRepository.findByEmail(email);
    }

    @Override
    public Optional<User> findByPhoneNumber(String phoneNumber) {
        log.debug("Finding user by phone number: {}", phoneNumber);
        return userRepository.findByPhoneNumber(phoneNumber);
    }

    @Override
    public List<User> findByRole(User.Role role) {
        log.debug("Finding users by role: {}", role);
        return userRepository.findByRole(role);
    }

    @Override
    public List<User> findByStatus(User.Status status) {
        log.debug("Finding users by status: {}", status);
        return userRepository.findByStatus(status);
    }

    @Override
    public List<User> findAll() {
        log.debug("Finding all users");
        return userRepository.findAll();
    }

    @Override
    public boolean existsByEmail(String email) {
        log.debug("Checking if user exists by email: {}", email);
        return userRepository.existsByEmail(email);
    }

    @Override
    public boolean existsByPhoneNumber(String phoneNumber) {
        log.debug("Checking if user exists by phone number: {}", phoneNumber);
        return userRepository.existsByPhoneNumber(phoneNumber);
    }

    @Override
    public void deleteById(String id) {
        log.debug("Deleting user by ID: {}", id);
        userRepository.deleteById(id);
        log.info("User deleted successfully with ID: {}", id);
    }

    @Override
    public long count() {
        log.debug("Counting total users");
        return userRepository.count();
    }

    @Override
    public long countByRole(User.Role role) {
        log.debug("Counting users by role: {}", role);
        return userRepository.countByRole(role);
    }

    @Override
    public long countByStatus(User.Status status) {
        log.debug("Counting users by status: {}", status);
        return userRepository.countByStatus(status);
    }
}

