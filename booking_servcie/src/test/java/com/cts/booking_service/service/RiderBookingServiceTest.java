package com.cts.booking_service.service;

import com.cts.booking_service.dao.RiderBookingDao;
import com.cts.booking_service.dto.rider.CreateBookingRequest;
import com.cts.booking_service.dto.rider.RiderBookingResponse;
import com.cts.booking_service.entity.Booking;
import com.cts.booking_service.exception.BookingNotFoundException;
import com.cts.booking_service.exception.InvalidVehicleTypeException;
import com.cts.booking_service.exception.UnauthorizedAccessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Rider Booking Service Tests")
class RiderBookingServiceTest {

    @Mock
    private RiderBookingDao riderBookingDao;

    @InjectMocks
    private RiderBookingService riderBookingService;

    private CreateBookingRequest createRequest;
    private Booking booking;

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

        // Setup booking entity
        booking = new Booking();
        booking.setId("booking-123");
        booking.setRiderId("rider-123");
        booking.setPickupLatitude(19.0760);
        booking.setPickupLongitude(72.8777);
        booking.setPickupAddress("Mumbai Central");
        booking.setDropoffLatitude(19.0596);
        booking.setDropoffLongitude(72.8295);
        booking.setDropoffAddress("Bandra West");
        booking.setVehicleType(Booking.VehicleType.SEDAN);
        booking.setFareAmount(new BigDecimal("150.00"));
        booking.setTripDistanceKm(new BigDecimal("5.5"));
        booking.setBookingStatus(Booking.BookingStatus.PENDING);
        booking.setPaymentStatus(Booking.PaymentStatus.PENDING);
        booking.setCreatedAt(OffsetDateTime.now());
    }

    // ==================== METHOD 1: Create Booking ====================

    @Test
    @DisplayName("Should create booking successfully")
    void testCreateBooking_Success() {
        // Given
        when(riderBookingDao.save(any(Booking.class))).thenReturn(booking);

        // When
        RiderBookingResponse response = riderBookingService.createBooking("rider-123", createRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getRiderId()).isEqualTo("rider-123");
        assertThat(response.getPickupAddress()).isEqualTo("Mumbai Central");
        assertThat(response.getDropoffAddress()).isEqualTo("Bandra West");
        assertThat(response.getVehicleType()).isEqualTo("sedan");
        assertThat(response.getBookingStatus()).isEqualTo("pending");

        verify(riderBookingDao, times(1)).save(any(Booking.class));
    }

    @Test
    @DisplayName("Should throw exception for invalid vehicle type")
    void testCreateBooking_InvalidVehicleType() {
        // Given
        createRequest.setVehicleType("INVALID_TYPE");

        // When & Then
        assertThatThrownBy(() -> riderBookingService.createBooking("rider-123", createRequest))
                .isInstanceOf(InvalidVehicleTypeException.class)
                .hasMessageContaining("INVALID_TYPE");

        verify(riderBookingDao, never()).save(any(Booking.class));
    }

    // ==================== METHOD 2: Cancel Booking ====================

    @Test
    @DisplayName("Should cancel booking successfully")
    void testCancelBooking_Success() {
        // Given
        booking.setBookingStatus(Booking.BookingStatus.PENDING);
        when(riderBookingDao.findById("booking-123")).thenReturn(Optional.of(booking));
        when(riderBookingDao.update(any(Booking.class))).thenReturn(booking);

        // When
        RiderBookingResponse response = riderBookingService.cancelBooking("booking-123", "rider-123");

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo("booking-123");

        verify(riderBookingDao, times(1)).findById("booking-123");
        verify(riderBookingDao, times(1)).update(any(Booking.class));
    }

    @Test
    @DisplayName("Should throw exception when booking not found")
    void testCancelBooking_NotFound() {
        // Given
        when(riderBookingDao.findById("booking-123")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> riderBookingService.cancelBooking("booking-123", "rider-123"))
                .isInstanceOf(BookingNotFoundException.class)
                .hasMessageContaining("booking-123");

        verify(riderBookingDao, times(1)).findById("booking-123");
        verify(riderBookingDao, never()).update(any(Booking.class));
    }

    @Test
    @DisplayName("Should throw exception when user is unauthorized")
    void testCancelBooking_Unauthorized() {
        // Given
        booking.setRiderId("rider-123");
        when(riderBookingDao.findById("booking-123")).thenReturn(Optional.of(booking));

        // When & Then
        assertThatThrownBy(() -> riderBookingService.cancelBooking("booking-123", "different-user"))
                .isInstanceOf(UnauthorizedAccessException.class);

        verify(riderBookingDao, times(1)).findById("booking-123");
        verify(riderBookingDao, never()).update(any(Booking.class));
    }
}