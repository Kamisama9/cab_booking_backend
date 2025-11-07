package com.cts.user_service.service;

import com.cts.user_service.dto.LoginRequest;
import com.cts.user_service.dto.SignupRequest;
import com.cts.user_service.dto.UserValidationResponse;
import com.cts.user_service.entity.User;
import com.cts.user_service.exception.*;
import com.cts.user_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public String registerUser(SignupRequest request) {
        log.info("Attempting to register user with email: {}", request.getEmail());
        validateSignupRequest(request);

        Optional<User> existingUser = userRepository.findByEmail(request.getEmail());
        if (existingUser.isPresent()) {
            log.warn("Registration failed: User already exists with email: {}", request.getEmail());
            throw new UserAlreadyExistsException("User already exists with email: " + request.getEmail());
        }

        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(parseRole(request.getRole()));
        user.setStatus(User.Status.PENDING_VERIFICATION);
        user.setEmailVerified(false);

        User savedUser = userRepository.save(user);
        log.info("User registered successfully with ID: {} and email: {}", savedUser.getId(), savedUser.getEmail());
        return "User registered successfully with ID: " + savedUser.getId();
    }

    public UserValidationResponse validateCredentials(LoginRequest req) {
        log.info("Validating credentials for email: {}", req.getEmail());
        Optional<User> userOptional = userRepository.findByEmail(req.getEmail());

        if (!userOptional.isPresent()) {
            log.warn("Login failed: User not found with email: {}", req.getEmail());
            throw new InvalidCredentialsException("Invalid email or password");
        }

        User user = userOptional.get();

        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            log.warn("Login failed: Invalid password for email: {}", req.getEmail());
            throw new InvalidCredentialsException("Invalid email or password");
        }

        if (user.getStatus() == User.Status.SUSPENDED) {
            log.warn("Login failed: Account suspended for email: {}", req.getEmail());
            throw new AccountSuspendedException("Your account has been suspended. Please contact support.");
        }

        if (user.getStatus() == User.Status.DELETED) {
            log.warn("Login failed: Account deleted for email: {}", req.getEmail());
            throw new UserNotFoundException("Account does not exist");
        }

        log.info("Credentials validated successfully for user ID: {}", user.getId());
        return new UserValidationResponse(user.getId(), user.getRole().name());
    }

    public User getUserById(String userId) {
        log.debug("Fetching user by ID: {}", userId);
        return userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("User not found with ID: {}", userId);
                    return new UserNotFoundException("User not found with ID: " + userId);
                });
    }

    @Transactional
    public User updateUser(String userId, User updateRequest) {
        log.info("Updating user with ID: {}", userId);
        User existingUser = getUserById(userId);

        // Block password updates (should use separate endpoint)
        if (updateRequest.getPassword() != null) {
            throw new InvalidRequestException("Cannot update password through this endpoint. Use password reset.");
        }

        // Block role updates (only admin should change roles)
        if (updateRequest.getRole() != null && updateRequest.getRole() != existingUser.getRole()) {
            throw new InvalidRequestException("Cannot change user role");
        }

        // Block status updates (only admin/internal should change status)
        if (updateRequest.getStatus() != null && updateRequest.getStatus() != existingUser.getStatus()) {
            throw new InvalidRequestException("Cannot change user status");
        }

        if (updateRequest.getFirstName() != null && !updateRequest.getFirstName().trim().isEmpty()) {
            existingUser.setFirstName(updateRequest.getFirstName().trim());
        }

        if (updateRequest.getLastName() != null && !updateRequest.getLastName().trim().isEmpty()) {
            existingUser.setLastName(updateRequest.getLastName().trim());
        }

        if (updateRequest.getPhoneNumber() != null && !updateRequest.getPhoneNumber().trim().isEmpty()) {
            // Validate phone number is unique
            if (!updateRequest.getPhoneNumber().equals(existingUser.getPhoneNumber())) {
                if (userRepository.existsByPhoneNumber(updateRequest.getPhoneNumber())) {
                    throw new UserAlreadyExistsException("Phone number already in use");
                }
                existingUser.setPhoneNumber(updateRequest.getPhoneNumber().trim());
            }
        }

        if (updateRequest.getCity() != null) {
            existingUser.setCity(updateRequest.getCity().trim());
        }

        if (updateRequest.getState() != null) {
            existingUser.setState(updateRequest.getState().trim());
        }

        User updatedUser = userRepository.save(existingUser);
        log.info("User updated successfully with ID: {}", userId);
        return updatedUser;
    }

    @Transactional
    public void updateUserStatus(String userId, User.Status newStatus) {
        log.info("Updating status for user ID: {} to {}", userId, newStatus);
        User user = getUserById(userId);
        user.setStatus(newStatus);
        userRepository.save(user);
        log.info("User status updated successfully for user ID: {}", userId);
    }


    // Add these methods to UserService.java

    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        log.info("Fetching all users");
        return userRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<User> getUsersByRole(User.Role role) {
        log.info("Fetching users by role: {}", role);
        return userRepository.findByRole(role);
    }

    @Transactional
    public void deleteUser(String userId) {
        log.info("Deleting user: {}", userId);
        User user = getUserById(userId);

        user.setStatus(User.Status.DELETED);
        userRepository.save(user);



        log.info("User deleted: {}", userId);
    }

    private void validateSignupRequest(SignupRequest request) {
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new InvalidRequestException("Email is required");
        }

        if (request.getPassword() == null || request.getPassword().length() < 6) {
            throw new InvalidRequestException("Password must be at least 6 characters");
        }

        if (request.getFirstName() == null || request.getFirstName().trim().isEmpty()) {
            throw new InvalidRequestException("First name is required");
        }

        // Better email validation
        String emailRegex = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        if (!request.getEmail().matches(emailRegex)) {
            throw new InvalidRequestException("Invalid email format");
        }

        // Validate role if provided
        if (request.getRole() != null && !request.getRole().trim().isEmpty()) {
            try {
                User.Role.valueOf(request.getRole().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new InvalidRequestException("Invalid role. Valid roles: RIDER, DRIVER, ADMIN");
            }
        }
    }
    private User.Role parseRole(String roleStr) {
        if (roleStr == null || roleStr.trim().isEmpty()) {
            return User.Role.RIDER;
        }

        try {
            return User.Role.valueOf(roleStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidRequestException("Invalid role: " + roleStr + ". Valid roles are: RIDER, DRIVER, ADMIN");
        }
    }
}