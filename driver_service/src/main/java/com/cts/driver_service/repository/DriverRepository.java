package com.cts.driver_service.repository;

import com.cts.driver_service.entity.Driver;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DriverRepository extends JpaRepository<Driver, String> {
    
    /**
     * Find driver by userId
     */
    Optional<Driver> findByUserId(String userId);

    /**
     * Find drivers by verification status with pagination
     */
    Page<Driver> findByVerificationStatus(Driver.VerificationStatus status, Pageable pageable);
}