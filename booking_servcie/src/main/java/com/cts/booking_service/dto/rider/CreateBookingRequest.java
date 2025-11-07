package com.cts.booking_service.dto.rider;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateBookingRequest {
    private Double pickupLatitude;
    private Double pickupLongitude;
    private String pickupAddress;

    private Double dropoffLatitude;
    private Double dropoffLongitude;
    private String dropoffAddress;

    private String vehicleType; // AUTO, BIKE, SEDAN, SUV
}