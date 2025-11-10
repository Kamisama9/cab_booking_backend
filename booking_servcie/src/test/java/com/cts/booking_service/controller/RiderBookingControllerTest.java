package com.cts.booking_service.controller;

import com.cts.booking_service.dto.rider.CreateBookingRequest;
import com.cts.booking_service.dto.rider.RiderBookingResponse;
import com.cts.booking_service.exception.MissingHeaderException;
import com.cts.booking_service.service.RiderBookingService;
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

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Rider Booking Controller Tests")
class RiderBookingControllerTest {

    @Mock
    private RiderBookingService riderBookingService;

    @Mock
    private HttpServletRequest httpRequest;

    @InjectMocks
    private RiderBookingController riderBookingController;

    private CreateBookingRequest createRequest;
    private RiderBookingResponse bookingResponse;

    @BeforeEach
    void setUp() {
        // Setup request
        createRequest = new CreateBookingRequest();
        createRequest.setPickupLatitude(19.0760);
        createRequest.setPickupLongitude(72.8777);
        createRequest.setPickupAddress("Mumbai Central");
        createRequest.setDropoffLatitude(19.0596);
        createRequest.setDropoffLongitude(72.8295);
        createRequest.setDropoffAddress("Bandra West");
        createRequest.setVehicleType("SEDAN");

        // Setup response
        bookingResponse = new RiderBookingResponse();
        bookingResponse.setId("booking-123");
        bookingResponse.setRiderId("user-123");
        bookingResponse.setPickupAddress("Mumbai Central");
        bookingResponse.setDropoffAddress("Bandra West");
        bookingResponse.setVehicleType("sedan");
        bookingResponse.setFareAmount(new BigDecimal("150.00"));
        bookingResponse.setBookingStatus("pending");
    }

    // ==================== ENDPOINT 1: Create Booking ====================

    @Test
    @DisplayName("Should create booking successfully")
    void testCreateBooking_Success() {
        // Given
        when(httpRequest.getHeader("X-User-Id")).thenReturn("user-123");
        when(riderBookingService.createBooking(eq("user-123"), any(CreateBookingRequest.class)))
                .thenReturn(bookingResponse);

        // When
        ResponseEntity<RiderBookingResponse> response = 
                riderBookingController.createBooking(httpRequest, createRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo("booking-123");
        assertThat(response.getBody().getBookingStatus()).isEqualTo("pending");

        verify(riderBookingService, times(1))
                .createBooking(eq("user-123"), any(CreateBookingRequest.class));
    }

    @Test
    @DisplayName("Should throw exception when X-User-Id header is missing")
    void testCreateBooking_MissingHeader() {
        // Given
        when(httpRequest.getHeader("X-User-Id")).thenReturn(null);

        // When & Then
        assertThatThrownBy(() -> riderBookingController.createBooking(httpRequest, createRequest))
                .isInstanceOf(MissingHeaderException.class)
                .hasMessageContaining("X-User-Id");

        verify(riderBookingService, never())
                .createBooking(anyString(), any(CreateBookingRequest.class));
    }

    // ==================== ENDPOINT 2: Cancel Booking ====================

    @Test
    @DisplayName("Should cancel booking successfully")
    void testCancelBooking_Success() {
        // Given
        bookingResponse.setBookingStatus("cancelled");
        when(httpRequest.getHeader("X-User-Id")).thenReturn("user-123");
        when(riderBookingService.cancelBooking("booking-123", "user-123"))
                .thenReturn(bookingResponse);

        // When
        ResponseEntity<RiderBookingResponse> response = 
                riderBookingController.cancelBooking(httpRequest, "booking-123");

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getBookingStatus()).isEqualTo("cancelled");

        verify(riderBookingService, times(1))
                .cancelBooking("booking-123", "user-123");
    }

    @Test
    @DisplayName("Should throw exception when header missing during cancel")
    void testCancelBooking_MissingHeader() {
        // Given
        when(httpRequest.getHeader("X-User-Id")).thenReturn(null);

        // When & Then
        assertThatThrownBy(() -> riderBookingController.cancelBooking(httpRequest, "booking-123"))
                .isInstanceOf(MissingHeaderException.class);

        verify(riderBookingService, never()).cancelBooking(anyString(), anyString());
    }
}