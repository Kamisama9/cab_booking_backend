package com.cts.booking_service.dto.driver;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AcceptBookingRequest {
    
    @NotBlank(message = "Vehicle ID is required")
    private String vehicleId;
}