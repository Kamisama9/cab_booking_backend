package com.cts.booking_service.service;

import com.cts.booking_service.client.UserServiceClient;
import com.cts.booking_service.client.dto.UserResponse;
import com.cts.booking_service.dao.DriverBookingDao;
import com.cts.booking_service.dto.common.PageResponse;
import com.cts.booking_service.dto.driver.*;
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
public class DriverBookingService {

    private final DriverBookingDao driverBookingDao;

    private final UserServiceClient userServiceClient;


    @Transactional(readOnly = true)
    public List<DriverBookingResponse> getAvailableBookings(String driverId, String vehicleType) {
        log.info("Fetching available bookings for driver: {} (vehicleType: {})", driverId, vehicleType);

        if (driverBookingDao.hasActiveBooking(driverId)) {
            log.warn("Driver {} already has an active booking", driverId);
            throw new ActiveBookingExistsException("You already have an active booking. Complete it first before accepting new bookings.");
        }

        List<Booking> bookings;

        if (vehicleType != null && !vehicleType.isBlank()) {
            try {
                Booking.VehicleType type = Booking.VehicleType.valueOf(vehicleType.toUpperCase());
                bookings = driverBookingDao.findAvailableBookingsByVehicleType(type);
            } catch (IllegalArgumentException e) {
                log.error("Invalid vehicle type: {}", vehicleType);
                throw new InvalidVehicleTypeException(vehicleType, "AUTO, BIKE, SEDAN, SUV");
            }
        } else {
            bookings = driverBookingDao.findAvailableBookings();
        }

        log.info("Found {} available bookings", bookings.size());

        List<DriverBookingResponse> responses = bookings.stream()
                .map(DriverBookingResponse::fromEntity)
                .collect(Collectors.toList());

        return populateRiderDetailsForList(responses);
    }

    @Transactional(readOnly = true)
    public PageResponse<DriverBookingResponse> getMyBookings(
            String driverId,
            String status,
            int page,
            int size) {

        log.info("Fetching bookings for driver: {} (status: {}, page: {}, size: {})",
                driverId, status, page, size);

        Page<Booking> bookingsPage;

        if (status != null && !status.isBlank()) {
            try {
                Booking.BookingStatus bookingStatus = Booking.BookingStatus.valueOf(status.toUpperCase());
                bookingsPage = driverBookingDao.findByDriverIdAndStatus(driverId, bookingStatus, page, size);
            } catch (IllegalArgumentException e) {
                log.error("Invalid status: {}", status);
                throw new InvalidRequestException("Invalid booking status: " + status + ". Valid values are: PENDING, ACCEPTED, STARTED, COMPLETED, CANCELLED");
            }
        } else {
            bookingsPage = driverBookingDao.findAllByDriverId(driverId, page, size);
        }

        // Convert to response
        List<DriverBookingResponse> content = bookingsPage.getContent()
                .stream()
                .map(DriverBookingResponse::fromEntity)
                .collect(Collectors.toList());

        // Populate rider details
        content = populateRiderDetailsForList(content);

        PageResponse<DriverBookingResponse> response = new PageResponse<>();
        response.setContent(content);
        response.setPage(bookingsPage.getNumber());
        response.setSize(bookingsPage.getSize());
        response.setTotalElements(bookingsPage.getTotalElements());
        response.setTotalPages(bookingsPage.getTotalPages());
        response.setLast(bookingsPage.isLast());

        return response;
    }

    @Transactional(readOnly = true)
    public DriverBookingResponse getActiveBooking(String driverId) {
        log.info("Fetching active booking for driver: {}", driverId);

        return driverBookingDao.findActiveBooking(driverId)
                .map(DriverBookingResponse::fromEntity)
                .map(this::populateRiderDetails)
                .orElse(null);
    }

