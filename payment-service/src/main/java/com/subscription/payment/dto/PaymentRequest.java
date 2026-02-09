package com.subscription.payment.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Payment Request DTO
 * 
 * Ödeme oluşturma isteği
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentRequest {

    @NotNull(message = "Subscription ID gereklidir")
    private Long subscriptionId;

    @NotNull(message = "Customer ID gereklidir")
    private Long customerId;

    @NotNull(message = "Tutar gereklidir")
    @DecimalMin(value = "0.01", message = "Tutar 0'dan büyük olmalıdır")
    private BigDecimal amount;

    @NotBlank(message = "Para birimi gereklidir")
    @Size(min = 3, max = 3, message = "Para birimi 3 karakter olmalıdır")
    private String currency;

    @NotBlank(message = "Ödeme yöntemi gereklidir")
    private String paymentMethod;

    private String description;
}
