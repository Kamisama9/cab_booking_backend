package com.cts.booking_service.exception;

public class BookingNotFoundException extends RuntimeException {
    public BookingNotFoundException(String message) {
        super(message);
    }

    public BookingNotFoundException(String bookingId, boolean byId) {
        super(String.format("Booking with ID '%s' not found", bookingId));
    }
}

