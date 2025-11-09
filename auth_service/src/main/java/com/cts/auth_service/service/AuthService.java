package com.cts.auth_service.service;

import com.cts.auth_service.dao.UserDao;
import com.cts.auth_service.dto.AuthResponse;
import com.cts.auth_service.dto.LoginRequest;
import com.cts.auth_service.dto.SignupRequest;
import com.cts.auth_service.dto.UserValidationResponse;
import com.cts.auth_service.exception.AuthenticationException;
import com.cts.auth_service.exception.UserServiceException;
import com.cts.auth_service.exception.ValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class AuthService {


    @Autowired
    private UserDao userDao;

    @Autowired
    private JwtService jwtService;

    //service for login method
    public AuthResponse login(LoginRequest request) {

        if (request.getEmail() == null || request.getEmail().isEmpty()) {
            throw new ValidationException("Email is required for login");
        }

        if (request.getPassword() == null || request.getPassword().isEmpty()) {
            throw new ValidationException("Password is required for login");
        }

        //call userDao to validate user credentials
        ResponseEntity<UserValidationResponse> loginResponse = userDao.validateUserCredentials(request);

        if (!loginResponse.getStatusCode().is2xxSuccessful() || loginResponse.getBody() == null) {
            throw new AuthenticationException("Invalid email or password. Please check your credentials and try again.");
        }

        UserValidationResponse validationResponse = loginResponse.getBody();

        if (validationResponse.getUserId() == null || validationResponse.getRole() == null) {
            throw new AuthenticationException("Invalid email or password. Please check your credentials and try again.");
        }


        String token = jwtService.generateToken(
                validationResponse.getUserId(),
                validationResponse.getRole()
        );

        return new AuthResponse(token);
    }

    public String signup(SignupRequest request) {

        ResponseEntity<String> responseEntity = userDao.registerUser(request);

        if (!responseEntity.getStatusCode().is2xxSuccessful() || responseEntity.getBody() == null) {
            throw new ValidationException("User with this email already exists or invalid data provided");
        }

        return "User registered successfully. Please login to continue.";
    }

}