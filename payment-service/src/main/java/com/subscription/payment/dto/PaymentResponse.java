package com.subscription.payment.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Payment Response DTO
 * 
 * Ödeme bilgisi yanıtı
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResponse {

    private Long paymentId;
    private Long subscriptionId;
    private Long customerId;
    private BigDecimal amount;
    private String currency;
    private String status;
    private String paymentMethod;
    private String providerTransactionId;
    private String description;
    private String errorMessage;
    private Instant createdAt;
    private Instant updatedAt;
}
