package com.cts.booking_service.exception;

public class MissingHeaderException extends RuntimeException {
//    public MissingHeaderException(String message) {
//        super(message);
//    }

    public MissingHeaderException(String headerName) {
        super(String.format("Missing required header: %s", headerName));
    }
}

