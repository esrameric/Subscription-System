package com.subscription.notification.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * Payment Event DTO
 * 
 * Kafka üzerinden gelen ödeme olayı verilerini taşır
 * Payment Service'den Notification Service'e iletilir
 * 
 * Bu DTO ile:
 * - Ödeme bilgileri notification service'e ulaşır
 * - Müşteriye uygun bildirim şablonu hazırlanır
 * - Email/SMS/Push bildirimi gönderilir
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentEvent {

    private Long paymentId;           // Ödeme ID (unique)
    private Long subscriptionId;      // Hangi abonelik için ödeme yapıldı
    private Long customerId;          // Hangi müşteri ödeme yaptı
    private BigDecimal amount;        // Ödeme tutarı
    private String currency;          // Para birimi (USD, EUR, TRY vb.)
    private String status;            // Ödeme durumu: SUCCESS, FAILED, PENDING
    private String paymentMethod;     // Ödeme yöntemi (CREDIT_CARD, PAYPAL vb.)
    private String errorMessage;      // Hata mesajı (varsa)
    private Instant eventTime;        // Event oluşturulma zamanı
}
