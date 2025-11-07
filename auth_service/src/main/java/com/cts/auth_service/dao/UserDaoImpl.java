package com.cts.auth_service.dao;

import com.cts.auth_service.client.UserServiceClient;
import com.cts.auth_service.dto.LoginRequest;
import com.cts.auth_service.dto.SignupRequest;
import com.cts.auth_service.dto.UserValidationResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;

@Repository
public class UserDaoImpl implements UserDao {

    @Autowired
    private UserServiceClient userServiceClient;

    @Override
    public ResponseEntity<UserValidationResponse> validateUserCredentials(LoginRequest request) {
        return userServiceClient.validateCredentials(request);
    }

    @Override
    public ResponseEntity<String> registerUser(SignupRequest request) {
        return userServiceClient.registerUser(request);
    }
}

