package com.cts.booking_service.dao;

import com.cts.booking_service.entity.Booking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.OffsetDateTime;
import java.util.Optional;


public interface RiderBookingDao {


    Booking save(Booking booking);

    Optional<Booking> findById(String bookingId);

    Page<Booking> findAllByRiderId(String riderId, int page, int size);


    Page<Booking> findByRiderIdAndStatus(String riderId, Booking.BookingStatus status, int page, int size);


    Page<Booking> searchByPickupAddress(String riderId, String searchTerm, int page, int size);

    Page<Booking> searchByDropoffAddress(String riderId, String searchTerm, int page, int size);


    Page<Booking> searchByDate(String riderId, OffsetDateTime date, int page, int size);


    Booking update(Booking booking);


    boolean existsByIdAndRiderId(String bookingId, String riderId);

    // Add these methods to RiderBookingDao interface

    Page<Booking> findByStatus(Booking.BookingStatus status, Pageable pageable);
    Page<Booking> findAll(Pageable pageable);
}