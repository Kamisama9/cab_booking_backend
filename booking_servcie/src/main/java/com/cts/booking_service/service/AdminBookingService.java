package com.cts.booking_service.service;

import com.cts.booking_service.client.UserServiceClient;
import com.cts.booking_service.dto.UserResponse;
import com.cts.booking_service.dao.RiderBookingDao;
import com.cts.booking_service.dto.common.PageResponse;
import com.cts.booking_service.dto.rider.RiderBookingResponse;
import com.cts.booking_service.entity.Booking;
import com.cts.booking_service.exception.BookingNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminBookingService {

    private final RiderBookingDao riderBookingDao;
    private final UserServiceClient userServiceClient;

    @Transactional(readOnly = true)
    public PageResponse<RiderBookingResponse> getAllBookings(String status, int page, int size) {
        log.info("Admin fetching all bookings (status: {}, page: {}, size: {})", status, page, size);

        Pageable pageable = PageRequest.of(page, size);
        Page<Booking> bookingsPage;

        // Filter by status if provided
        if (status != null && !status.isBlank()) {
            try {
                Booking.BookingStatus bookingStatus = Booking.BookingStatus.valueOf(status.toUpperCase());
                bookingsPage = riderBookingDao.findByStatus(bookingStatus, pageable);
            } catch (IllegalArgumentException e) {
                log.error("Invalid status: {}", status);
                throw new RuntimeException("Invalid booking status: " + status + ". Valid values are: PENDING, ACCEPTED, STARTED, COMPLETED, CANCELLED");
            }
        } else {
            bookingsPage = riderBookingDao.findAll(pageable);
        }

        // Convert to response DTOs
        List<RiderBookingResponse> content = bookingsPage.getContent()
                .stream()
                .map(RiderBookingResponse::fromEntity)
                .collect(Collectors.toList());

        // Populate user details
        content = populateUserDetails(content);

        // Build paginated response
        PageResponse<RiderBookingResponse> response = new PageResponse<>();
        response.setContent(content);
        response.setPage(bookingsPage.getNumber());
        response.setSize(bookingsPage.getSize());
        response.setTotalElements(bookingsPage.getTotalElements());
        response.setTotalPages(bookingsPage.getTotalPages());
        response.setLast(bookingsPage.isLast());

        log.info("Admin retrieved {} bookings", content.size());
        return response;
    }

    @Transactional(readOnly = true)
    public RiderBookingResponse getBookingById(String bookingId) {
        log.info("Admin fetching booking: {}", bookingId);

        Booking booking = riderBookingDao.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException(bookingId, true));

        RiderBookingResponse response = RiderBookingResponse.fromEntity(booking);
        return populateUserDetails(response);
    }

    // ==================== HELPER METHODS ====================

    /**
     * Populate user details for a list of bookings
     */
    private List<RiderBookingResponse> populateUserDetails(List<RiderBookingResponse> responses) {
        return responses.stream()
                .map(this::populateUserDetails)
                .collect(Collectors.toList());
    }

    /**
     * Fetch and populate driver details from User Service
     */
    private RiderBookingResponse populateUserDetails(RiderBookingResponse response) {
        // Fetch driver name and phone
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
            }
        }
        return response;
    }
}