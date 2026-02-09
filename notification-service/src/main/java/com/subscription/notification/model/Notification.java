package com.subscription.notification.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

/**
 * Notification Entity
 * 
 * Gönderilen tüm bildirimlerin kayıtlarını tutar:
 * - Bildirim detayları (konu, içerik, alıcı)
 * - Gönderim durumu ve zamanı
 * - Hata bilgileri (varsa)
 * - İlişkili entity bilgisi (payment, subscription vb.)
 * 
 * Veritabanı tablosu: notifications
 */
@Entity
@Table(name = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;  // Benzersiz bildirim ID

    @Column(nullable = false)
    private Long customerId;  // Hangi müşteriye gönderildi

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private NotificationType type;  // PAYMENT_SUCCESS, PAYMENT_FAILED vb.

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private NotificationChannel channel;  // EMAIL, SMS, PUSH

    @Column(nullable = false)
    private String recipient;  // Email adresi veya telefon numarası

    @Column(nullable = false)
    private String subject;  // Bildirim konusu (email subject)

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;  // Bildirim içeriği (email body)

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private NotificationStatus status;  // PENDING, SENT, FAILED, RETRYING

    private String errorMessage;  // Hata durumunda hata mesajı

    private Long relatedEntityId;  // İlgili entity ID (Payment ID, Subscription ID vb.)

    @Column(nullable = false)
    private Instant createdAt;  // Oluşturulma zamanı

    private Instant sentAt;  // Gönderilme zamanı (başarılı ise)  // Gönderilme zamanı (başarılı ise)

    private Integer retryCount;  // Kaç kez denendi

    /**
     * Entity ilk kez persist edilmeden önce çağrılır
     * 
     * Otomatik olarak:
     * - Oluşturulma zamanını ayarlar
     * - Retry count'u başlatır (0)
     */
    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        if (retryCount == null) {
            retryCount = 0;
        }
    }
}
