package com.cts.auth_service.config;

import feign.Logger;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {

    @Bean
    Logger.Level feignLoggerLevel() {
        return Logger.Level.BASIC;  // ✅ Changed from FULL (too verbose)
    }

    @Bean
    public ErrorDecoder errorDecoder() {
        return new ErrorDecoder.Default();  // ✅ Use default, let exceptions propagate
    }
}