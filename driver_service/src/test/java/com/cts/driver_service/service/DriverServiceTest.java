package com.cts.driver_service.service;

import com.cts.driver_service.dto.DriverProfileRequest;
import com.cts.driver_service.dto.DriverResponse;
import com.cts.driver_service.entity.Driver;
import com.cts.driver_service.exception.DriverNotFoundException;
import com.cts.driver_service.exception.InvalidDateFormatException;
import com.cts.driver_service.repository.DriverRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Driver Service Tests")
class DriverServiceTest {

    @Mock
    private DriverRepository driverRepository;

    @InjectMocks
    private DriverService driverService;

    private Driver driver;
    private DriverProfileRequest profileRequest;

    @BeforeEach
    void setUp() {
        // Setup driver entity
        driver = new Driver();
        driver.setId("driver-123");
        driver.setUserId("user-123");
        driver.setAadhaarNumber("123456789012");
        driver.setDlNumber("MH01-2023-1234567");
        driver.setDlExpiryDate(LocalDate.parse("2030-12-31"));
        driver.setVerificationStatus(Driver.VerificationStatus.PENDING);

        // Setup profile request
        profileRequest = new DriverProfileRequest();
        profileRequest.setAadhaarNumber("123456789012");
        profileRequest.setDlNumber("MH01-2023-1234567");
        profileRequest.setDlExpiryDate("2030-12-31");
    }

    // ==================== METHOD 1: Get Driver Profile ====================

    @Test
    @DisplayName("Should get driver profile successfully")
    void testGetDriverProfile_Success() {
        // Given
        when(driverRepository.findByUserId("user-123")).thenReturn(Optional.of(driver));

        // When
        DriverResponse response = driverService.getDriverProfile("user-123");

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getUserId()).isEqualTo("user-123");
        assertThat(response.getAadhaarNumber()).isEqualTo("123456789012");
        assertThat(response.getVerificationStatus()).isEqualTo("PENDING");

        verify(driverRepository, times(1)).findByUserId("user-123");
    }

    @Test
    @DisplayName("Should throw exception when driver not found")
    void testGetDriverProfile_NotFound() {
        // Given
        when(driverRepository.findByUserId("user-123")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> driverService.getDriverProfile("user-123"))
                .isInstanceOf(DriverNotFoundException.class)
                .hasMessageContaining("user-123");

        verify(driverRepository, times(1)).findByUserId("user-123");
    }

    // ==================== METHOD 2: Create/Update Profile ====================

    @Test
    @DisplayName("Should create new driver profile")
    void testCreateProfile_NewDriver() {
        // Given
        when(driverRepository.findByUserId("user-123")).thenReturn(Optional.empty());
        when(driverRepository.save(any(Driver.class))).thenReturn(driver);

        // When
        Driver result = driverService.createOrUpdateProfile("user-123", profileRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo("user-123");

        verify(driverRepository, times(1)).findByUserId("user-123");
        verify(driverRepository, times(1)).save(any(Driver.class));
    }

    @Test
    @DisplayName("Should update existing driver profile")
    void testUpdateProfile_ExistingDriver() {
        // Given
        when(driverRepository.findByUserId("user-123")).thenReturn(Optional.of(driver));
        when(driverRepository.save(any(Driver.class))).thenReturn(driver);

        // When
        Driver result = driverService.createOrUpdateProfile("user-123", profileRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("driver-123");

        verify(driverRepository, times(1)).findByUserId("user-123");
        verify(driverRepository, times(1)).save(any(Driver.class));
    }

    @Test
    @DisplayName("Should throw exception for expired license")
    void testCreateProfile_ExpiredLicense() {
        // Given
        profileRequest.setDlExpiryDate("2020-01-01"); // Expired date

        // When & Then
        assertThatThrownBy(() -> driverService.createOrUpdateProfile("user-123", profileRequest))
                .isInstanceOf(InvalidDateFormatException.class)
                .hasMessageContaining("expired");

        verify(driverRepository, never()).save(any(Driver.class));
    }
}