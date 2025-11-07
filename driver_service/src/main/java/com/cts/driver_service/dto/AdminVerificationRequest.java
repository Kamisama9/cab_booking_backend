package com.cts.driver_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminVerificationRequest {
    
    @NotBlank(message = "Action is required")
    @Pattern(
        regexp = "APPROVE|REJECT|UNDER_REVIEW", 
        message = "Invalid action. Valid values: APPROVE, REJECT, UNDER_REVIEW"
    )
    private String action;
    
    private String rejectionReason; // Optional, but required if action is REJECT
}