package com.cts.driver_service.controller;

import com.cts.driver_service.dto.AdminVerificationRequest;
import com.cts.driver_service.dto.DriverResponse;
import com.cts.driver_service.entity.Driver;
import com.cts.driver_service.exception.InvalidStatusException;
import com.cts.driver_service.service.DriverService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/drivers")
@RequiredArgsConstructor
@Slf4j
public class AdminDriverController {

    private final DriverService driverService;

    /**
     * Get all drivers with pagination and optional filtering by status
     * GET /api/v1/admin/drivers?page=0&size=20&status=PENDING
     */
    @GetMapping
    public ResponseEntity<Page<DriverResponse>> listDrivers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status) {
        
        log.info("Admin: List drivers request - page: {}, size: {}, status: {}", page, size, status);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<DriverResponse> drivers;

        if (status != null && !status.isBlank()) {
            // Filter by status
            Driver.VerificationStatus verificationStatus;
            try {
                verificationStatus = Driver.VerificationStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new InvalidStatusException(
                    "Invalid verification status: " + status + 
                    ". Valid values: PENDING, UNDER_REVIEW, APPROVED, REJECTED"
                );
            }
            drivers = driverService.listByStatus(verificationStatus, pageable);
        } else {
            // Get all drivers
            drivers = driverService.listAll(pageable);
        }

        log.info("Admin: Returning {} drivers (page {} of {})", 
                 drivers.getNumberOfElements(), 
                 drivers.getNumber() + 1, 
                 drivers.getTotalPages());
        
        return ResponseEntity.ok(drivers);
    }

    /**
     * Get driver by ID with user details
     * GET /api/v1/admin/drivers/{driverId}
     */
    @GetMapping("/{driverId}")
    public ResponseEntity<DriverResponse> getDriver(@PathVariable String driverId) {
        log.info("Admin: Get driver request for ID: {}", driverId);
        
        DriverResponse driver = driverService.getById(driverId);
        
        log.info("Admin: Successfully retrieved driver: {}", driverId);
        return ResponseEntity.ok(driver);
    }

    /**
     * Verify driver (approve/reject/under_review)
     * PUT /api/v1/admin/drivers/{driverId}/verify
     * 
     * Request Body:
     * {
     *   "action": "APPROVE",  // or "REJECT" or "UNDER_REVIEW"
     *   "rejectionReason": "Optional reason if rejecting"
     * }
     */
    @PutMapping("/{driverId}/verify")
    public ResponseEntity<DriverResponse> verifyDriver(
            @PathVariable String driverId,
            @Valid @RequestBody AdminVerificationRequest verificationRequest) {
        
        log.info("Admin: Verify driver {} with action: {}", driverId, verificationRequest.getAction());

        DriverResponse driver = driverService.adminVerifyDriver(driverId, verificationRequest);
        
        log.info("Admin: Driver {} verification completed with status: {}", 
                 driverId, driver.getVerificationStatus());
        
        return ResponseEntity.ok(driver);
    }

    /**
     * Delete driver
     * DELETE /api/v1/admin/drivers/{driverId}
     */
    @DeleteMapping("/{driverId}")
    public ResponseEntity<Void> deleteDriver(@PathVariable String driverId) {
        log.info("Admin: Delete driver request for ID: {}", driverId);
        
        // Note: This expects driverId, but service uses userId
        // You may need to fetch driver first to get userId, or modify service
        driverService.deleteDriverById(driverId);
        
        log.info("Admin: Driver {} deleted successfully", driverId);
        return ResponseEntity.noContent().build();
    }
}