package com.cts.driver_service.exception;

public class DriverNotApprovedException extends RuntimeException {
    public DriverNotApprovedException(String message) {
        super(message);
    }
}

