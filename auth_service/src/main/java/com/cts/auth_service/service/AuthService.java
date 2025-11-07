package com.cts.auth_service.service;

import com.cts.auth_service.dao.UserDao;
import com.cts.auth_service.dto.AuthResponse;
import com.cts.auth_service.dto.LoginRequest;
import com.cts.auth_service.dto.SignupRequest;
import com.cts.auth_service.dto.UserValidationResponse;
import com.cts.auth_service.exception.AuthenticationException;
import com.cts.auth_service.exception.UserServiceException;
import com.cts.auth_service.exception.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    @Autowired
    private UserDao userDao;

    @Autowired
    private JwtService jwtService;

    public AuthResponse login(LoginRequest request) {
        log.info("AuthService: Attempting login for {}", request.getEmail());

        if (request.getEmail() == null || request.getEmail().isEmpty()) {
            throw new ValidationException("Email is required for login");
        }

        if (request.getPassword() == null || request.getPassword().isEmpty()) {
            throw new ValidationException("Password is required for login");
        }

        ResponseEntity<UserValidationResponse> responseEntity = userDao.validateUserCredentials(request);

        if (!responseEntity.getStatusCode().is2xxSuccessful() || responseEntity.getBody() == null) {
            log.warn("Login failed for user: {} - Invalid credentials", request.getEmail());
            throw new AuthenticationException("Invalid email or password. Please check your credentials and try again.");
        }

        UserValidationResponse validationResponse = responseEntity.getBody();

        if (validationResponse.getUserId() == null || validationResponse.getRole() == null) {
            log.warn("Login failed for user: {} - Invalid response data", request.getEmail());
            throw new AuthenticationException("Invalid email or password. Please check your credentials and try again.");
        }

        log.info("Login successful for user: {} with role: {}", request.getEmail(), validationResponse.getRole());

        String token = jwtService.generateToken(
                validationResponse.getUserId(),
                validationResponse.getRole()
        );

        return new AuthResponse(token);
    }

    public String signup(SignupRequest request) {
        log.info("AuthService: Attempting signup for {}", request.getEmail());

        if (request.getEmail() == null || request.getEmail().isEmpty()) {
            throw new ValidationException("Email is required for signup");
        }

        if (request.getPassword() == null || request.getPassword().isEmpty()) {
            throw new ValidationException("Password is required for signup");
        }

        if (request.getFirstName() == null || request.getFirstName().isEmpty()) {
            throw new ValidationException("First name is required for signup");
        }

        if (request.getLastName() == null || request.getLastName().isEmpty()) {
            throw new ValidationException("Last name is required for signup");
        }

        if (request.getRole() == null || request.getRole().isEmpty()) {
            request.setRole("RIDER");
            log.info("No role specified, defaulting to RIDER");
        }

        ResponseEntity<String> responseEntity = userDao.registerUser(request);

        if (!responseEntity.getStatusCode().is2xxSuccessful() || responseEntity.getBody() == null) {
            log.warn("Signup failed for user: {} - User may already exist", request.getEmail());
            throw new ValidationException("User with this email already exists or invalid data provided");
        }

        log.info("Signup successful for user: {}", request.getEmail());
        return "User registered successfully. Please login to continue.";
    }

}