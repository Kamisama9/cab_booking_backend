package com.cts.driver_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "vehicles")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String driverId; // Foreign key to Driver

    @Column(nullable = false, unique = true)
    private String registrationNumber; // e.g., MH01AB1234

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VehicleType vehicleType;

    public enum VehicleType {
        BIKE,
        AUTO,
        SEDAN,
        SUV
    }

    private String manufacturer; // e.g., Honda, Toyota
    private String model; // e.g., City, Innova
    private Integer year; // Manufacturing year
    private String color;

    // Insurance details
    private String insuranceNumber;
    private LocalDate insuranceExpiryDate;

    // Registration certificate details
    private String rcNumber;
    private LocalDate rcExpiryDate;

    // Pollution certificate
    private String pucNumber;
    private LocalDate pucExpiryDate;

    // Document paths (uploaded images)
    private String rcFrontPath;
    private String rcBackPath;
    private String insurancePath;
    private String pucPath;
    private String vehiclePhotoPath;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VerificationStatus verificationStatus = VerificationStatus.PENDING;

    public enum VerificationStatus {
        PENDING,       // Just created
        UNDER_REVIEW,  // Admin reviewing
        APPROVED,      // Verified and active
        REJECTED       // Rejected with reason
    }

    private String rejectionReason; // If rejected

    private Boolean isActive = true; // Can be used for rides
    private Boolean isDeleted = false; // Soft delete

    @CreationTimestamp
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    private OffsetDateTime updatedAt;
}