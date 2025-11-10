package com.cts.user_service.service;

import com.cts.user_service.dto.LoginRequest;
import com.cts.user_service.dto.SignupRequest;
import com.cts.user_service.dto.UserValidationResponse;
import com.cts.user_service.entity.User;
import com.cts.user_service.exception.*;
import com.cts.user_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public String registerUser(SignupRequest request) {
        validateSignupRequest(request);

        Optional<User> existingUser = userRepository.findByEmail(request.getEmail());
        if (existingUser.isPresent()) {
            throw new UserAlreadyExistsException("User already exists with email: " + request.getEmail());
        }

        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(parseRole(request.getRole()));
        if(request.getRole().equals("RIDER"))
        {
            user.setStatus(User.Status.ACTIVE);
        }
        else
        {
            user.setStatus(User.Status.PENDING_VERIFICATION);
        }
        user.setEmailVerified(false);

        User savedUser = userRepository.save(user);
        return "User registered successfully with ID: " + savedUser.getId();
    }

    public UserValidationResponse validateCredentials(LoginRequest req) {
        Optional<User> userOptional = userRepository.findByEmail(req.getEmail());

        if (!userOptional.isPresent()) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        User user = userOptional.get();

        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        if (user.getStatus() == User.Status.SUSPENDED) {
            throw new AccountSuspendedException("Your account has been suspended. Please contact support.");
        }

        if (user.getStatus() == User.Status.DELETED) {
            throw new UserNotFoundException("Account does not exist");
        }

        return new UserValidationResponse(user.getId(), user.getRole().name());
    }

    public User getUserById(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));
    }

    @Transactional
    public User updateUser(String userId, User updateRequest) {
        User existingUser = getUserById(userId);

        if (updateRequest.getPassword() != null) {
            throw new InvalidRequestException("Cannot update password through this endpoint. Use password reset.");
        }

        if (updateRequest.getRole() != null && updateRequest.getRole() != existingUser.getRole()) {
            throw new InvalidRequestException("Cannot change user role");
        }

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

        return userRepository.save(existingUser);
    }

    @Transactional
    public void updateUserStatus(String userId, User.Status newStatus) {
        User user = getUserById(userId);
        user.setStatus(newStatus);
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<User> getUsersByRole(User.Role role) {
        return userRepository.findByRole(role);
    }

    @Transactional(readOnly = true)
    public Page<User> getAllUsersPaginated(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return userRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<User> getUsersByRolePaginated(User.Role role, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return userRepository.findByRole(role, pageable);
    }

    @Transactional(readOnly = true)
    public Page<User> getUsersByStatusPaginated(User.Status status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return userRepository.findByStatus(status, pageable);
    }

    @Transactional
    public void deleteUser(String userId) {
        User user = getUserById(userId);
        user.setStatus(User.Status.DELETED);
        userRepository.save(user);
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

        String emailRegex = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        if (!request.getEmail().matches(emailRegex)) {
            throw new InvalidRequestException("Invalid email format");
        }

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