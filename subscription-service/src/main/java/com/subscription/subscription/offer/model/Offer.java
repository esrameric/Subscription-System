package com.subscription.subscription.offer.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * Offer Entity - Abonelik Paket Tanımları
 * 
 * Bu entity, sistemdeki tüm abonelik paketlerini (offer) temsil eder.
 * Örnek: "Premium Aylık Paket", "Basic Yıllık Paket"
 * 
 * JPA Annotations:
 * @Entity: Bu sınıfın bir veritabanı tablosuna karşılık geldiğini belirtir
 * @Table: Tablo adını ve özelliklerini tanımlar
 * @Id: Primary key alanını belirtir
 * @GeneratedValue: Primary key'in otomatik oluşturulma stratejisini tanımlar
 *   - IDENTITY: Veritabanının auto-increment özelliğini kullanır
 * @Column: Kolon özelliklerini tanımlar (nullable, unique, length, vb.)
 * 
 * Lombok Annotations:
 * @Getter/@Setter: Getter ve setter methodlarını otomatik oluşturur
 * @NoArgsConstructor: Parametresiz constructor oluşturur (JPA için gerekli)
 * @AllArgsConstructor: Tüm fieldları içeren constructor oluşturur
 * @Builder: Builder pattern implementasyonu (örn: Offer.builder().name("Premium").build())
 */
@Entity
@Table(name = "offers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Offer {

    /**
     * Primary Key - Unique identifier
     * 
     * IDENTITY strategy:
     * - Her yeni kayıtta veritabanı otomatik olarak ID üretir
     * - PostgreSQL'de SERIAL tipini kullanır
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Offer Adı
     * 
     * nullable = false: Bu alan zorunludur, NULL olamaz
     * Örnek: "Premium Monthly", "Basic Annual"
     */
    @Column(nullable = false)
    private String name;

    /**
     * Paket Açıklaması
     * 
     * Paketin içeriğini açıklar
     * Örnek: "Sınırsız kullanım, premium özellikler"
     */
    @Column(nullable = false)
    private String description;

    /**
     * Fiyat Bilgisi
     * 
     * BigDecimal: Para birimi için önerilen tip
     * - Float/Double hassasiyet kayıplarına neden olur
     * - BigDecimal matematiksel hesaplamalar için daha güvenli
     * Örnek: 99.99, 499.00
     */
    @Column(nullable = false)
    private BigDecimal price;

    /**
     * Periyod Uzunluğu (Ay Cinsinden)
     * 
     * Aboneliğin kaç ay geçerli olacağını belirtir
     * Örnek:
     * - 1 = Aylık paket
     * - 12 = Yıllık paket
     * - 3 = Üç aylık paket
     */
    @Column(nullable = false)
    private Integer period;

    /**
     * Offer Durumu
     * 
     * ACTIVE: Müşteriler bu paketi seçebilir
     * INACTIVE: Paket artık sunulmuyor (mevcut abonelikler etkilenmez)
     * 
     * Default değer: "ACTIVE"
     */
    @Column(nullable = false)
    private String status = "ACTIVE";

    /**
     * Oluşturulma Zamanı
     * 
     * Instant: UTC timezone'da zaman damgası
     * - Timezone bağımsız
     * - ISO-8601 formatında
     * Instant.now(): O anki zamanı verir
     */
    private Instant createdAt = Instant.now();

    /**
     * Son Güncellenme Zamanı
     * 
     * Her update işleminde bu alan güncellenmelidir
     */
    private Instant updatedAt = Instant.now();
}
