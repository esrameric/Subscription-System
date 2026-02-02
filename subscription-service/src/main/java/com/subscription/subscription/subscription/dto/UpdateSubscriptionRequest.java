package com.subscription.subscription.subscription.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * UpdateSubscriptionRequest DTO
 * 
 * Abonelik durumunu güncellemek için kullanılır
 * 
 * Kullanım Senaryoları:
 * 1. İptal: ACTIVE -> DEACTIVE
 * 2. Askıya Alma: ACTIVE -> SUSPEND (ödeme problemi)
 * 3. Yeniden Aktifleştirme: SUSPEND -> ACTIVE
 * 4. Askıdan İptal: SUSPEND -> DEACTIVE
 * 
 * Not: Sadece status güncellemesi yapılır
 * - Offer değiştirmek isterse yeni subscription oluşturulur
 * - nextRenewalDate manuel güncellenemez (business logic ile yönetilir)
 * 
 * Pattern Özellikleri:
 * - Single Responsibility: Sadece status günceller
 * - Immutability: Kritik alanlar (offerId, customerId) değiştirilemez
 * 
 * Örnek Request:
 * PUT /api/v1/subscriptions/5
 * {
 *   "status": "DEACTIVE"
 * }
 */
@Getter
@Setter
public class UpdateSubscriptionRequest {

    /**
     * Yeni Status Değeri
     * 
     * Geçerli Değerler:
     * - ACTIVE: Aktif abonelik
     * - DEACTIVE: İptal edilmiş
     * - SUSPEND: Askıya alınmış
     * 
     * @NotBlank: Boş olamaz
     * 
     * Gelişmiş Versiyon:
     * - Enum kullanılabilir: SubscriptionStatus enum
     * - Custom validator yazılabilir: @ValidStatus
     * - Status transition rules uygulanabilir (state machine)
     * 
     * Örnek State Machine:
     * - ACTIVE -> DEACTIVE: İzin ver
     * - ACTIVE -> SUSPEND: İzin ver
     * - DEACTIVE -> ACTIVE: İzin verme (yeni subscription gerekli)
     * - SUSPEND -> ACTIVE: İzin ver
     * - SUSPEND -> DEACTIVE: İzin ver
     */
    @NotBlank(message = "Status boş olamaz")
    private String status;
}
