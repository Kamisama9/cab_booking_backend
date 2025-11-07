package com.cts.booking_service.dao;

import com.cts.booking_service.entity.Booking;
import com.cts.booking_service.repository.DriverBookingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class DriverBookingDaoImpl implements DriverBookingDao {

    private final DriverBookingRepository repository;

    @Override
    public Optional<Booking> findById(String bookingId) {
        log.debug("Finding booking by ID: {}", bookingId);
        return repository.findById(bookingId);
    }

    @Override
    public Booking save(Booking booking) {
        log.debug("Saving booking: {}", booking.getId());
        return repository.save(booking);
    }

    @Override
    public Page<Booking> findAllByDriverId(String driverId, int page, int size) {
        log.debug("Finding all bookings for driver: {} (page: {}, size: {})", driverId, page, size);
        Pageable pageable = PageRequest.of(page, size);
        return repository.findByDriverIdOrderByCreatedAtDesc(driverId, pageable);
    }

    @Override
    public Page<Booking> findByDriverIdAndStatus(String driverId, Booking.BookingStatus status, int page, int size) {
        log.debug("Finding bookings for driver: {} with status: {} (page: {}, size: {})",
                driverId, status, page, size);
        Pageable pageable = PageRequest.of(page, size);
        return repository.findByDriverIdAndBookingStatusOrderByCreatedAtDesc(driverId, status, pageable);
    }

    @Override
    public Optional<Booking> findActiveBooking(String driverId) {
        log.debug("Finding active booking for driver: {}", driverId);
        return repository.findActiveBookingByDriverId(driverId);
    }

    @Override
    public boolean hasActiveBooking(String driverId) {
        log.debug("Checking if driver {} has active booking", driverId);
        return repository.hasActiveBooking(driverId);
    }

    @Override
    public List<Booking> findAvailableBookings() {
        log.debug("Finding all available bookings");
        return repository.findPendingBookings();
    }

    @Override
    public List<Booking> findAvailableBookingsByVehicleType(Booking.VehicleType vehicleType) {
        log.debug("Finding available bookings for vehicle type: {}", vehicleType);
        return repository.findPendingBookingsByVehicleType(vehicleType);
    }

    @Override
    public long getCompletedBookingCount(String driverId) {
        log.debug("Getting completed booking count for driver: {}", driverId);
        return repository.countCompletedBookingsByDriverId(driverId);
    }

    @Override
    public BigDecimal getTotalEarnings(String driverId) {
        log.debug("Getting total earnings for driver: {}", driverId);
        BigDecimal earnings = repository.getTotalEarningsByDriverId(driverId);
        return earnings != null ? earnings : BigDecimal.ZERO;
    }

    @Override
    public List<Booking> findBookingsBetweenDates(String driverId, OffsetDateTime startDate, OffsetDateTime endDate) {
        log.debug("Finding bookings for driver: {} between {} and {}", driverId, startDate, endDate);
        return repository.findDriverBookingsBetweenDates(driverId, startDate, endDate);
    }
}