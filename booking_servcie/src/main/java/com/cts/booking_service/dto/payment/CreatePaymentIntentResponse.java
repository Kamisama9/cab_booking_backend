package com.cts.booking_service.dto.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePaymentIntentResponse {
    
    private String clientSecret;
    private String paymentIntentId;
    private BigDecimal amount;
    private String currency;
    private String bookingId;
}