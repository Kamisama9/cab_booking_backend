package com.cts.user_service.dto;

import com.cts.user_service.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserValidationResponse {
    private String userId;
    private String role;
}
