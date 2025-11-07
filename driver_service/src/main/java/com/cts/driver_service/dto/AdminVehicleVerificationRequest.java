package com.cts.driver_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminVehicleVerificationRequest {
    private String action; // APPROVE | REJECT | UNDER_REVIEW
    private String rejectionReason;
}