package com.cts.booking_service.dto.rider;

import com.cts.booking_service.entity.Booking;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RiderBookingResponse {
    private String id;
    private String riderId;
    private String driverId;
    private String driverName;  // Populated from User Service
    private String driverPhone;  // Populated from User Service
    private String vehicleId;

    private String pickupAddress;
    private String dropoffAddress;

    private String vehicleType;
    private BigDecimal fareAmount;
    private BigDecimal tripDistanceKm;
    private Integer tripDurationMinutes;

    private String bookingStatus;

    // Payment Info
    private String paymentId;  // ADDED: From entity
    private String paymentStatus;  // ADDED: From entity

    // Timestamps
    private OffsetDateTime requestTime;
    private OffsetDateTime pickupTime;
    private OffsetDateTime dropoffTime;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;  // ADDED: From entity

    public static RiderBookingResponse fromEntity(Booking booking) {
        RiderBookingResponse response = new RiderBookingResponse();
        response.setId(booking.getId());
        response.setRiderId(booking.getRiderId());
        response.setDriverId(booking.getDriverId());
        response.setVehicleId(booking.getVehicleId());
        response.setPickupAddress(booking.getPickupAddress());
        response.setDropoffAddress(booking.getDropoffAddress());
        response.setVehicleType(booking.getVehicleType() != null ? booking.getVehicleType().name().toLowerCase() : null);
        response.setFareAmount(booking.getFareAmount());
        response.setTripDistanceKm(booking.getTripDistanceKm());
        response.setTripDurationMinutes(booking.getTripDurationMinutes());
        response.setBookingStatus(booking.getBookingStatus() != null ? booking.getBookingStatus().name().toLowerCase() : null);
        

        
        // Payment Info
        response.setPaymentId(booking.getPaymentId());  // ADDED
        response.setPaymentStatus(booking.getPaymentStatus() != null ? booking.getPaymentStatus().name().toLowerCase() : null);  // ADDED
        
        // Timestamps
        response.setRequestTime(booking.getRequestTime());
        response.setPickupTime(booking.getPickupTime());
        response.setDropoffTime(booking.getDropoffTime());
        response.setCreatedAt(booking.getCreatedAt());
        response.setUpdatedAt(booking.getUpdatedAt());  // ADDED
        
        return response;
    }
}