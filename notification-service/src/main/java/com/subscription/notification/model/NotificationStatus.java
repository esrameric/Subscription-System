package com.subscription.notification.model;

/**
 * Bildirim Durumları
 * 
 * Bildirimin gönderim sürecindeki durumunu takip eder:
 * 
 * - PENDING: Oluşturuldu, henüz gönderilmedi (sırada bekliyor)
 * - SENT: Başarıyla gönderildi
 * - FAILED: Gönderim başarısız oldu (hata mesajı kaydedilir)
 * - RETRYING: Tekrar deneniyor (ilk denemede başarısız olduysa)
 * 
 * Bu statüler sayesinde:
 * - Başarısız bildirimleri tekrar deneyebiliriz
 * - Bildirim geçmişini takip edebiliriz
 * - Raporlama yapabiliriz (başarı oranı vb.)
 */
public enum NotificationStatus {
    PENDING,   // Beklemede
    SENT,      // Gönderildi
    FAILED,    // Başarısız
    RETRYING   // Tekrar deneniyor
}
