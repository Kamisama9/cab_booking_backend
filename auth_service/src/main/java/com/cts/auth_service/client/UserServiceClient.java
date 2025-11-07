package com.cts.auth_service.client;


import com.cts.auth_service.config.FeignConfig;
import com.cts.auth_service.dto.LoginRequest;
import com.cts.auth_service.dto.SignupRequest;
import com.cts.auth_service.dto.UserValidationResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name="USER-SERVICE", configuration = FeignConfig.class)
public interface UserServiceClient {

    @PostMapping("/api/v1/internal/validate-credentials")
    ResponseEntity<UserValidationResponse> validateCredentials(@RequestBody LoginRequest request);

    @PostMapping("/api/v1/internal/register")
    ResponseEntity<String> registerUser(@RequestBody SignupRequest request);
}
