package com.cts.driver_service.controller;

import com.cts.driver_service.dto.DriverAvailabilityRequest;
import com.cts.driver_service.dto.DriverProfileRequest;
import com.cts.driver_service.dto.DriverResponse;
import com.cts.driver_service.entity.Driver;
import com.cts.driver_service.exception.UnauthorizedException;
import com.cts.driver_service.service.DriverService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/drivers")
@RequiredArgsConstructor
@Slf4j
public class DriverController {

    private final DriverService driverService;

    /**
     * Get driver profile with user details
     * GET /api/v1/drivers/me
     * 
     * Requires: X-User-Id header (set by API Gateway after JWT validation)
     */
    @GetMapping("/me")
    public ResponseEntity<DriverResponse> getDriverDetails(HttpServletRequest request) {
        String userId = extractUserId(request);
        
        log.info("Driver: Get profile request for userId: {}", userId);
        DriverResponse driver = driverService.getDriverProfile(userId);
        
        return ResponseEntity.ok(driver);
    }

    /**
     * Create driver profile
     * POST /api/v1/drivers/me/profile
     * 
     * Request Body:
     * {
     *   "aadhaarNumber": "123456789012",
     *   "dlNumber": "MH01-2023-1234567",
     *   "dlExpiryDate": "2030-12-31"
     * }
     */
    @PostMapping("/me/profile")
    public ResponseEntity<Driver> createProfile(
            HttpServletRequest request,
            @Valid @RequestBody DriverProfileRequest profileRequest) {

        String userId = extractUserId(request);
        
        log.info("Driver: Create profile request for userId: {}", userId);
        Driver driver = driverService.createOrUpdateProfile(userId, profileRequest);
        
        log.info("Driver: Profile created successfully for userId: {}", userId);
        return new ResponseEntity<>(driver, HttpStatus.CREATED);
    }

    /**
     * Update driver profile
     * PUT /api/v1/drivers/me/profile
     */
    @PutMapping("/me/profile")
    public ResponseEntity<Driver> updateProfile(
            HttpServletRequest request,
            @Valid @RequestBody DriverProfileRequest profileRequest) {

        String userId = extractUserId(request);
        
        log.info("Driver: Update profile request for userId: {}", userId);
        Driver driver = driverService.createOrUpdateProfile(userId, profileRequest);
        
        log.info("Driver: Profile updated successfully for userId: {}", userId);
        return ResponseEntity.ok(driver);
    }

    /**
     * Update driver availability
     * PUT /api/v1/drivers/me/availability
     * 
     * Request Body:
     * {
     *   "isAvailable": true
     * }
     */
    @PutMapping("/me/availability")
    public ResponseEntity<Driver> setAvailability(
            HttpServletRequest request,
            @Valid @RequestBody DriverAvailabilityRequest availabilityRequest) {

        String userId = extractUserId(request);
        
        log.info("Driver: Update availability request for userId: {} to {}", 
                 userId, availabilityRequest.getIsAvailable());
        
        Driver driver = driverService.setAvailability(userId, availabilityRequest);
        
        log.info("Driver: Availability updated successfully for userId: {}", userId);
        return ResponseEntity.ok(driver);
    }

    /**
     * Helper method to extract userId from request header
     */
    private String extractUserId(HttpServletRequest request) {
        String userId = request.getHeader("X-User-Id");
        if (userId == null || userId.isBlank()) {
            log.error("Missing or invalid X-User-Id header in request");
            throw new UnauthorizedException("Missing or invalid X-User-Id header");
        }
        return userId;
    }
}