    @Transactional
    public DriverBookingResponse acceptBooking(
            String bookingId,
            String driverId,
            AcceptBookingRequest request) {

        log.info("Driver {} accepting booking {}", driverId, bookingId);

        // Check if driver already has an active booking
        if (driverBookingDao.hasActiveBooking(driverId)) {
            log.error("Driver {} already has an active booking", driverId);
            throw new ActiveBookingExistsException("You already have an active booking. Complete it first before accepting new bookings.");
        }

        // Find booking
        Booking booking = driverBookingDao.findById(bookingId)
                .orElseThrow(() -> {
                    log.error("Booking not found: {}", bookingId);
                    return new BookingNotFoundException(bookingId, true);
                });

        // Validate booking status
        if (booking.getBookingStatus() != Booking.BookingStatus.PENDING) {
            log.error("Booking {} is not in PENDING status (current: {})",
                    bookingId, booking.getBookingStatus());
            throw new InvalidBookingStatusException("This booking is no longer available. Current status: " + booking.getBookingStatus().name().toLowerCase());
        }

        // Validate booking not already assigned
        if (booking.getDriverId() != null) {
            log.error("Booking {} already assigned to driver {}", bookingId, booking.getDriverId());
            throw new BookingAlreadyAssignedException(bookingId, booking.getDriverId());
        }

        // Assign driver
        booking.setDriverId(driverId);
        booking.setVehicleId(request.getVehicleId());
        booking.setBookingStatus(Booking.BookingStatus.ACCEPTED);
        booking.setUpdatedAt(OffsetDateTime.now());

        Booking updated = driverBookingDao.save(booking);

        log.info("Booking {} accepted successfully by driver {}", bookingId, driverId);

        // TODO: Notify rider that driver accepted
        // TODO: Update driver availability in Driver Service (set to busy)

        DriverBookingResponse response = DriverBookingResponse.fromEntity(updated);
        return populateRiderDetails(response);
    }

   // TODO: Don't accept ride


    @Transactional
    public DriverBookingResponse startRide(String bookingId, String driverId) {
        log.info("Driver {} starting ride {}", driverId, bookingId);

        Booking booking = driverBookingDao.findById(bookingId)
                .orElseThrow(() -> {
                    log.error("Booking not found: {}", bookingId);
                    return new BookingNotFoundException(bookingId, true);
                });

        // Verify ownership
        if (!driverId.equals(booking.getDriverId())) {
            log.error("Unauthorized: Driver {} trying to start booking owned by {}",
                    driverId, booking.getDriverId());
            throw new UnauthorizedAccessException(driverId, bookingId);
        }

        // Validate status
        if (booking.getBookingStatus() != Booking.BookingStatus.ACCEPTED) {
            log.error("Cannot start ride in {} status", booking.getBookingStatus());
            throw new InvalidBookingStatusException("Cannot start ride. Booking must be in 'accepted' status. Current status: " + booking.getBookingStatus().name().toLowerCase());
        }

        // Update booking
        booking.setBookingStatus(Booking.BookingStatus.STARTED);
        booking.setPickupTime(OffsetDateTime.now());
        booking.setUpdatedAt(OffsetDateTime.now());

        Booking updated = driverBookingDao.save(booking);

        log.info("Ride {} started successfully", bookingId);

        // TODO: Notify rider that ride has started

        DriverBookingResponse response = DriverBookingResponse.fromEntity(updated);
        return populateRiderDetails(response);
    }


