package com.cts.driver_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private String id;
    private String first_name;
    private String last_name;
    private String email;
    private String phoneNumber;
    private String city;
    private String state;
    private String role;
    private String status;
}