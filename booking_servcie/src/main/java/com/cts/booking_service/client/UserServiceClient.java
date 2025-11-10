package com.cts.booking_service.client;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import com.cts.booking_service.dto.UserResponse;



@FeignClient(name = "USER-SERVICE")
public interface UserServiceClient {

    @GetMapping("/api/v1/internal/users/{userId}")
    UserResponse getUserById(@PathVariable("userId") String userId);
}

