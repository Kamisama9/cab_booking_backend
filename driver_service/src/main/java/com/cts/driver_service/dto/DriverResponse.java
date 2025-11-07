package com.cts.driver_service.dto;

import com.cts.driver_service.entity.Driver;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DriverResponse {
    // Driver-specific fields
    private String id;
    private String userId;
    private String aadhaarNumber;
    private String dlNumber;
    private LocalDate dlExpiryDate;
    private String verificationStatus; // Changed to String for easier JSON handling
    private Boolean isAvailable;
    private Boolean docsSubmitted;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    
    // User details from User Service
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String city;
    private String state;
    
    /**
     * Convert Driver entity to DriverResponse DTO
     */
    public static DriverResponse fromEntity(Driver driver, UserResponse userDetails) {
        DriverResponseBuilder builder = DriverResponse.builder()
                .id(driver.getId())
                .userId(driver.getUserId())
                .aadhaarNumber(driver.getAadhaarNumber())
                .dlNumber(driver.getDlNumber())
                .dlExpiryDate(driver.getDlExpiryDate())
                .verificationStatus(driver.getVerificationStatus() != null 
                    ? driver.getVerificationStatus().name() 
                    : "PENDING")
                .isAvailable(driver.isAvailable())
                .docsSubmitted(driver.isDocsSubmitted())
                .createdAt(driver.getCreatedAt())
                .updatedAt(driver.getUpdatedAt());
        
        // Add user details if available
        if (userDetails != null) {
            builder
                .firstName(userDetails.getFirst_name())
                .lastName(userDetails.getLast_name())
                .email(userDetails.getEmail())
                .phoneNumber(userDetails.getPhoneNumber())
                .city(userDetails.getCity())
                .state(userDetails.getState());
        }
        
        return builder.build();
    }
}