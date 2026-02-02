package com.subscription.subscription.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Payment Response DTO
 * 
 * Payment Service'ten dönen ödeme cevabı
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {

    private Long id;
    private Long subscriptionId;
    private Long customerId;
    private BigDecimal amount;
    private String status;  // SUCCESS, FAILED, PENDING
    private String paymentMethod;
    private String transactionId;
    private String failureReason;
    private Instant createdAt;
}
