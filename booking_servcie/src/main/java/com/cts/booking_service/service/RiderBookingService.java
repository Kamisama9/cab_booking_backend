package com.cts.booking_service.service;

import com.cts.booking_service.client.UserServiceClient;
import com.cts.booking_service.client.dto.UserResponse;
import com.cts.booking_service.dao.RiderBookingDao;
import com.cts.booking_service.dto.common.PageResponse;
import com.cts.booking_service.dto.rider.*;
import com.cts.booking_service.entity.Booking;
import com.cts.booking_service.exception.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RiderBookingService {

    private final RiderBookingDao riderBookingDao;
    private final UserServiceClient userServiceClient;

    @Transactional
    public RiderBookingResponse createBooking(String riderId, CreateBookingRequest request) {
        log.info("Creating booking for rider: {}", riderId);

        validateCreateBookingRequest(request);

        Booking booking = new Booking();
        booking.setRiderId(riderId);
        booking.setPickupLatitude(request.getPickupLatitude());
        booking.setPickupLongitude(request.getPickupLongitude());
        booking.setPickupAddress(request.getPickupAddress());
        booking.setDropoffLatitude(request.getDropoffLatitude());
        booking.setDropoffLongitude(request.getDropoffLongitude());
        booking.setDropoffAddress(request.getDropoffAddress());


        try {
            booking.setVehicleType(Booking.VehicleType.valueOf(request.getVehicleType().toUpperCase()));
        } catch (Exception e) {
            log.error("Invalid vehicle type: {}", request.getVehicleType());
            throw new InvalidVehicleTypeException(request.getVehicleType(), "AUTO, BIKE, SEDAN, SUV");
        }


        BigDecimal distance = calculateDistance(
                request.getPickupLatitude(), request.getPickupLongitude(),
                request.getDropoffLatitude(), request.getDropoffLongitude()
        );
        booking.setTripDistanceKm(distance);
        booking.setFareAmount(calculateFare(distance, booking.getVehicleType()));
        booking.setTripDurationMinutes(calculateDuration(distance));

        // Set initial status
        booking.setBookingStatus(Booking.BookingStatus.PENDING);
        booking.setPaymentStatus(Booking.PaymentStatus.PENDING);
        booking.setRequestTime(OffsetDateTime.now());

        // Save via DAO
        Booking savedBooking = riderBookingDao.save(booking);

        log.info("Booking created successfully: {}", savedBooking.getId());

        // TODO: Trigger driver search/assignment

        return RiderBookingResponse.fromEntity(savedBooking);
    }


    @Transactional(readOnly = true)
    public PageResponse<RiderBookingResponse> getMyBookings(
            String riderId,
            String filterType,
            String searchTerm,
            String status,
            int page,
            int size) {

        log.info("Fetching bookings for rider: {} (filter: {}, search: {}, status: {})",
                riderId, filterType, searchTerm, status);

        Page<Booking> bookingsPage;
        if (status != null && !status.isBlank()) {
            try {
                Booking.BookingStatus bookingStatus = Booking.BookingStatus.valueOf(status.toUpperCase());
                bookingsPage = riderBookingDao.findByRiderIdAndStatus(riderId, bookingStatus, page, size);
            } catch (IllegalArgumentException e) {
                log.error("Invalid status: {}", status);
                throw new InvalidRequestException("Invalid booking status: " + status + ". Valid values are: PENDING, ACCEPTED, STARTED, COMPLETED, CANCELLED");
            }
        }
        // Filter by pickup address
        else if ("pickup".equals(filterType) && searchTerm != null && !searchTerm.isBlank()) {
            bookingsPage = riderBookingDao.searchByPickupAddress(riderId, searchTerm, page, size);
        }
        // Filter by dropoff address
        else if ("dropoff".equals(filterType) && searchTerm != null && !searchTerm.isBlank()) {
            bookingsPage = riderBookingDao.searchByDropoffAddress(riderId, searchTerm, page, size);
        }
        // Filter by travel date
        else if ("travel_date".equals(filterType) && searchTerm != null && !searchTerm.isBlank()) {
            try {
                OffsetDateTime date = OffsetDateTime.parse(searchTerm + "T00:00:00Z");
                bookingsPage = riderBookingDao.searchByDate(riderId, date, page, size);
            } catch (Exception e) {
                log.error("Invalid date format: {}", searchTerm);
                throw new InvalidRequestException("Invalid date format. Use YYYY-MM-DD");
            }
        }
        // No filter - get all
        else {
            bookingsPage = riderBookingDao.findAllByRiderId(riderId, page, size);
        }

        // Convert to response
        List<RiderBookingResponse> content = bookingsPage.getContent()
                .stream()
                .map(RiderBookingResponse::fromEntity)
                .collect(Collectors.toList());

        // Populate driver details
        content = populateDriverDetailsForList(content);

        PageResponse<RiderBookingResponse> response = new PageResponse<>();
        response.setContent(content);
        response.setPage(bookingsPage.getNumber());
        response.setSize(bookingsPage.getSize());
        response.setTotalElements(bookingsPage.getTotalElements());
        response.setTotalPages(bookingsPage.getTotalPages());
        response.setLast(bookingsPage.isLast());

        return response;
    }

    @Transactional
    public RiderBookingResponse cancelBooking(String bookingId, String riderId) {
        log.info("Cancelling booking {} by rider {}", bookingId, riderId);

        Booking booking = riderBookingDao.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException(bookingId, true));

        if (!booking.getRiderId().equals(riderId)) {
            throw new UnauthorizedAccessException(riderId, bookingId);
        }

        // Can only cancel PENDING or ACCEPTED
        if (booking.getBookingStatus() != Booking.BookingStatus.PENDING &&
                booking.getBookingStatus() != Booking.BookingStatus.ACCEPTED) {
            throw new InvalidBookingStatusException("Cannot cancel booking in " + booking.getBookingStatus() + " status");
        }

        booking.setBookingStatus(Booking.BookingStatus.CANCELLED);
        booking.setCancelledBy(Booking.CancelledBy.RIDER);
        booking.setCancellationReason("Cancelled by rider");

        Booking updated = riderBookingDao.update(booking);
        return RiderBookingResponse.fromEntity(updated);
    }

    @Transactional(readOnly = true)
    public RiderBookingResponse getBookingDetails(String bookingId, String riderId) {
        log.info("Fetching booking details: {} for rider: {}", bookingId, riderId);

        Booking booking = riderBookingDao.findById(bookingId)
                .orElseThrow(() -> {
                    log.error("Booking not found: {}", bookingId);
                    return new BookingNotFoundException(bookingId, true);
                });

        // Verify ownership
        if (!booking.getRiderId().equals(riderId)) {
            log.error("Unauthorized access to booking {} by rider {}", bookingId, riderId);
            throw new UnauthorizedAccessException(riderId, bookingId);
        }

        RiderBookingResponse response = RiderBookingResponse.fromEntity(booking);
        return populateDriverDetails(response);
    }


    //TODO : Cancel Booking method
    //TODO : Rate Driver method



    private void validateCreateBookingRequest(CreateBookingRequest request) {
        if (request.getPickupLatitude() == null || request.getPickupLongitude() == null) {
            throw new InvalidRequestException("Pickup location coordinates (latitude and longitude) are required");
        }
        if (request.getDropoffLatitude() == null || request.getDropoffLongitude() == null) {
            throw new InvalidRequestException("Dropoff location coordinates (latitude and longitude) are required");
        }
        if (request.getPickupAddress() == null || request.getPickupAddress().isBlank()) {
            throw new InvalidRequestException("Pickup address is required");
        }
        if (request.getDropoffAddress() == null || request.getDropoffAddress().isBlank()) {
            throw new InvalidRequestException("Dropoff address is required");
        }
        if (request.getVehicleType() == null || request.getVehicleType().isBlank()) {
            throw new InvalidRequestException("Vehicle type is required");
        }
    }

    private BigDecimal calculateDistance(Double lat1, Double lon1, Double lat2, Double lon2) {
        // Haversine formula
        final int R = 6371; // Earth radius in km

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        double distance = R * c;

        return BigDecimal.valueOf(Math.round(distance * 100.0) / 100.0);
    }

    private BigDecimal calculateFare(BigDecimal distance, Booking.VehicleType vehicleType) {
        // Base fare + per km rate
        BigDecimal baseFare = switch (vehicleType) {
            case AUTO -> BigDecimal.valueOf(30);
            case BIKE -> BigDecimal.valueOf(20);
            case SEDAN -> BigDecimal.valueOf(50);
            case SUV -> BigDecimal.valueOf(70);
        };

        BigDecimal perKmRate = switch (vehicleType) {
            case AUTO -> BigDecimal.valueOf(12);
            case BIKE -> BigDecimal.valueOf(8);
            case SEDAN -> BigDecimal.valueOf(15);
            case SUV -> BigDecimal.valueOf(20);
        };

        return baseFare.add(distance.multiply(perKmRate))
                .setScale(2, RoundingMode.HALF_UP);
    }

    private Integer calculateDuration(BigDecimal distance) {
        // Assume average speed of 30 km/h
        double hours = distance.doubleValue() / 30.0;
        return (int) Math.round(hours * 60); // Convert to minutes
    }

    // Helper method to fetch and populate driver details
    private RiderBookingResponse populateDriverDetails(RiderBookingResponse response) {
        if (response.getDriverId() != null) {
            try {
                UserResponse driver = userServiceClient.getUserById(response.getDriverId());
                if (driver != null) {
                    response.setDriverName(driver.getFirstName() + " " + driver.getLastName());
                    response.setDriverPhone(driver.getPhoneNumber());
                    log.debug("Fetched driver details for driverId: {}", response.getDriverId());
                }
            } catch (Exception e) {
                log.warn("Failed to fetch driver details for driverId: {}. Error: {}",
                    response.getDriverId(), e.getMessage());
                // Continue without driver details - don't fail the entire request
            }
        }
        return response;
    }

    private List<RiderBookingResponse> populateDriverDetailsForList(List<RiderBookingResponse> responses) {
        return responses.stream()
                .map(this::populateDriverDetails)
                .collect(Collectors.toList());
    }
}