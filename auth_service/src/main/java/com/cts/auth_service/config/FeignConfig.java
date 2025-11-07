package com.cts.auth_service.config;

import com.cts.auth_service.exception.AuthenticationException;
import com.cts.auth_service.exception.UserServiceException;
import com.cts.auth_service.exception.ValidationException;
import feign.Logger;
import feign.Response;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {

    @Bean
    Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }

    @Bean
    public ErrorDecoder errorDecoder() {
        return new CustomErrorDecoder();
    }

    static class CustomErrorDecoder implements ErrorDecoder {
        private final ErrorDecoder defaultErrorDecoder = new Default();

        @Override
        public Exception decode(String methodKey, Response response) {
            switch (response.status()) {
                case 400:
                    return new ValidationException("Invalid request data provided");
                case 401:
                    return new AuthenticationException("Invalid email or password");
                case 403:
                    return new UserServiceException("Access denied to user service");
                case 404:
                    return new AuthenticationException("User not found");
                case 409:
                    return new ValidationException("User with this email already exists");
                case 500:
                case 502:
                case 503:
                case 504:
                    return new UserServiceException("User service is temporarily unavailable. Please try again later.");
                default:
                    if (response.status() >= 400 && response.status() <= 499) {
                        return new ValidationException("Client error: " + response.reason());
                    }
                    if (response.status() >= 500 && response.status() <= 599) {
                        return new UserServiceException("Server error: " + response.reason());
                    }
                    return defaultErrorDecoder.decode(methodKey, response);
            }
        }
    }
}

