package com.subscription.payment.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Payment Event DTO
 * 
 * Kafka üzerinden gönderilecek ödeme olayı
 * Subscription service bu eventi dinleyecek
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentEvent {

    private Long paymentId;
    private Long subscriptionId;
    private Long customerId;
    private BigDecimal amount;
    private String currency;
    private String status; // SUCCESS, FAILED, PENDING
    private String paymentMethod;
    private String errorMessage;
    private Instant eventTime;
}
