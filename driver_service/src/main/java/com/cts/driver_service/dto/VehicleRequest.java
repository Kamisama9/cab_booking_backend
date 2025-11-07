package com.cts.driver_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VehicleRequest {
    
    @NotBlank(message = "Registration number is required")
    @Pattern(
        regexp = "^[A-Z]{2}[0-9]{2}[A-Z]{1,2}[0-9]{4}$", 
        message = "Invalid registration number format. Example: MH01AB1234"
    )
    private String registrationNumber;
    
    @NotBlank(message = "Vehicle type is required")
    @Pattern(
        regexp = "BIKE|AUTO|SEDAN|SUV", 
        message = "Invalid vehicle type. Valid values: BIKE, AUTO, SEDAN, SUV"
    )
    private String vehicleType;
    
    private String manufacturer; // Optional
    
    private String model; // Optional
    
    @Min(value = 1980, message = "Year must be after 1980")
    @Max(value = 2030, message = "Year cannot be in the future")
    private Integer year; // Optional
    
    private String color; // Optional

    // Insurance details
    private String insuranceNumber; // Optional
    
    @Pattern(
        regexp = "\\d{4}-\\d{2}-\\d{2}", 
        message = "Insurance expiry date must be in format yyyy-MM-dd"
    )
    private String insuranceExpiryDate; // Optional, yyyy-MM-dd

    // RC details
    private String rcNumber; // Optional
    
    @Pattern(
        regexp = "\\d{4}-\\d{2}-\\d{2}", 
        message = "RC expiry date must be in format yyyy-MM-dd"
    )
    private String rcExpiryDate; // Optional, yyyy-MM-dd

    // PUC details
    private String pucNumber; // Optional
    
    @Pattern(
        regexp = "\\d{4}-\\d{2}-\\d{2}", 
        message = "PUC expiry date must be in format yyyy-MM-dd"
    )
    private String pucExpiryDate; // Optional, yyyy-MM-dd
}