package com.cts.booking_service.controller;

import com.cts.booking_service.dto.common.PageResponse;
import com.cts.booking_service.dto.rider.*;
import com.cts.booking_service.exception.MissingHeaderException;
import com.cts.booking_service.service.RiderBookingService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
public class RiderBookingController {

    private final RiderBookingService riderBookingService;

    @PostMapping
    public ResponseEntity<RiderBookingResponse> createBooking(
            HttpServletRequest httpRequest,
            @RequestBody CreateBookingRequest request) {

        String riderId = httpRequest.getHeader("X-User-Id");

        if (riderId == null || riderId.isBlank()) {
            log.error("Missing X-User-Id header");
            throw new MissingHeaderException("X-User-Id");
        }

        RiderBookingResponse booking = riderBookingService.createBooking(riderId, request);
        return new ResponseEntity<>(booking, HttpStatus.CREATED);
    }

    @GetMapping("/me")
    public ResponseEntity<PageResponse<RiderBookingResponse>> getMyBookings(
            HttpServletRequest httpRequest,
            @RequestParam(required = false) String filterType,
            @RequestParam(required = false) String searchTerm,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        String riderId = httpRequest.getHeader("X-User-Id");

        if (riderId == null || riderId.isBlank()) {
            log.error("Missing X-User-Id header");
            throw new MissingHeaderException("X-User-Id");
        }

        PageResponse<RiderBookingResponse> bookings = riderBookingService.getMyBookings(
                riderId, filterType, searchTerm, status, page, size
        );
        return new ResponseEntity<>(bookings, HttpStatus.OK);
    }


    @GetMapping("/{bookingId}")
    public ResponseEntity<RiderBookingResponse> getBookingDetails(
            HttpServletRequest httpRequest,
            @PathVariable String bookingId) {

        String riderId = httpRequest.getHeader("X-User-Id");

        if (riderId == null || riderId.isBlank()) {
            log.error("Missing X-User-Id header");
            throw new MissingHeaderException("X-User-Id");
        }

        RiderBookingResponse booking = riderBookingService.getBookingDetails(bookingId, riderId);
        return new ResponseEntity<>(booking, HttpStatus.OK);
    }

   //TODO: Cancel Booking
   @PutMapping("/{bookingId}/cancel")
   public ResponseEntity<RiderBookingResponse> cancelBooking(
           HttpServletRequest httpRequest,
           @PathVariable String bookingId) {

       String riderId = httpRequest.getHeader("X-User-Id");
       if (riderId == null || riderId.isBlank()) {
           throw new MissingHeaderException("X-User-Id");
       }

       RiderBookingResponse booking = riderBookingService.cancelBooking(bookingId, riderId);
       return new ResponseEntity<>(booking, HttpStatus.OK);
   }
   //TODO: Rate Driver
}