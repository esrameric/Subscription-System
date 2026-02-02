package com.subscription.subscription.subscription.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import java.time.Instant;

/**
 * SubscriptionResponse DTO
 * 
 * Client'a döndürülen subscription bilgileri
 * 
 * Response DTO Özellikleri:
 * - Tüm bilgileri içerir (ID dahil)
 * - Validation gerektirmez (sadece okuma)
 * - Entity'den DTO'ya mapping yapılır
 * 
 * Client Tarafında Kullanım:
 * - Subscription listesi gösterme
 * - Subscription detayları
 * - Yenileme tarihini gösterme
 * - Status bilgisi
 * 
 * Gelişmiş Versiyon:
 * - Offer bilgilerini de içerebilir (nested object)
 * - Customer bilgilerini de içerebilir
 * - Örnek:
 *   {
 *     "id": 1,
 *     "customer": { "id": 123, "name": "John" },
 *     "offer": { "id": 5, "name": "Premium", "price": 99.99 },
 *     "nextRenewalDate": "2026-02-26T00:00:00Z",
 *     "status": "ACTIVE"
 *   }
 */
@Getter
@Setter
@AllArgsConstructor
public class SubscriptionResponse {

    /**
     * Subscription ID
     * 
     * Unique identifier
     * Client bu ID ile subscription referans eder
     */
    private Long id;

    /**
     * Müşteri ID
     * 
     * Hangi müşteriye ait olduğunu belirtir
     */
    private Long customerId;

    /**
     * Offer ID
     * 
     * Hangi pakete abone olduğunu belirtir
     * Client bu ID ile offer detaylarını ayrı endpoint'ten çekebilir
     */
    private Long offerId;

    /**
     * Bir Sonraki Yenileme Tarihi
     * 
     * ISO-8601 formatında: "2026-02-26T00:00:00Z"
     * 
     * Client tarafında kullanım:
     * - "Aboneliğiniz 26 Şubat 2026'da yenilenecektir"
     * - Countdown timer
     * - Yenileme hatırlatması
     */
    private Instant nextRenewalDate;

    /**
     * Abonelik Durumu
     * 
     * ACTIVE / DEACTIVE / SUSPEND
     * 
     * UI'da kullanım:
     * - Badge: "Aktif" (yeşil), "İptal Edildi" (kırmızı), "Askıda" (sarı)
     * - İptal butonu göster/gizle
     * - Yenileme seçenekleri
     */
    private String status;

    /**
     * Oluşturulma Zamanı
     * 
     * Aboneliğin başlangıç tarihi
     */
    private Instant createdAt;

    /**
     * Güncellenme Zamanı
     * 
     * Son değişiklik tarihi
     */
    private Instant updatedAt;
}
