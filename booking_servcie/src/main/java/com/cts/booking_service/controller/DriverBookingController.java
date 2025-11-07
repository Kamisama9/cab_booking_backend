package com.cts.booking_service.controller;

import com.cts.booking_service.client.UserServiceClient;
import com.cts.booking_service.dto.common.PageResponse;
import com.cts.booking_service.dto.driver.*;
import com.cts.booking_service.exception.MissingHeaderException;
import com.cts.booking_service.service.DriverBookingService;
import jakarta.servlet.http.HttpServletRequest;
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


    @GetMapping("/available")
    public ResponseEntity<List<DriverBookingResponse>> getAvailableBookings(
            HttpServletRequest httpRequest,
            @RequestParam(required = false) String vehicleType) {

        String driverId = httpRequest.getHeader("X-User-Id");

        if (driverId == null || driverId.isBlank()) {
            log.error("Missing X-User-Id header");
            throw new MissingHeaderException("X-User-Id");
        }

        List<DriverBookingResponse> bookings = driverBookingService.getAvailableBookings(driverId, vehicleType);
        return new ResponseEntity<>(bookings, HttpStatus.OK);
    }


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

        PageResponse<DriverBookingResponse> bookings = driverBookingService.getMyBookings(driverId, status, page, size);
        return new ResponseEntity<>(bookings, HttpStatus.OK);
    }


    @GetMapping("/active")
    public ResponseEntity<DriverBookingResponse> getActiveBooking(HttpServletRequest httpRequest) {
        String driverId = httpRequest.getHeader("X-User-Id");

        if (driverId == null || driverId.isBlank()) {
            log.error("Missing X-User-Id header");
            throw new MissingHeaderException("X-User-Id");
        }

        DriverBookingResponse booking = driverBookingService.getActiveBooking(driverId);

        if (booking == null) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

        return new ResponseEntity<>(booking, HttpStatus.OK);
    }


    @PutMapping("/{bookingId}/accept")
    public ResponseEntity<DriverBookingResponse> acceptBooking(
            HttpServletRequest httpRequest,
            @PathVariable String bookingId,
            @RequestBody AcceptBookingRequest request) {

        String driverId = httpRequest.getHeader("X-User-Id");

        if (driverId == null || driverId.isBlank()) {
            log.error("Missing X-User-Id header");
            throw new MissingHeaderException("X-User-Id");
        }

        DriverBookingResponse booking = driverBookingService.acceptBooking(bookingId, driverId, request);
        return new ResponseEntity<>(booking, HttpStatus.OK);
    }


    //TODO: REJECT BOOKING



    @PutMapping("/{bookingId}/start")
    public ResponseEntity<DriverBookingResponse> startRide(
            HttpServletRequest httpRequest,
            @PathVariable String bookingId) {

        String driverId = httpRequest.getHeader("X-User-Id");

        if (driverId == null || driverId.isBlank()) {
            log.error("Missing X-User-Id header");
            throw new MissingHeaderException("X-User-Id");
        }

        DriverBookingResponse booking = driverBookingService.startRide(bookingId, driverId);
        return new ResponseEntity<>(booking, HttpStatus.OK);
    }


    @PutMapping("/{bookingId}/complete")
    public ResponseEntity<DriverBookingResponse> completeRide(
            HttpServletRequest httpRequest,
            @PathVariable String bookingId,
            @RequestBody CompleteBookingRequest request) {

        String driverId = httpRequest.getHeader("X-User-Id");

        if (driverId == null || driverId.isBlank()) {
            log.error("Missing X-User-Id header");
            throw new MissingHeaderException("X-User-Id");
        }

        DriverBookingResponse booking = driverBookingService.completeRide(bookingId, driverId, request);
        return new ResponseEntity<>(booking, HttpStatus.OK);
    }


     // TODO: CANCEL BOOKING


     // TODO: RATE RIDER



    @GetMapping("/{bookingId}")
    public ResponseEntity<DriverBookingResponse> getBookingDetails(
            HttpServletRequest httpRequest,
            @PathVariable String bookingId) {

        String driverId = httpRequest.getHeader("X-User-Id");

        if (driverId == null || driverId.isBlank()) {
            log.error("Missing X-User-Id header");
            throw new MissingHeaderException("X-User-Id");
        }

        DriverBookingResponse booking = driverBookingService.getBookingDetails(bookingId, driverId);
        return new ResponseEntity<>(booking, HttpStatus.OK);
    }
}