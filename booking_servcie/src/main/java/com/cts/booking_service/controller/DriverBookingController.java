package com.cts.booking_service.controller;

import com.cts.booking_service.dto.common.PageResponse;
import com.cts.booking_service.dto.driver.*;
import com.cts.booking_service.exception.MissingHeaderException;
import com.cts.booking_service.service.DriverBookingService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/driver/bookings")
@RequiredArgsConstructor
public class DriverBookingController {

    private final DriverBookingService driverBookingService;

    /**
     * Get available bookings for driver to accept
     * GET /api/v1/driver/bookings/available
     */
    @GetMapping("/available")
    public ResponseEntity<List<DriverBookingResponse>> getAvailableBookings(
            HttpServletRequest httpRequest,
            @RequestParam(required = false) String vehicleType) {

        String driverId = httpRequest.getHeader("X-User-Id");

        if (driverId == null || driverId.isBlank()) {
            log.error("Missing X-User-Id header");
            throw new MissingHeaderException("X-User-Id");
        }

        log.info("Driver {} fetching available bookings", driverId);
        List<DriverBookingResponse> bookings = driverBookingService.getAvailableBookings(driverId, vehicleType);
        return new ResponseEntity<>(bookings, HttpStatus.OK);
    }

    /**
     * Get driver's bookings with optional status filter
     * GET /api/v1/driver/bookings/me
     */
    @GetMapping("/me")
    public ResponseEntity<PageResponse<DriverBookingResponse>> getMyBookings(
            HttpServletRequest httpRequest,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        String driverId = httpRequest.getHeader("X-User-Id");

        if (driverId == null || driverId.isBlank()) {
            log.error("Missing X-User-Id header");
            throw new MissingHeaderException("X-User-Id");
        }

        log.info("Driver {} fetching bookings", driverId);
        PageResponse<DriverBookingResponse> bookings = driverBookingService.getMyBookings(driverId, status, page, size);
        return new ResponseEntity<>(bookings, HttpStatus.OK);
    }

    /**
     * Get driver's active booking
     * GET /api/v1/driver/bookings/active
     */
    @GetMapping("/active")
    public ResponseEntity<DriverBookingResponse> getActiveBooking(HttpServletRequest httpRequest) {
        String driverId = httpRequest.getHeader("X-User-Id");

        if (driverId == null || driverId.isBlank()) {
            log.error("Missing X-User-Id header");
            throw new MissingHeaderException("X-User-Id");
        }

        log.info("Driver {} fetching active booking", driverId);
        DriverBookingResponse booking = driverBookingService.getActiveBooking(driverId);

        if (booking == null) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

        return new ResponseEntity<>(booking, HttpStatus.OK);
    }

    /**
     * Accept a booking
     * PUT /api/v1/driver/bookings/{bookingId}/accept
     */
    @PutMapping("/{bookingId}/accept")
    public ResponseEntity<DriverBookingResponse> acceptBooking(
            HttpServletRequest httpRequest,
            @PathVariable String bookingId,
            @Valid @RequestBody AcceptBookingRequest request) {

        String driverId = httpRequest.getHeader("X-User-Id");

        if (driverId == null || driverId.isBlank()) {
            log.error("Missing X-User-Id header");
            throw new MissingHeaderException("X-User-Id");
        }

        log.info("Driver {} accepting booking {}", driverId, bookingId);
        DriverBookingResponse booking = driverBookingService.acceptBooking(bookingId, driverId, request);
        return new ResponseEntity<>(booking, HttpStatus.OK);
    }

    /**
     * Start a ride
     * PUT /api/v1/driver/bookings/{bookingId}/start
     */
    @PutMapping("/{bookingId}/start")
    public ResponseEntity<DriverBookingResponse> startRide(
            HttpServletRequest httpRequest,
            @PathVariable String bookingId) {

        String driverId = httpRequest.getHeader("X-User-Id");

        if (driverId == null || driverId.isBlank()) {
            log.error("Missing X-User-Id header");
            throw new MissingHeaderException("X-User-Id");
        }

        log.info("Driver {} starting ride {}", driverId, bookingId);
        DriverBookingResponse booking = driverBookingService.startRide(bookingId, driverId);
        return new ResponseEntity<>(booking, HttpStatus.OK);
    }

    /**
     * Complete a ride
     * PUT /api/v1/driver/bookings/{bookingId}/complete
     */
    @PutMapping("/{bookingId}/complete")
    public ResponseEntity<DriverBookingResponse> completeRide(
            HttpServletRequest httpRequest,
            @PathVariable String bookingId,
            @Valid @RequestBody CompleteBookingRequest request) {

        String driverId = httpRequest.getHeader("X-User-Id");

        if (driverId == null || driverId.isBlank()) {
            log.error("Missing X-User-Id header");
            throw new MissingHeaderException("X-User-Id");
        }

        log.info("Driver {} completing ride {}", driverId, bookingId);
        DriverBookingResponse booking = driverBookingService.completeRide(bookingId, driverId, request);
        return new ResponseEntity<>(booking, HttpStatus.OK);
    }

    /**
     * Get booking details by ID
     * GET /api/v1/driver/bookings/{bookingId}
     */
    @GetMapping("/{bookingId}")
    public ResponseEntity<DriverBookingResponse> getBookingDetails(
            HttpServletRequest httpRequest,
            @PathVariable String bookingId) {

        String driverId = httpRequest.getHeader("X-User-Id");

        if (driverId == null || driverId.isBlank()) {
            log.error("Missing X-User-Id header");
            throw new MissingHeaderException("X-User-Id");
        }

        log.info("Driver {} fetching booking {}", driverId, bookingId);
        DriverBookingResponse booking = driverBookingService.getBookingDetails(bookingId, driverId);
        return new ResponseEntity<>(booking, HttpStatus.OK);
    }
}