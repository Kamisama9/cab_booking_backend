package com.cts.booking_service.dao;


import com.cts.booking_service.entity.Booking;
import com.cts.booking_service.repository.RiderBookingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class RiderBookingDaoImpl implements RiderBookingDao {

    private final RiderBookingRepository repository;

    @Override
    public Booking save(Booking booking) {
        log.debug("Saving booking for rider: {}", booking.getRiderId());
        return repository.save(booking);
    }

    @Override
    public Optional<Booking> findById(String bookingId) {
        log.debug("Finding booking by ID: {}", bookingId);
        return repository.findById(bookingId);
    }

    @Override
    public Page<Booking> findAllByRiderId(String riderId, int page, int size) {
        log.debug("Finding all bookings for rider: {} (page: {}, size: {})", riderId, page, size);
        Pageable pageable = PageRequest.of(page, size);
        return repository.findByRiderIdOrderByCreatedAtDesc(riderId, pageable);
    }

    @Override
    public Page<Booking> findByRiderIdAndStatus(String riderId, Booking.BookingStatus status, int page, int size) {
        log.debug("Finding bookings for rider: {} with status: {} (page: {}, size: {})",
                riderId, status, page, size);
        Pageable pageable = PageRequest.of(page, size);
        return repository.findByRiderIdAndBookingStatusOrderByCreatedAtDesc(riderId, status, pageable);
    }

    @Override
    public Page<Booking> searchByPickupAddress(String riderId, String searchTerm, int page, int size) {
        log.debug("Searching bookings by pickup address for rider: {} (search: '{}')", riderId, searchTerm);
        Pageable pageable = PageRequest.of(page, size);
        return repository.searchByPickupAddress(riderId, searchTerm, pageable);
    }

    @Override
    public Page<Booking> searchByDropoffAddress(String riderId, String searchTerm, int page, int size) {
        log.debug("Searching bookings by dropoff address for rider: {} (search: '{}')", riderId, searchTerm);
        Pageable pageable = PageRequest.of(page, size);
        return repository.searchByDropoffAddress(riderId, searchTerm, pageable);
    }

    @Override
    public Page<Booking> searchByDate(String riderId, OffsetDateTime date, int page, int size) {
        log.debug("Searching bookings by date for rider: {} (date: {})", riderId, date);
        Pageable pageable = PageRequest.of(page, size);
        return repository.searchByDate(riderId, date, pageable);
    }

    @Override
    public Booking update(Booking booking) {
        log.debug("Updating booking: {}", booking.getId());
        return repository.save(booking);
    }

    @Override
    public boolean existsByIdAndRiderId(String bookingId, String riderId) {
        log.debug("Checking if booking {} belongs to rider {}", bookingId, riderId);
        return repository.findById(bookingId)
                .map(booking -> booking.getRiderId().equals(riderId))
                .orElse(false);
    }

    @Override
    public Page<Booking> findByStatus(Booking.BookingStatus status, Pageable pageable) {
        log.debug("Finding all bookings with status: {}", status);
        return repository.findByBookingStatusOrderByCreatedAtDesc(status, pageable);
    }

    @Override
    public Page<Booking> findAll(Pageable pageable) {
        log.debug("Finding all bookings");
        return repository.findAll(pageable);
    }
}