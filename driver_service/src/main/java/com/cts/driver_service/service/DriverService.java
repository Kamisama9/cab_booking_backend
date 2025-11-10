package com.cts.driver_service.service;

import com.cts.driver_service.client.UserServiceClient;
import com.cts.driver_service.dto.*;
import com.cts.driver_service.entity.Driver;
import com.cts.driver_service.entity.Vehicle;
import com.cts.driver_service.exception.*;
import com.cts.driver_service.repository.DriverRepository;
import com.cts.driver_service.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class DriverService {

    private final DriverRepository driverRepository;
    private final VehicleRepository vehicleRepository;
    private final UserServiceClient userServiceClient;

    // ========================================
    // DRIVER METHODS (returns DriverResponse with user details)
    // ========================================

    /**
     * Get driver profile with user details
     */
    public DriverResponse getDriverProfile(String userId) {
        log.info("Fetching driver profile for userId: {}", userId);
        Driver driver = driverRepository.findByUserId(userId)
                .orElseThrow(() -> new DriverNotFoundException("Driver profile not found for userId: " + userId));
        
        return enrichWithUserDetails(driver);
    }

    /**
     * Create or update driver profile
     */
    @Transactional
    public Driver createOrUpdateProfile(String userId, DriverProfileRequest req) {
        log.info("Creating/Updating driver profile for userId: {}", userId);

        Driver driver = driverRepository.findByUserId(userId)
                .orElseGet(() -> {
                    Driver newDriver = new Driver();
                    newDriver.setUserId(userId);
                    newDriver.setVerificationStatus(Driver.VerificationStatus.PENDING);
                    newDriver.setDocsSubmitted(false);
                    newDriver.setAvailable(false);
                    log.info("Creating new driver profile for userId: {}", userId);
                    return newDriver;
                });

        // Update Aadhaar number
        if (req.getAadhaarNumber() != null && !req.getAadhaarNumber().isBlank()) {
            driver.setAadhaarNumber(req.getAadhaarNumber());
        }

        // Update DL number
        if (req.getDlNumber() != null && !req.getDlNumber().isBlank()) {
            driver.setDlNumber(req.getDlNumber());
        }

        // Update DL expiry date
        if (req.getDlExpiryDate() != null && !req.getDlExpiryDate().isBlank()) {
            try {
                LocalDate expiryDate = LocalDate.parse(req.getDlExpiryDate());

                if (expiryDate.isBefore(LocalDate.now())) {
                    throw new InvalidDateFormatException("Driving license has expired. Expiry date cannot be in the past.");
                }

                driver.setDlExpiryDate(expiryDate);
            } catch (DateTimeParseException e) {
                throw new InvalidDateFormatException("Invalid date format for DL expiry date. Expected format: yyyy-MM-dd");
            }
        }

        Driver saved = driverRepository.save(driver);
        log.info("Driver profile saved successfully for userId: {}", userId);
        return saved;
    }

    /**
     * Get all drivers with pagination
     */
    public Page<DriverResponse> listAll(Pageable pageable) {
        log.info("Admin: Fetching drivers page {} with size {}", pageable.getPageNumber(), pageable.getPageSize());
        Page<Driver> driversPage = driverRepository.findAll(pageable);
        
        return driversPage.map(this::enrichWithUserDetails);
    }

    /**
     * Get drivers by verification status with pagination
     */
    public Page<DriverResponse> listByStatus(Driver.VerificationStatus status, Pageable pageable) {
        log.info("Admin: Fetching drivers by status: {} (page {}, size {})", 
                 status, pageable.getPageNumber(), pageable.getPageSize());
        
        Page<Driver> driversPage = driverRepository.findByVerificationStatus(status, pageable);
        
        return driversPage.map(this::enrichWithUserDetails);
    }

    /**
     * Get driver by ID
     */
    public DriverResponse getById(String driverId) {
        log.info("Admin: Fetching driver by ID: {}", driverId);
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new DriverNotFoundException("Driver not found with id: " + driverId));
        
        return enrichWithUserDetails(driver);
    }

    /**
     * Admin verify driver (approve/reject/under_review)
     */
    @Transactional
    public DriverResponse adminVerifyDriver(String driverId, AdminVerificationRequest req) {
        log.info("Admin: Verifying driver {} with action: {}", driverId, req.getAction());
        
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new DriverNotFoundException("Driver not found with id: " + driverId));

        switch (req.getAction().toUpperCase()) {
            case "APPROVE":
                driver.setVerificationStatus(Driver.VerificationStatus.APPROVED);
                driver.setUpdatedAt(OffsetDateTime.now());

                // Update user status to ACTIVE in User Service
                try {
                    userServiceClient.updateUserStatus(
                            driver.getUserId(),
                            Map.of("status", "ACTIVE")
                    );
                    log.info("User account activated for driver: {}", driverId);
                } catch (Exception e) {
                    log.error("Failed to activate user account for driver {}: {}", driverId, e.getMessage());
                    throw new RuntimeException("Failed to activate user account: " + e.getMessage());
                }
                break;

            case "REJECT":
                driver.setVerificationStatus(Driver.VerificationStatus.REJECTED);
                driver.setUpdatedAt(OffsetDateTime.now());
                log.info("Driver {} rejected", driverId);
                break;

            case "UNDER_REVIEW":
                driver.setVerificationStatus(Driver.VerificationStatus.UNDER_REVIEW);
                log.info("Driver {} marked under review", driverId);
                break;

            default:
                throw new IllegalArgumentException(
                    "Invalid action: " + req.getAction() + ". Valid actions are: APPROVE, REJECT, UNDER_REVIEW"
                );
        }

        Driver saved = driverRepository.save(driver);
        log.info("Driver {} verification updated successfully", driverId);
        
        return enrichWithUserDetails(saved);
    }


    // ========================================
    // HELPER METHOD - Fetch user details and merge
    // ========================================

    /**
     * Enrich driver entity with user details from User Service
     */
    private DriverResponse enrichWithUserDetails(Driver driver) {
        UserResponse userDetails = null;
        
        try {
            log.debug("Fetching user details for driver: {}", driver.getId());
            userDetails = userServiceClient.getUserById(driver.getUserId());
            log.debug("Successfully fetched user details for driver: {}", driver.getId());
        } catch (Exception e) {
            log.warn("Failed to fetch user details for driver: {}. Error: {}", driver.getId(), e.getMessage());
            // Continue without user details - don't fail the entire request
        }
        
        return DriverResponse.fromEntity(driver, userDetails);
    }

    @Transactional
public void deleteDriverById(String driverId) { // Rename method
    Driver driver = driverRepository.findById(driverId)
        .orElseThrow(() -> new DriverNotFoundException("Driver not found"));
    
    // Soft delete vehicles
    vehicleRepository.findByDriverIdAndIsDeletedFalse(driverId)
        .forEach(v -> {
            v.setIsDeleted(true);
            vehicleRepository.save(v);
        });
    
    driverRepository.deleteById(driverId);
    log.info("Deleted driver {}", driverId);
}

}