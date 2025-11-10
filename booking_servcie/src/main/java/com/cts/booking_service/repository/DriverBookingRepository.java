package com.cts.booking_service.repository;

import com.cts.booking_service.entity.Booking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DriverBookingRepository extends JpaRepository<Booking, String> {
    Page<Booking> findByDriverIdOrderByCreatedAtDesc(
            @Param("driverId") String driverId,
            Pageable pageable
    );
    Page<Booking> findByDriverIdAndBookingStatusOrderByCreatedAtDesc(
            @Param("driverId") String driverId,
            @Param("status") Booking.BookingStatus status,
            Pageable pageable
    );
    @Query("SELECT b FROM Booking b WHERE b.driverId = :driverId " +
            "AND b.bookingStatus IN ('ACCEPTED', 'STARTED') " +
            "ORDER BY b.createdAt DESC")
    Optional<Booking> findActiveBookingByDriverId(@Param("driverId") String driverId);

    @Query("SELECT b FROM Booking b WHERE b.bookingStatus = 'PENDING' " +
            "AND b.driverId IS NULL " +
            "ORDER BY b.createdAt ASC")
    List<Booking> findPendingBookings();

    @Query("SELECT b FROM Booking b WHERE b.bookingStatus = 'PENDING' " +
            "AND b.driverId IS NULL " +
            "AND b.vehicleType = :vehicleType " +
            "ORDER BY b.createdAt ASC")
    List<Booking> findPendingBookingsByVehicleType(
            @Param("vehicleType") Booking.VehicleType vehicleType
    );


    @Query("SELECT COUNT(b) FROM Booking b WHERE b.driverId = :driverId " +
            "AND b.bookingStatus = 'COMPLETED'")
    long countCompletedBookingsByDriverId(@Param("driverId") String driverId);


    @Query("SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END " +
            "FROM Booking b WHERE b.driverId = :driverId " +
            "AND b.bookingStatus IN ('ACCEPTED', 'STARTED')")
    boolean hasActiveBooking(@Param("driverId") String driverId);

}
