package com.cts.driver_service.controller;

import com.cts.driver_service.dto.DriverProfileRequest;
import com.cts.driver_service.dto.DriverResponse;
import com.cts.driver_service.entity.Driver;
import com.cts.driver_service.exception.UnauthorizedException;
import com.cts.driver_service.service.DriverService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Driver Controller Tests")
class DriverControllerTest {

    @Mock
    private DriverService driverService;

    @Mock
    private HttpServletRequest httpRequest;

    @InjectMocks
    private DriverController driverController;

    private DriverProfileRequest profileRequest;
    private Driver driver;
    private DriverResponse driverResponse;

    @BeforeEach
    void setUp() {
        // Setup profile request
        profileRequest = new DriverProfileRequest();
        profileRequest.setAadhaarNumber("123456789012");
        profileRequest.setDlNumber("MH01-2023-1234567");
        profileRequest.setDlExpiryDate("2030-12-31");

        // Setup driver entity
        driver = new Driver();
        driver.setId("driver-123");
        driver.setUserId("user-123");
        driver.setAadhaarNumber("123456789012");
        driver.setDlNumber("MH01-2023-1234567");
        driver.setDlExpiryDate(LocalDate.parse("2030-12-31"));
        driver.setVerificationStatus(Driver.VerificationStatus.PENDING);

        // Setup driver response
        driverResponse = DriverResponse.builder()
                .id("driver-123")
                .userId("user-123")
                .aadhaarNumber("123456789012")
                .dlNumber("MH01-2023-1234567")
                .verificationStatus("PENDING")
                .build();
    }

    // ==================== ENDPOINT 1: Get Driver Profile ====================

    @Test
    @DisplayName("Should get driver profile successfully")
    void testGetDriverProfile_Success() {
        // Given
        when(httpRequest.getHeader("X-User-Id")).thenReturn("user-123");
        when(driverService.getDriverProfile("user-123")).thenReturn(driverResponse);

        // When
        ResponseEntity<DriverResponse> response = driverController.getDriverDetails(httpRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo("driver-123");
        assertThat(response.getBody().getUserId()).isEqualTo("user-123");

        verify(driverService, times(1)).getDriverProfile("user-123");
    }

    @Test
    @DisplayName("Should throw exception when X-User-Id header is missing")
    void testGetDriverProfile_MissingHeader() {
        // Given
        when(httpRequest.getHeader("X-User-Id")).thenReturn(null);

        // When & Then
        assertThatThrownBy(() -> driverController.getDriverDetails(httpRequest))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("X-User-Id");

        verify(driverService, never()).getDriverProfile(anyString());
    }

    // ==================== ENDPOINT 2: Create Driver Profile ====================

    @Test
    @DisplayName("Should create driver profile successfully")
    void testCreateProfile_Success() {
        // Given
        when(httpRequest.getHeader("X-User-Id")).thenReturn("user-123");
        when(driverService.createOrUpdateProfile(eq("user-123"), any(DriverProfileRequest.class)))
                .thenReturn(driver);

        // When
        ResponseEntity<Driver> response = driverController.createProfile(httpRequest, profileRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo("driver-123");
        assertThat(response.getBody().getUserId()).isEqualTo("user-123");

        verify(driverService, times(1))
                .createOrUpdateProfile(eq("user-123"), any(DriverProfileRequest.class));
    }

    @Test
    @DisplayName("Should throw exception when header missing during profile creation")
    void testCreateProfile_MissingHeader() {
        // Given
        when(httpRequest.getHeader("X-User-Id")).thenReturn(null);

        // When & Then
        assertThatThrownBy(() -> driverController.createProfile(httpRequest, profileRequest))
                .isInstanceOf(UnauthorizedException.class);

        verify(driverService, never())
                .createOrUpdateProfile(anyString(), any(DriverProfileRequest.class));
    }
}