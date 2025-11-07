package com.cts.booking_service.client.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private String id;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String city;
    private String state;
    private String role;
    private String status;
    private Boolean isEmailVerified;
    private LocalDate createdAt;
    private LocalDate updatedAt;
}

