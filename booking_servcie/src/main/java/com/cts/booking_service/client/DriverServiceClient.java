package com.cts.booking_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "DRIVER-SERVICE")
public interface DriverServiceClient {
    @PutMapping("/api/v1/drivers/me/availability")
    void updateAvailability(@RequestBody Map<String, Boolean> availability);
}
