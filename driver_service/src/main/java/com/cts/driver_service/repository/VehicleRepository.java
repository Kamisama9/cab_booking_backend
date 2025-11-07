package com.cts.driver_service.repository;

import com.cts.driver_service.entity.Vehicle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, String> {

    /**
     * Find all vehicles by driver ID (excluding soft-deleted)
     */
    List<Vehicle> findByDriverIdAndIsDeletedFalse(String driverId);

    /**
     * Find single vehicle by ID and driver ID (excluding soft-deleted)
     */
    Optional<Vehicle> findByIdAndDriverIdAndIsDeletedFalse(String id, String driverId);

    /**
     * Find vehicle by registration number (excluding soft-deleted)
     */
    Optional<Vehicle> findByRegistrationNumberAndIsDeletedFalse(String registrationNumber);

    /**
     * Find vehicles by verification status with pagination (excluding soft-deleted)
     */
    Page<Vehicle> findByVerificationStatusAndIsDeletedFalse(
            Vehicle.VerificationStatus status, 
            Pageable pageable
    );

    /**
     * Check if registration number exists (excluding soft-deleted)
     */
    boolean existsByRegistrationNumberAndIsDeletedFalse(String registrationNumber);
}