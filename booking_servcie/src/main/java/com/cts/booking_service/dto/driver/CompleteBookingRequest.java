package com.cts.booking_service.dto.driver;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompleteBookingRequest {
    private BigDecimal finalDistanceKm;
    private Integer finalDurationMinutes;
    private BigDecimal finalFare;
}