package com.cts.booking_service.dto.driver;

import com.cts.booking_service.entity.Booking;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DriverBookingResponse {
    private String id;
    private String riderId;
    private String riderName;
    private String riderPhone;

    private String pickupAddress;
    private Double pickupLatitude;
    private Double pickupLongitude;

    private String dropoffAddress;
    private Double dropoffLatitude;
    private Double dropoffLongitude;

    private String vehicleType;
    private BigDecimal fareAmount;
    private BigDecimal tripDistanceKm;
    private Integer tripDurationMinutes;

    private String bookingStatus;
    private OffsetDateTime requestTime;
    private OffsetDateTime pickupTime;
    private OffsetDateTime dropoffTime;
    private OffsetDateTime createdAt;

    private Double distanceFromDriver;

    public static DriverBookingResponse fromEntity(Booking booking) {
        DriverBookingResponse response = new DriverBookingResponse();
        response.setId(booking.getId());
        response.setRiderId(booking.getRiderId());
        response.setPickupAddress(booking.getPickupAddress());
        response.setPickupLatitude(booking.getPickupLatitude());
        response.setPickupLongitude(booking.getPickupLongitude());
        response.setDropoffAddress(booking.getDropoffAddress());
        response.setDropoffLatitude(booking.getDropoffLatitude());
        response.setDropoffLongitude(booking.getDropoffLongitude());
        response.setVehicleType(booking.getVehicleType() != null ? booking.getVehicleType().name().toLowerCase() : null);
        response.setFareAmount(booking.getFareAmount());
        response.setTripDistanceKm(booking.getTripDistanceKm());
        response.setTripDurationMinutes(booking.getTripDurationMinutes());
        response.setBookingStatus(booking.getBookingStatus() != null ? booking.getBookingStatus().name().toLowerCase() : null);
        response.setRequestTime(booking.getRequestTime());
        response.setPickupTime(booking.getPickupTime());
        response.setDropoffTime(booking.getDropoffTime());
        response.setCreatedAt(booking.getCreatedAt());
        return response;
    }
}