package com.cts.booking_service.exception;

public class ActiveBookingExistsException extends RuntimeException {
    public ActiveBookingExistsException(String message) {
        super(message);
    }

    public ActiveBookingExistsException(String driverId, String activeBookingId) {
        super(String.format("Driver '%s' already has an active booking '%s'. Complete it before accepting new bookings.", driverId, activeBookingId));
    }
}

