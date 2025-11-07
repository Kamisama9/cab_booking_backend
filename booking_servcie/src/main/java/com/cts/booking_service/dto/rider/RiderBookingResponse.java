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
    private String driverName;
    private String driverPhone;
    private String vehicleId;

    private String pickupAddress;
    private String dropoffAddress;

    private String vehicleType;
    private BigDecimal fareAmount;
    private BigDecimal tripDistanceKm;
    private Integer tripDurationMinutes;

    private String bookingStatus;
    private String cancelledBy;
    private String cancellationReason;

    private Integer driverRating;
    private String driverFeedback;

    private OffsetDateTime requestTime;
    private OffsetDateTime pickupTime;
    private OffsetDateTime dropoffTime;
    private OffsetDateTime createdAt;

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
        response.setCancelledBy(booking.getCancelledBy() != null ? booking.getCancelledBy().name() : null);
        response.setCancellationReason(booking.getCancellationReason());
        response.setDriverRating(booking.getDriverRating());
        response.setDriverFeedback(booking.getDriverFeedback());
        response.setRequestTime(booking.getRequestTime());
        response.setPickupTime(booking.getPickupTime());
        response.setDropoffTime(booking.getDropoffTime());
        response.setCreatedAt(booking.getCreatedAt());
        return response;
    }
}