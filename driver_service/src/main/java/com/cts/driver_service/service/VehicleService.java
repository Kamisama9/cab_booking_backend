package com.cts.driver_service.service;

import com.cts.driver_service.dto.AdminVehicleVerificationRequest;
import com.cts.driver_service.dto.VehicleRequest;
import com.cts.driver_service.entity.Driver;
import com.cts.driver_service.entity.Vehicle;
import com.cts.driver_service.exception.DriverNotFoundException;
import com.cts.driver_service.exception.InvalidDateFormatException;
import com.cts.driver_service.repository.DriverRepository;
import com.cts.driver_service.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class VehicleService {

    private final VehicleRepository vehicleRepository;
    private final DriverRepository driverRepository;

    // ========================================
    // DRIVER VEHICLE METHODS
    // ========================================

    /**
     * Get all vehicles for a driver
     */
    public List<Vehicle> getDriverVehicles(String userId) {
        log.info("Fetching vehicles for userId: {}", userId);
        
        Driver driver = driverRepository.findByUserId(userId)
                .orElseThrow(() -> new DriverNotFoundException("Driver profile not found"));

        List<Vehicle> vehicles = vehicleRepository.findByDriverIdAndIsDeletedFalse(driver.getId());
        log.info("Found {} vehicles for driver {}", vehicles.size(), driver.getId());
        
        return vehicles;
    }

    /**
     * Get single vehicle details
     */
    public Vehicle getVehicle(String vehicleId, String userId) {
        log.info("Fetching vehicle {} for userId: {}", vehicleId, userId);
        
        Driver driver = driverRepository.findByUserId(userId)
                .orElseThrow(() -> new DriverNotFoundException("Driver profile not found"));

        return vehicleRepository.findByIdAndDriverIdAndIsDeletedFalse(vehicleId, driver.getId())
                .orElseThrow(() -> new RuntimeException("Vehicle not found"));
    }

    /**
     * Add new vehicle
     */
    @Transactional
    public Vehicle addVehicle(String userId, VehicleRequest request) {
        log.info("Adding vehicle for userId: {}", userId);

        // Validate driver exists
        Driver driver = driverRepository.findByUserId(userId)
                .orElseThrow(() -> new DriverNotFoundException("Driver profile not found"));

        // Validate registration number is unique
        if (vehicleRepository.existsByRegistrationNumberAndIsDeletedFalse(request.getRegistrationNumber())) {
            throw new RuntimeException("Vehicle with registration number " + request.getRegistrationNumber() + " already exists");
        }

        // Create vehicle
        Vehicle vehicle = new Vehicle();
        vehicle.setDriverId(driver.getId());
        vehicle.setRegistrationNumber(request.getRegistrationNumber().toUpperCase());

        // Parse vehicle type
        try {
            vehicle.setVehicleType(Vehicle.VehicleType.valueOf(request.getVehicleType().toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid vehicle type. Valid types: BIKE, AUTO, SEDAN, SUV");
        }

        // Set basic details
        vehicle.setManufacturer(request.getManufacturer());
        vehicle.setModel(request.getModel());
        vehicle.setYear(request.getYear());
        vehicle.setColor(request.getColor());

        // Set insurance details
        vehicle.setInsuranceNumber(request.getInsuranceNumber());
        if (request.getInsuranceExpiryDate() != null) {
            vehicle.setInsuranceExpiryDate(parseDate(request.getInsuranceExpiryDate()));
        }

        // Set RC details
        vehicle.setRcNumber(request.getRcNumber());
        if (request.getRcExpiryDate() != null) {
            vehicle.setRcExpiryDate(parseDate(request.getRcExpiryDate()));
        }

        // Set PUC details
        vehicle.setPucNumber(request.getPucNumber());
        if (request.getPucExpiryDate() != null) {
            vehicle.setPucExpiryDate(parseDate(request.getPucExpiryDate()));
        }

        // Set initial status
        vehicle.setVerificationStatus(Vehicle.VerificationStatus.PENDING);
        vehicle.setIsActive(true);
        vehicle.setIsDeleted(false);

        Vehicle saved = vehicleRepository.save(vehicle);
        log.info("Vehicle added successfully: {}", saved.getId());

        return saved;
    }

    /**
     * Update vehicle
     */
    @Transactional
    public Vehicle updateVehicle(String vehicleId, String userId, VehicleRequest request) {
        log.info("Updating vehicle: {} for userId: {}", vehicleId, userId);

        Driver driver = driverRepository.findByUserId(userId)
                .orElseThrow(() -> new DriverNotFoundException("Driver profile not found"));

        Vehicle vehicle = vehicleRepository.findByIdAndDriverIdAndIsDeletedFalse(vehicleId, driver.getId())
                .orElseThrow(() -> new RuntimeException("Vehicle not found"));

        // Cannot update if approved (security measure)
        if (vehicle.getVerificationStatus() == Vehicle.VerificationStatus.APPROVED) {
            throw new RuntimeException("Cannot update approved vehicle. Please add a new vehicle if changes are needed.");
        }

        // Update fields only if provided
        if (request.getManufacturer() != null) {
            vehicle.setManufacturer(request.getManufacturer());
        }
        if (request.getModel() != null) {
            vehicle.setModel(request.getModel());
        }
        if (request.getYear() != null) {
            vehicle.setYear(request.getYear());
        }
        if (request.getColor() != null) {
            vehicle.setColor(request.getColor());
        }

        // Update insurance
        if (request.getInsuranceNumber() != null) {
            vehicle.setInsuranceNumber(request.getInsuranceNumber());
        }
        if (request.getInsuranceExpiryDate() != null) {
            vehicle.setInsuranceExpiryDate(parseDate(request.getInsuranceExpiryDate()));
        }

        // Update RC
        if (request.getRcNumber() != null) {
            vehicle.setRcNumber(request.getRcNumber());
        }
        if (request.getRcExpiryDate() != null) {
            vehicle.setRcExpiryDate(parseDate(request.getRcExpiryDate()));
        }

        // Update PUC
        if (request.getPucNumber() != null) {
            vehicle.setPucNumber(request.getPucNumber());
        }
        if (request.getPucExpiryDate() != null) {
            vehicle.setPucExpiryDate(parseDate(request.getPucExpiryDate()));
        }

        Vehicle saved = vehicleRepository.save(vehicle);
        log.info("Vehicle {} updated successfully", vehicleId);
        
        return saved;
    }

    /**
     * Delete vehicle (soft delete)
     */
    @Transactional
    public void deleteVehicle(String vehicleId, String userId) {
        log.info("Deleting vehicle: {} for userId: {}", vehicleId, userId);

        Driver driver = driverRepository.findByUserId(userId)
                .orElseThrow(() -> new DriverNotFoundException("Driver profile not found"));

        Vehicle vehicle = vehicleRepository.findByIdAndDriverIdAndIsDeletedFalse(vehicleId, driver.getId())
                .orElseThrow(() -> new RuntimeException("Vehicle not found"));

        // Soft delete
        vehicle.setIsDeleted(true);
        vehicle.setIsActive(false);
        vehicleRepository.save(vehicle);
        
        log.info("Vehicle {} deleted successfully", vehicleId);
    }

    // ========================================
    // ADMIN VEHICLE METHODS
    // ========================================

    /**
     * Get vehicles by status with pagination
     */
    public Page<Vehicle> getVehiclesByStatus(Vehicle.VerificationStatus status, Pageable pageable) {
        log.info("Admin: Fetching vehicles by status: {} (page {}, size {})", 
                 status, pageable.getPageNumber(), pageable.getPageSize());
        
        return vehicleRepository.findByVerificationStatusAndIsDeletedFalse(status, pageable);
    }

    /**
     * Admin verify vehicle
     */
    @Transactional
    public Vehicle adminVerifyVehicle(String vehicleId, AdminVehicleVerificationRequest request) {
        log.info("Admin: Verifying vehicle {} with action: {}", vehicleId, request.getAction());

        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new RuntimeException("Vehicle not found"));

        switch (request.getAction().toUpperCase()) {
            case "APPROVE":
                vehicle.setVerificationStatus(Vehicle.VerificationStatus.APPROVED);
                vehicle.setIsActive(true);
                vehicle.setRejectionReason(null);
                log.info("Vehicle {} approved", vehicleId);
                break;

            case "REJECT":
                vehicle.setVerificationStatus(Vehicle.VerificationStatus.REJECTED);
                vehicle.setIsActive(false);
                vehicle.setRejectionReason(request.getRejectionReason());
                log.info("Vehicle {} rejected: {}", vehicleId, request.getRejectionReason());
                break;

            case "UNDER_REVIEW":
                vehicle.setVerificationStatus(Vehicle.VerificationStatus.UNDER_REVIEW);
                log.info("Vehicle {} marked under review", vehicleId);
                break;

            default:
                throw new IllegalArgumentException("Invalid action. Valid actions: APPROVE, REJECT, UNDER_REVIEW");
        }

        Vehicle saved = vehicleRepository.save(vehicle);
        log.info("Vehicle {} verification updated successfully", vehicleId);
        
        return saved;
    }

    // ========================================
    // HELPER METHODS
    // ========================================

    /**
     * Parse date string to LocalDate
     */
    private LocalDate parseDate(String dateStr) {
        try {
            return LocalDate.parse(dateStr);
        } catch (DateTimeParseException e) {
            throw new InvalidDateFormatException("Invalid date format. Expected: yyyy-MM-dd");
        }
    }

   public List<Vehicle> getVehiclesByDriverId(String driverId) {
    return vehicleRepository.findByDriverIdAndIsDeletedFalse(driverId);
}
}