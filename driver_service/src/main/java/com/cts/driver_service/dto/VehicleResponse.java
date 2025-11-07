package com.cts.driver_service.dto;

import com.cts.driver_service.entity.Vehicle;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VehicleResponse {
    private String id;
    private String driverId;
    private String registrationNumber;
    private String vehicleType;
    private String manufacturer;
    private String model;
    private Integer year;
    private String color;
    private String insuranceNumber;
    private LocalDate insuranceExpiryDate;
    private String rcNumber;
    private LocalDate rcExpiryDate;
    private String pucNumber;
    private LocalDate pucExpiryDate;
    private String verificationStatus;
    private String rejectionReason;
    private Boolean isActive;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public static VehicleResponse fromEntity(Vehicle vehicle) {
        VehicleResponse response = new VehicleResponse();
        response.setId(vehicle.getId());
        response.setDriverId(vehicle.getDriverId());
        response.setRegistrationNumber(vehicle.getRegistrationNumber());
        response.setVehicleType(vehicle.getVehicleType() != null ? vehicle.getVehicleType().name() : null);
        response.setManufacturer(vehicle.getManufacturer());
        response.setModel(vehicle.getModel());
        response.setYear(vehicle.getYear());
        response.setColor(vehicle.getColor());
        response.setInsuranceNumber(vehicle.getInsuranceNumber());
        response.setInsuranceExpiryDate(vehicle.getInsuranceExpiryDate());
        response.setRcNumber(vehicle.getRcNumber());
        response.setRcExpiryDate(vehicle.getRcExpiryDate());
        response.setPucNumber(vehicle.getPucNumber());
        response.setPucExpiryDate(vehicle.getPucExpiryDate());
        response.setVerificationStatus(vehicle.getVerificationStatus() != null ? vehicle.getVerificationStatus().name() : null);
        response.setRejectionReason(vehicle.getRejectionReason());
        response.setIsActive(vehicle.getIsActive());
        response.setCreatedAt(vehicle.getCreatedAt());
        response.setUpdatedAt(vehicle.getUpdatedAt());
        return response;
    }
}