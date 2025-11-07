package com.cts.driver_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DriverProfileRequest {
    
    @NotBlank(message = "Aadhaar number is required")
    @Pattern(
        regexp = "\\d{12}", 
        message = "Aadhaar number must be exactly 12 digits"
    )
    private String aadhaarNumber;
    
    @NotBlank(message = "Driving license number is required")
    private String dlNumber;
    
    @NotBlank(message = "Driving license expiry date is required")
    @Pattern(
        regexp = "\\d{4}-\\d{2}-\\d{2}", 
        message = "Date must be in format yyyy-MM-dd"
    )
    private String dlExpiryDate; // yyyy-MM-dd format
}