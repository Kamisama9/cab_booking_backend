package com.cts.booking_service.service;

import com.cts.booking_service.dao.RiderBookingDao;
import com.cts.booking_service.dto.payment.CreatePaymentIntentResponse;
import com.cts.booking_service.dto.payment.PaymentDetailsResponse;
import com.cts.booking_service.dto.payment.UpdatePaymentRequest;
import com.cts.booking_service.entity.Booking;
import com.cts.booking_service.exception.BookingNotFoundException;
import com.cts.booking_service.exception.InvalidBookingStatusException;
import com.cts.booking_service.exception.InvalidRequestException;
import com.cts.booking_service.exception.UnauthorizedAccessException;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final RiderBookingDao riderBookingDao;

    @Value("${stripe.secret.key}")
    private String stripeSecretKey;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeSecretKey;
        log.info("✅ Stripe API initialized");
    }

    /**
     * Create Stripe PaymentIntent and return client secret
     */
    @Transactional
    public CreatePaymentIntentResponse createPaymentIntent(String bookingId, String userId) {
        log.info("🔑 Creating payment intent for booking: {} by user: {}", bookingId, userId);

        // Find booking
        Booking booking = riderBookingDao.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException(bookingId, true));

        // Verify user authorization
        if (!booking.getRiderId().equals(userId)) {
            log.error("❌ Unauthorized payment intent creation by user {} for booking {}", userId, bookingId);
            throw new UnauthorizedAccessException(userId, bookingId);
        }

        // Validate booking is completed
        if (booking.getBookingStatus() != Booking.BookingStatus.COMPLETED) {
            throw new InvalidBookingStatusException("Payment can only be made for completed bookings. Current status: " + booking.getBookingStatus());
        }

        // Check if already paid
        if (booking.getPaymentStatus() == Booking.PaymentStatus.COMPLETED) {
            throw new InvalidRequestException("Payment already completed for this booking");
        }

        try {
            // Create Stripe PaymentIntent
            long amountInPaise = (long) (booking.getFareAmount().doubleValue() * 100);
            
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(amountInPaise)
                    .setCurrency("inr")
                    .putMetadata("bookingId", bookingId)
                    .putMetadata("riderId", userId)
                    .putMetadata("driverId", booking.getDriverId() != null ? booking.getDriverId() : "N/A")
                    .setDescription("Payment for booking " + bookingId)
                    .build();

            PaymentIntent paymentIntent = PaymentIntent.create(params);

            log.info("✅ PaymentIntent created: {} for booking: {}", paymentIntent.getId(), bookingId);

            return CreatePaymentIntentResponse.builder()
                    .clientSecret(paymentIntent.getClientSecret())
                    .paymentIntentId(paymentIntent.getId())
                    .amount(booking.getFareAmount())
                    .currency("inr")
                    .bookingId(bookingId)
                    .build();

        } catch (StripeException e) {
            log.error("❌ Stripe error creating PaymentIntent for booking: {}", bookingId, e);
            throw new RuntimeException("Failed to create payment intent: " + e.getMessage());
        }
    }

    /**
     * Update payment details after successful Stripe payment
     */
    @Transactional
    public PaymentDetailsResponse updatePayment(UpdatePaymentRequest request, String userId) {
        log.info("💳 Updating payment for booking: {} by user: {}", request.getBookingId(), userId);

        // Find booking
        Booking booking = riderBookingDao.findById(request.getBookingId())
                .orElseThrow(() -> new BookingNotFoundException(request.getBookingId(), true));

        // Verify user authorization
        if (!booking.getRiderId().equals(userId)) {
            log.error("❌ Unauthorized payment update by user {} for booking {}", userId, request.getBookingId());
            throw new UnauthorizedAccessException(userId, request.getBookingId());
        }

        // Validate booking is completed
        if (booking.getBookingStatus() != Booking.BookingStatus.COMPLETED) {
            throw new InvalidBookingStatusException("Can only update payment for completed bookings");
        }

        // Update payment details
        booking.setPaymentId(request.getPaymentId());
        booking.setPaymentMethod(request.getPaymentMethod() != null ? request.getPaymentMethod() : "card");
        booking.setPaymentStatus(Booking.PaymentStatus.COMPLETED);
        booking.setPaidAt(LocalDateTime.now());

        riderBookingDao.save(booking);

        log.info("✅ Payment updated successfully for booking: {}", request.getBookingId());

        return PaymentDetailsResponse.builder()
                .bookingId(booking.getId())
                .paymentId(booking.getPaymentId())
                .amount(booking.getFareAmount())
                .paymentMethod(booking.getPaymentMethod())
                .paymentStatus(booking.getPaymentStatus().name())
                .paidAt(booking.getPaidAt())
                .message("Payment completed successfully")
                .build();
    }

    /**
     * Mark payment as failed
     */
    @Transactional
    public void markPaymentFailed(String bookingId, String userId, String reason) {
        log.warn("⚠️ Marking payment as failed for booking: {} by user: {}, reason: {}", bookingId, userId, reason);

        Booking booking = riderBookingDao.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException(bookingId, true));

        if (!booking.getRiderId().equals(userId)) {
            throw new UnauthorizedAccessException(userId, bookingId);
        }

        booking.setPaymentStatus(Booking.PaymentStatus.FAILED);
        riderBookingDao.save(booking);

        log.info("❌ Payment marked as failed for booking: {}", bookingId);
    }

    /**
     * Get payment details for a booking
     */
    @Transactional(readOnly = true)
    public PaymentDetailsResponse getPaymentDetails(String bookingId, String userId) {
        log.info("📋 Fetching payment details for booking: {}", bookingId);

        Booking booking = riderBookingDao.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException(bookingId, true));

        if (!booking.getRiderId().equals(userId)) {
            throw new UnauthorizedAccessException(userId, bookingId);
        }

        return PaymentDetailsResponse.builder()
                .bookingId(booking.getId())
                .paymentId(booking.getPaymentId())
                .amount(booking.getFareAmount())
                .paymentMethod(booking.getPaymentMethod())
                .paymentStatus(booking.getPaymentStatus().name())
                .paidAt(booking.getPaidAt())
                .build();
    }
}