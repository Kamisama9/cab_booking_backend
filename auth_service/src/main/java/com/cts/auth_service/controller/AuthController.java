package com.cts.auth_service.controller;

import com.cts.auth_service.dto.AuthResponse;
import com.cts.auth_service.dto.LoginRequest;
import com.cts.auth_service.dto.SignupRequest;
import com.cts.auth_service.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/auth")
public class AuthController {


    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    //get email and password from request body to LoginRequest and return AuthResponse with token
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {

        AuthResponse response = authService.login(request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/signup")
    //get details from request body to SignupRequest and return success message
    public ResponseEntity<String> signup(@RequestBody SignupRequest request) {
        String response = authService.signup(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

}
