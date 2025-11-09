package com.cts.user_service.controller;

import com.cts.user_service.entity.User;
import com.cts.user_service.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    @GetMapping("/users/me")
    public ResponseEntity<User> getCurrentUser(HttpServletRequest request) {
        String userId = request.getHeader("X-User-Id");

        if (userId == null || userId.trim().isEmpty()) {
            log.warn("Unauthorized access attempt: Missing X-User-Id header");
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        log.info("Fetching current user profile for user ID: {}", userId);
        User user = userService.getUserById(userId);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @PutMapping("/users/me")
    public ResponseEntity<User> updateCurrentUser(
            @RequestBody User user,
            HttpServletRequest request) {
        String userId = request.getHeader("X-User-Id");

        if (userId == null || userId.trim().isEmpty()) {
            log.warn("Unauthorized update attempt: Missing X-User-Id header");
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        log.info("Updating user profile for user ID: {}", userId);
        User updatedUser = userService.updateUser(userId, user);
        return new ResponseEntity<>(updatedUser, HttpStatus.OK);
    }
}