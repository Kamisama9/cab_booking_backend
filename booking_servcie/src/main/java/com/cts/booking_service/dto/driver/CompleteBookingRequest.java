package com.cts.booking_service.dto.driver;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompleteBookingRequest {
    
    @DecimalMin(value = "0.0", inclusive = false, message = "Final distance must be greater than 0")
    private BigDecimal finalDistanceKm;
    
    @Positive(message = "Final duration must be greater than 0")
    private Integer finalDurationMinutes;
    
    @DecimalMin(value = "0.0", inclusive = false, message = "Final fare must be greater than 0")
    private BigDecimal finalFare;
}