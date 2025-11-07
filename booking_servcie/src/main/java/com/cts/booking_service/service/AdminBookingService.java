package com.cts.booking_service.service;

import com.cts.booking_service.client.UserServiceClient;
import com.cts.booking_service.client.dto.UserResponse;
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

        if (status != null && !status.isBlank()) {
            try {
                Booking.BookingStatus bookingStatus = Booking.BookingStatus.valueOf(status.toUpperCase());
                // You'll need to add this method to RiderBookingDao
                bookingsPage = riderBookingDao.findByStatus(bookingStatus, pageable);
            } catch (IllegalArgumentException e) {
                log.error("Invalid status: {}", status);
                throw new RuntimeException("Invalid booking status: " + status);
            }
        } else {
            // Get all bookings - you'll need to add this method
            bookingsPage = riderBookingDao.findAll(pageable);
        }

        List<RiderBookingResponse> content = bookingsPage.getContent()
                .stream()
                .map(RiderBookingResponse::fromEntity)
                .collect(Collectors.toList());

        // Populate driver/rider details
        content = populateUserDetails(content);

        PageResponse<RiderBookingResponse> response = new PageResponse<>();
        response.setContent(content);
        response.setPage(bookingsPage.getNumber());
        response.setSize(bookingsPage.getSize());
        response.setTotalElements(bookingsPage.getTotalElements());
        response.setTotalPages(bookingsPage.getTotalPages());
        response.setLast(bookingsPage.isLast());

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

    private List<RiderBookingResponse> populateUserDetails(List<RiderBookingResponse> responses) {
        return responses.stream()
                .map(this::populateUserDetails)
                .collect(Collectors.toList());
    }

    private RiderBookingResponse populateUserDetails(RiderBookingResponse response) {
        // Fetch driver name
        if (response.getDriverId() != null) {
            try {
                UserResponse driver = userServiceClient.getUserById(response.getDriverId());
                if (driver != null) {
                    response.setDriverName(driver.getFirstName() + " " + driver.getLastName());
                    response.setDriverPhone(driver.getPhoneNumber());
                }
            } catch (Exception e) {
                log.warn("Failed to fetch driver details: {}", e.getMessage());
            }
        }
        return response;
    }
}