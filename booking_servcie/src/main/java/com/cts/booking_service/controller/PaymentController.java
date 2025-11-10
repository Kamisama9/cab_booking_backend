package com.cts.booking_service.controller;

import com.cts.booking_service.dto.payment.CreatePaymentIntentRequest;
import com.cts.booking_service.dto.payment.CreatePaymentIntentResponse;
import com.cts.booking_service.dto.payment.PaymentDetailsResponse;
import com.cts.booking_service.dto.payment.UpdatePaymentRequest;
import com.cts.booking_service.exception.MissingHeaderException;
import com.cts.booking_service.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/create-intent")
    public ResponseEntity<CreatePaymentIntentResponse> createPaymentIntent(
            HttpServletRequest httpRequest,
            @Valid @RequestBody CreatePaymentIntentRequest request) {

        String userId = httpRequest.getHeader("X-User-Id");
        if (userId == null || userId.isBlank()) {
            log.error("❌ Missing X-User-Id header");
            throw new MissingHeaderException("X-User-Id");
        }

        log.info("🔑 User {} creating payment intent for booking {}", userId, request.getBookingId());
        CreatePaymentIntentResponse response = paymentService.createPaymentIntent(request.getBookingId(), userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<PaymentDetailsResponse> updatePayment(
            HttpServletRequest httpRequest,
            @Valid @RequestBody UpdatePaymentRequest request) {

        String userId = httpRequest.getHeader("X-User-Id");
        if (userId == null || userId.isBlank()) {
            log.error("❌ Missing X-User-Id header");
            throw new MissingHeaderException("X-User-Id");
        }

        log.info("💳 User {} updating payment for booking {}", userId, request.getBookingId());
        PaymentDetailsResponse response = paymentService.updatePayment(request, userId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/{bookingId}/failed")
    public ResponseEntity<Map<String, String>> markPaymentFailed(
            HttpServletRequest httpRequest,
            @PathVariable String bookingId,
            @RequestParam(required = false, defaultValue = "Unknown error") String reason) {

        String userId = httpRequest.getHeader("X-User-Id");
        if (userId == null || userId.isBlank()) {
            throw new MissingHeaderException("X-User-Id");
        }

        log.warn("⚠️ User {} marking payment as failed for booking {}", userId, bookingId);
        paymentService.markPaymentFailed(bookingId, userId, reason);
        
        return ResponseEntity.ok(Map.of(
                "message", "Payment marked as failed",
                "bookingId", bookingId
        ));
    }

    /**
     * ✅ Get payment details for a booking
     * GET /api/v1/payments/{bookingId}
     */
    @GetMapping("/{bookingId}")
    public ResponseEntity<PaymentDetailsResponse> getPaymentDetails(
            HttpServletRequest httpRequest,
            @PathVariable String bookingId) {

        String userId = httpRequest.getHeader("X-User-Id");
        if (userId == null || userId.isBlank()) {
            throw new MissingHeaderException("X-User-Id");
        }

        log.info("📋 User {} fetching payment details for booking {}", userId, bookingId);
        PaymentDetailsResponse response = paymentService.getPaymentDetails(bookingId, userId);
        return ResponseEntity.ok(response);
    }
}