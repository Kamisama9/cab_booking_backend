package com.cts.booking_service.exception;

public class BookingAlreadyAssignedException extends RuntimeException {
    public BookingAlreadyAssignedException(String message) {
        super(message);
    }

    public BookingAlreadyAssignedException(String bookingId, String driverId) {
        super(String.format("Booking '%s' has already been assigned to driver '%s'", bookingId, driverId));
    }
}

