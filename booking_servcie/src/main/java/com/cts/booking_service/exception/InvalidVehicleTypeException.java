package com.cts.booking_service.exception;

public class InvalidVehicleTypeException extends RuntimeException {
    public InvalidVehicleTypeException(String message) {
        super(message);
    }

    public InvalidVehicleTypeException(String vehicleType, String validTypes) {
        super(String.format("Invalid vehicle type '%s'. Valid types are: %s", vehicleType, validTypes));
    }
}

