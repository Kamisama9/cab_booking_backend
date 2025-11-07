package com.cts.driver_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.cts.driver_service.dto.UserResponse;

import java.util.Map;

@FeignClient(name = "USER-SERVICE")
public interface UserServiceClient {

    @GetMapping("/api/v1/internal/users/{userId}")
    UserResponse getUserById(@PathVariable("userId") String userId);
    
    @PutMapping("/api/v1/internal/users/{userId}/status")
    void updateUserStatus(@PathVariable("userId") String userId,
                          @RequestBody Map<String, String> statusUpdate);
}