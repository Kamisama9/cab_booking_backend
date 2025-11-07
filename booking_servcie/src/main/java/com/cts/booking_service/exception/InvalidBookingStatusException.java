package com.cts.booking_service.exception;

public class InvalidBookingStatusException extends RuntimeException {
    public InvalidBookingStatusException(String message) {
        super(message);
    }

    public InvalidBookingStatusException(String bookingId, String currentStatus, String expectedStatus) {
        super(String.format("Booking '%s' is in '%s' status, expected '%s'", bookingId, currentStatus, expectedStatus));
    }
}

