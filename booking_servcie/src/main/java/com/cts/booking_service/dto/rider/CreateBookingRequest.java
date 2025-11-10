package com.cts.booking_service.dto.rider;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateBookingRequest {
    
    @NotNull(message = "Pickup latitude is required")
    private Double pickupLatitude;
    
    @NotNull(message = "Pickup longitude is required")
    private Double pickupLongitude;
    
    @NotBlank(message = "Pickup address is required")
    private String pickupAddress;

    @NotNull(message = "Dropoff latitude is required")
    private Double dropoffLatitude;
    
    @NotNull(message = "Dropoff longitude is required")
    private Double dropoffLongitude;
    
    @NotBlank(message = "Dropoff address is required")
    private String dropoffAddress;

    @NotBlank(message = "Vehicle type is required")
    private String vehicleType;
}