package com.cts.user_service.controller;

import com.cts.user_service.entity.User;
import com.cts.user_service.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
@Slf4j
public class AdminUserController {

    private final UserService userService;

    /**
     * Get all users (Admin only) - Legacy endpoint
     * GET /api/v1/admin/users
     */
    @GetMapping
    public ResponseEntity<?> getAllUsers(
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        log.info("Admin: Fetching users - role: {}, status: {}, page: {}, size: {}", role, status, page, size);

        // ✅ If pagination params are provided, use paginated response
        if (page >= 0 && size > 0) {
            if (role != null && !role.trim().isEmpty()) {
                try {
                    User.Role userRole = User.Role.valueOf(role.toUpperCase());
                    Page<User> usersPage = userService.getUsersByRolePaginated(userRole, page, size);
                    return new ResponseEntity<>(usersPage, HttpStatus.OK);
                } catch (IllegalArgumentException e) {
                    log.error("Invalid role: {}", role);
                    return new ResponseEntity<>(
                        Map.of("message", "Invalid role. Valid values: RIDER, DRIVER, ADMIN"),
                        HttpStatus.BAD_REQUEST
                    );
                }
            } else if (status != null && !status.trim().isEmpty()) {
                try {
                    User.Status userStatus = User.Status.valueOf(status.toUpperCase());
                    Page<User> usersPage = userService.getUsersByStatusPaginated(userStatus, page, size);
                    return new ResponseEntity<>(usersPage, HttpStatus.OK);
                } catch (IllegalArgumentException e) {
                    log.error("Invalid status: {}", status);
                    return new ResponseEntity<>(
                        Map.of("message", "Invalid status. Valid values: ACTIVE, SUSPENDED, PENDING_VERIFICATION, DELETED"),
                        HttpStatus.BAD_REQUEST
                    );
                }
            } else {
                Page<User> usersPage = userService.getAllUsersPaginated(page, size);
                return new ResponseEntity<>(usersPage, HttpStatus.OK);
            }
        }

        // ✅ Legacy: If no pagination params, return full list
        if (role != null && !role.trim().isEmpty()) {
            try {
                User.Role userRole = User.Role.valueOf(role.toUpperCase());
                List<User> users = userService.getUsersByRole(userRole);
                return new ResponseEntity<>(users, HttpStatus.OK);
            } catch (IllegalArgumentException e) {
                log.error("Invalid role: {}", role);
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
        }

        List<User> users = userService.getAllUsers();
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    /**
     * Get user by ID (Admin only)
     * GET /api/v1/admin/users/{userId}
     */
    @GetMapping("/{userId}")
    public ResponseEntity<User> getUserById(@PathVariable String userId) {
        log.info("Admin: Fetching user by ID: {}", userId);
        User user = userService.getUserById(userId);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    /**
     * Update user (Admin only)
     * PUT /api/v1/admin/users/{userId}
     */
    @PutMapping("/{userId}")
    public ResponseEntity<User> updateUser(
            @PathVariable String userId,
            @RequestBody User user) {
        log.info("Admin: Updating user: {}", userId);
        User updatedUser = userService.updateUser(userId, user);
        return new ResponseEntity<>(updatedUser, HttpStatus.OK);
    }

    /**
     * Update user status (Admin only)
     * PUT /api/v1/admin/users/{userId}/status
     */
    @PutMapping("/{userId}/status")
    public ResponseEntity<Map<String, String>> updateUserStatus(
            @PathVariable String userId,
            @RequestBody Map<String, String> statusUpdate) {
        log.info("Admin: Updating status for user: {}", userId);

        String newStatusStr = statusUpdate.get("status");
        if (newStatusStr == null || newStatusStr.trim().isEmpty()) {
            return new ResponseEntity<>(
                    Map.of("message", "Status field is required"),
                    HttpStatus.BAD_REQUEST
            );
        }

        try {
            User.Status status = User.Status.valueOf(newStatusStr.toUpperCase());
            userService.updateUserStatus(userId, status);
            return new ResponseEntity<>(
                    Map.of("message", "User status updated successfully"),
                    HttpStatus.OK
            );
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(
                    Map.of("message", "Invalid status. Valid values: ACTIVE, SUSPENDED, PENDING_VERIFICATION, DELETED"),
                    HttpStatus.BAD_REQUEST
            );
        }
    }

    /**
     * Delete user (Admin only)
     * DELETE /api/v1/admin/users/{userId}
     */
    @DeleteMapping("/{userId}")
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable String userId) {
        log.info("Admin: Deleting user: {}", userId);
        userService.deleteUser(userId);
        return new ResponseEntity<>(
                Map.of("message", "User deleted successfully"),
                HttpStatus.OK
        );
    }
}