    @Transactional
    public DriverBookingResponse completeRide(
            String bookingId,
            String driverId,
            CompleteBookingRequest request) {

        log.info("Driver {} completing ride {}", driverId, bookingId);

        Booking booking = driverBookingDao.findById(bookingId)
                .orElseThrow(() -> {
                    log.error("Booking not found: {}", bookingId);
                    return new BookingNotFoundException(bookingId, true);
                });

        // Verify ownership
        if (!driverId.equals(booking.getDriverId())) {
            log.error("Unauthorized: Driver {} trying to complete booking owned by {}",
                    driverId, booking.getDriverId());
            throw new UnauthorizedAccessException(driverId, bookingId);
        }

        // Validate status
        if (booking.getBookingStatus() != Booking.BookingStatus.STARTED) {
            log.error("Cannot complete ride in {} status", booking.getBookingStatus());
            throw new InvalidBookingStatusException("Cannot complete ride. Booking must be in 'started' status. Current status: " + booking.getBookingStatus().name().toLowerCase());
        }

        // Update trip details
        if (request.getFinalDistanceKm() != null) {
            booking.setTripDistanceKm(request.getFinalDistanceKm());
        }

        if (request.getFinalDurationMinutes() != null) {
            booking.setTripDurationMinutes(request.getFinalDurationMinutes());
        }

        // Calculate final fare
        if (request.getFinalFare() != null) {
            booking.setFareAmount(request.getFinalFare());
        } else if (request.getFinalDistanceKm() != null) {
            // Recalculate fare based on actual distance
            BigDecimal newFare = calculateFare(request.getFinalDistanceKm(), booking.getVehicleType());
            booking.setFareAmount(newFare);
        }

        // Update status
        booking.setBookingStatus(Booking.BookingStatus.COMPLETED);
        booking.setDropoffTime(OffsetDateTime.now());
        booking.setPaymentStatus(Booking.PaymentStatus.PENDING);
        booking.setUpdatedAt(OffsetDateTime.now());

        Booking updated = driverBookingDao.save(booking);

        log.info("Ride {} completed successfully (Fare: {})", bookingId, booking.getFareAmount());

        // TODO: Notify rider that ride is complete
        // TODO: Trigger payment processing
        // TODO: Make driver available again in Driver Service

        DriverBookingResponse response = DriverBookingResponse.fromEntity(updated);
        return populateRiderDetails(response);
    }

    /**
     * CANCEL BOOKING (by Driver)
     */
    // TODO: Implement cancellation with reasons and possible penalties
    // TODO: Rate Rider

    @Transactional(readOnly = true)
    public DriverBookingResponse getBookingDetails(String bookingId, String driverId) {
        log.info("Fetching booking details: {} for driver: {}", bookingId, driverId);

        Booking booking = driverBookingDao.findById(bookingId)
                .orElseThrow(() -> {
                    log.error("Booking not found: {}", bookingId);
                    return new BookingNotFoundException(bookingId, true);
                });

        // Allow viewing if it's a pending booking or if driver owns it
        if (booking.getDriverId() != null && !booking.getDriverId().equals(driverId)) {
            log.error("Unauthorized access to booking {} by driver {}", bookingId, driverId);
            throw new UnauthorizedAccessException(driverId, bookingId);
        }

        DriverBookingResponse response = DriverBookingResponse.fromEntity(booking);
        return populateRiderDetails(response);
    }

    // Helper method to fetch and populate rider details
    private DriverBookingResponse populateRiderDetails(DriverBookingResponse response) {
        if (response.getRiderId() != null) {
            try {
                UserResponse rider = userServiceClient.getUserById(response.getRiderId());
                if (rider != null) {
                    response.setRiderName(rider.getFirstName() + " " + rider.getLastName());
                    response.setRiderPhone(rider.getPhoneNumber());
                    log.debug("Fetched rider details for riderId: {}", response.getRiderId());
                }
            } catch (Exception e) {
                log.warn("Failed to fetch rider details for riderId: {}. Error: {}",
                    response.getRiderId(), e.getMessage());
                // Continue without rider details - don't fail the entire request
            }
        }
        return response;
    }

    private List<DriverBookingResponse> populateRiderDetailsForList(List<DriverBookingResponse> responses) {
        return responses.stream()
                .map(this::populateRiderDetails)
                .collect(Collectors.toList());
    }

    // Helper method
    private BigDecimal calculateFare(BigDecimal distance, Booking.VehicleType vehicleType) {
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
}