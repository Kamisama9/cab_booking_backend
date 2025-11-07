package com.cts.booking_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "bookings")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String riderId;

    private String driverId;
    private String vehicleId;

    @Column(nullable = false)
    private Double pickupLatitude;

    @Column(nullable = false)
    private Double pickupLongitude;

    @Column(nullable = false, length = 500)
    private String pickupAddress;

    @Column(nullable = false)
    private Double dropoffLatitude;

    @Column(nullable = false)
    private Double dropoffLongitude;

    @Column(nullable = false, length = 500)
    private String dropoffAddress;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private VehicleType vehicleType;

    public enum VehicleType {
        AUTO, BIKE, SEDAN, SUV
    }

    @Column(precision = 10, scale = 2)
    private BigDecimal fareAmount;

    @Column(precision = 10, scale = 2)
    private BigDecimal tripDistanceKm;

    private Integer tripDurationMinutes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BookingStatus bookingStatus;

    public enum BookingStatus {
        PENDING,      // Ride requested, waiting for driver
        ACCEPTED,     // Driver accepted
        STARTED,      // Driver started ride (picked up rider)
        COMPLETED,    // Ride completed
        CANCELLED     // Cancelled by rider or driver
    }

    // Timestamps
    private OffsetDateTime requestTime;     // When rider requested
    private OffsetDateTime pickupTime;      // When driver picked up rider
    private OffsetDateTime dropoffTime;     // When ride completed

    @CreationTimestamp
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    private OffsetDateTime updatedAt;

    @Column(length = 500)
    private String cancellationReason;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private CancelledBy cancelledBy;

    public enum CancelledBy {
        RIDER,
        DRIVER,
        ADMIN
    }

    private Integer riderRating;      // Driver rates rider (1-5)
    private Integer driverRating;     // Rider rates driver (1-5)

    @Column(length = 1000)
    private String riderFeedback;

    @Column(length = 1000)
    private String driverFeedback;


    private String paymentId;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private PaymentStatus paymentStatus;

    public enum PaymentStatus {
        PENDING,
        COMPLETED,
        FAILED,
        REFUNDED
    }
}