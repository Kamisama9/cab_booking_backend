package com.cts.booking_service.repository;

import com.cts.booking_service.entity.Booking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;

@Repository
public interface RiderBookingRepository extends JpaRepository<Booking, String> {

    Page<Booking> findByRiderIdOrderByCreatedAtDesc(
            @Param("riderId") String riderId,
            Pageable pageable
    );
    Page<Booking> findByRiderIdAndBookingStatusOrderByCreatedAtDesc(
            @Param("riderId") String riderId,
            @Param("status") Booking.BookingStatus status,
            Pageable pageable
    );

    @Query("SELECT b FROM Booking b WHERE b.riderId = :riderId " +
            "AND LOWER(b.pickupAddress) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "ORDER BY b.createdAt DESC")
    Page<Booking> searchByPickupAddress(
            @Param("riderId") String riderId,
            @Param("search") String search,
            Pageable pageable
    );

    @Query("SELECT b FROM Booking b WHERE b.riderId = :riderId " +
            "AND LOWER(b.dropoffAddress) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "ORDER BY b.createdAt DESC")
    Page<Booking> searchByDropoffAddress(
            @Param("riderId") String riderId,
            @Param("search") String search,
            Pageable pageable
    );

    @Query("SELECT b FROM Booking b WHERE b.riderId = :riderId " +
            "AND DATE(b.createdAt) = DATE(:date) " +
            "ORDER BY b.createdAt DESC")
    Page<Booking> searchByDate(
            @Param("riderId") String riderId,
            @Param("date") OffsetDateTime date,
            Pageable pageable
    );

    Page<Booking> findByBookingStatusOrderByCreatedAtDesc(
            Booking.BookingStatus status,
            Pageable pageable
    );
}