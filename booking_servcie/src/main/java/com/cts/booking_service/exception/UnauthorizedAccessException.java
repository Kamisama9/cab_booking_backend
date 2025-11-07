package com.cts.booking_service.exception;

public class UnauthorizedAccessException extends RuntimeException {
    public UnauthorizedAccessException(String message) {
        super(message);
    }

    public UnauthorizedAccessException(String userId, String bookingId) {
        super(String.format("User '%s' is not authorized to access booking '%s'", userId, bookingId));
    }
}

