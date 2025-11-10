package com.cts.booking_service.controller;

import com.cts.booking_service.dto.common.PageResponse;
import com.cts.booking_service.dto.rider.RiderBookingResponse;
import com.cts.booking_service.service.AdminBookingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin/bookings")
@RequiredArgsConstructor
public class AdminBookingController {

    private final AdminBookingService adminBookingService;

    /**
     * Get all bookings (Admin only)
     * GET /api/v1/admin/bookings
     */
    @GetMapping
    public ResponseEntity<PageResponse<RiderBookingResponse>> getAllBookings(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size) {

        log.info("Admin fetching all bookings (status: {}, page: {}, size: {})", status, page, size);
        PageResponse<RiderBookingResponse> bookings = adminBookingService.getAllBookings(status, page, size);
        return new ResponseEntity<>(bookings, HttpStatus.OK);
    }

    /**
     * Get booking by ID (Admin only)
     * GET /api/v1/admin/bookings/{bookingId}
     */
    @GetMapping("/{bookingId}")
    public ResponseEntity<RiderBookingResponse> getBookingById(@PathVariable String bookingId) {
        log.info("Admin fetching booking: {}", bookingId);
        RiderBookingResponse booking = adminBookingService.getBookingById(bookingId);
        return new ResponseEntity<>(booking, HttpStatus.OK);
    }
}