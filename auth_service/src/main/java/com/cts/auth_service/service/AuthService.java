package com.cts.auth_service.service;

import com.cts.auth_service.client.UserServiceClient;
import com.cts.auth_service.dto.AuthResponse;
import com.cts.auth_service.dto.LoginRequest;
import com.cts.auth_service.dto.SignupRequest;
import com.cts.auth_service.dto.UserValidationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserServiceClient userServiceClient;
    private final JwtService jwtService;

    public AuthResponse login(LoginRequest request) {
        log.info("Login attempt for email: {}", request.getEmail());
        
        // Let Feign client handle errors
        UserValidationResponse validation = userServiceClient
            .validateCredentials(request)
            .getBody();
        
        String token = jwtService.generateToken(
            validation.getUserId(), 
            validation.getRole()
        );
        
        log.info("Login successful for user: {}", validation.getUserId());
        
        return new AuthResponse(token, validation.getUserId(), validation.getRole());
    }

    public String signup(SignupRequest request) {
        log.info("Signup attempt for email: {}", request.getEmail());
        
        // Let Feign client handle errors
        String response = userServiceClient
            .registerUser(request)
            .getBody();
        
        log.info("Signup successful for email: {}", request.getEmail());
        
        return response;
    }
}