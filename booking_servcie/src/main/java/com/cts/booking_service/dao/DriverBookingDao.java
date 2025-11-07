package com.cts.booking_service.dao;

import com.cts.booking_service.entity.Booking;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface DriverBookingDao {

    Optional<Booking> findById(String bookingId);
    Booking save(Booking booking);
    Page<Booking> findAllByDriverId(String driverId, int page, int size);
    Page<Booking> findByDriverIdAndStatus(String driverId, Booking.BookingStatus status, int page, int size);
    Optional<Booking> findActiveBooking(String driverId);
    boolean hasActiveBooking(String driverId);
    List<Booking> findAvailableBookings();
    List<Booking> findAvailableBookingsByVehicleType(Booking.VehicleType vehicleType);
    long getCompletedBookingCount(String driverId);
    BigDecimal getTotalEarnings(String driverId);
    List<Booking> findBookingsBetweenDates(String driverId, OffsetDateTime startDate, OffsetDateTime endDate);
}