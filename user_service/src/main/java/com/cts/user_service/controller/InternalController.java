package com.cts.user_service.controller;

import com.cts.user_service.dto.LoginRequest;
import com.cts.user_service.dto.SignupRequest;
import com.cts.user_service.dto.UserValidationResponse;
import com.cts.user_service.entity.User;
import com.cts.user_service.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/internal")
@RequiredArgsConstructor
public class InternalController {

    private final UserService userService;

    @PostMapping("/validate-credentials")
    public ResponseEntity<UserValidationResponse> validateCredentials(@RequestBody LoginRequest req) {
        UserValidationResponse response = userService.validateCredentials(req);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> registerUser(@RequestBody SignupRequest request) {
        String message = userService.registerUser(request);
        return new ResponseEntity<>(Map.of("message", message), HttpStatus.CREATED);
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<User> getUserById(@PathVariable String userId) {
        User user = userService.getUserById(userId);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @PutMapping("/users/{userId}/status")
    public ResponseEntity<Map<String, String>> updateUserStatus(
            @PathVariable String userId,
            @RequestBody Map<String, String> statusUpdate) {

        String newStatusStr = statusUpdate.get("status");
        if (newStatusStr == null || newStatusStr.trim().isEmpty()) {
            return new ResponseEntity<>(
                Map.of("message", "Status field is required"),
                HttpStatus.BAD_REQUEST
            );
        }

        User.Status status = User.Status.valueOf(newStatusStr.toUpperCase());
        userService.updateUserStatus(userId, status);

        return new ResponseEntity<>(
            Map.of("message", "User status updated successfully"),
            HttpStatus.OK
        );
    }
}
