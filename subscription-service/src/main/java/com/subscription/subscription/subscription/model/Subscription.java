package com.subscription.subscription.subscription.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

/**
 * Subscription Entity - Müşteri Abonelik Kayıtları
 * 
 * Bu entity, müşterilerin satın aldığı abonelikleri temsil eder.
 * Her abonelik bir müşteriye ve bir offer'a bağlıdır.
 * 
 * İlişkiler:
 * - Subscription -> Customer (Many-to-One): Bir müşterinin birden fazla aboneliği olabilir
 * - Subscription -> Offer (Many-to-One): Bir offer'ın birden fazla aboneliği olabilir
 * 
 * Not: Bu versiyonda Customer ve Offer için sadece ID saklıyoruz (foreign key)
 * İleride @ManyToOne ilişkisi eklenebilir
 * 
 * Abonelik Yaşam Döngüsü:
 * 1. ACTIVE: Müşteri aktif olarak hizmetten yararlanıyor
 * 2. DEACTIVE: Müşteri aboneliği iptal etti (yenilenmeyecek)
 * 3. SUSPEND: Geçici olarak askıya alındı (ödeme sorunu, vb.)
 * 
 * Terimler:
 * @Entity: JPA entity class (veritabanı tablosuna karşılık gelir)
 * @Table: Tablo adını belirtir
 * @Id: Primary key
 * @GeneratedValue: Primary key otomatik oluşturulur
 * @Column: Kolon özellikleri
 */
@Entity
@Table(name = "subscriptions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Subscription {

    /**
     * Primary Key - Subscription ID
     * 
     * IDENTITY strategy: Database'in auto-increment özelliğini kullanır
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Müşteri ID
     * 
     * Foreign Key: customer-service'teki customer tablosuna referans
     * 
     * Mikroservis Mimarisinde Foreign Key:
     * - Doğrudan @ManyToOne ilişkisi kurmuyoruz (loosely coupled)
     * - Sadece ID saklıyoruz
     * - İhtiyaç halinde customer-service'e REST call yaparak müşteri bilgisi alınır
     * 
     * Bu yaklaşım:
     * + Servisler arası bağımlılık azalır
     * + Her servis kendi database'ini yönetir
     * - Join sorguları yapılamaz (distributed transactions)
     */
    @Column(nullable = false)
    private Long customerId;

    /**
     * Offer ID
     * 
     * Foreign Key: offers tablosuna referans
     * Hangi paketi satın aldığını belirtir
     */
    @Column(nullable = false)
    private Long offerId;

    /**
     * Bir Sonraki Yenileme Tarihi
     * 
     * Business Logic:
     * - Abonelik oluşturulduğunda: createdAt + offer.period ay
     * - Her yenileme sonrası: currentDate + offer.period ay
     * - Bu tarih geldiğinde SubscriptionRenewalJob çalışır
     * 
     * Instant:
     * - UTC timezone'da timestamp
     * - Timezone bağımsız (önemli: global uygulama için)
     * - ISO-8601 formatında: 2026-01-27T10:30:00Z
     * 
     * Örnek:
     * Bugün: 2026-01-27
     * Offer period: 1 ay
     * nextRenewalDate: 2026-02-27
     */
    @Column(nullable = false)
    private Instant nextRenewalDate;

    /**
     * Abonelik Durumu
     * 
     * Olası Değerler:
     * - ACTIVE: Aktif abonelik, hizmet kullanımda
     * - DEACTIVE: İptal edilmiş, yenilenmeyecek
     * - SUSPEND: Askıya alınmış (ödeme problemi, kullanıcı talebi, vb.)
     * 
     * Status Pattern:
     * - String yerine Enum kullanılabilir (type-safety için)
     * - Örnek: enum SubscriptionStatus { ACTIVE, DEACTIVE, SUSPEND }
     * 
     * Default: ACTIVE
     */
    @Column(nullable = false)
    private String status = "ACTIVE";

    /**
     * Oluşturulma Zamanı
     * 
     * Audit field: Ne zaman abonelik başladı
     * İlk kayıt anında set edilir, sonrası değişmez
     */
    @CreatedDate
    private Instant createdAt = Instant.now();

    /**
     * Güncellenme Zamanı
     * 
     * Audit field: Son güncelleme zamanı
     * Her update işleminde güncellenmeli
     * - Status değişikliği
     * - Yenileme işlemi
     * - vb.
     */
    @LastModifiedDate
    private Instant updatedAt = Instant.now();
}
