package com.cts.auth_service.dao;

import com.cts.auth_service.dto.LoginRequest;
import com.cts.auth_service.dto.SignupRequest;
import com.cts.auth_service.dto.UserValidationResponse;
import org.springframework.http.ResponseEntity;

public interface UserDao {
    ResponseEntity<UserValidationResponse> validateUserCredentials(LoginRequest request);
    ResponseEntity<String> registerUser(SignupRequest request);
}

