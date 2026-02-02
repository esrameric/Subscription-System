package com.subscription.subscription.offer.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * OfferResponse DTO
 * 
 * Bu sınıf, client'a dönen offer verilerini temsil eder.
 * 
 * Response DTO Özellikleri:
 * - Tüm alanları içerebilir (ID dahil)
 * - Read-only veriler için kullanılır
 * - Entity'den DTO'ya mapping yapılır
 * - JSON formatında serialize edilir
 * 
 * Request DTO ile Farkı:
 * - Request: Client'tan gelen veriler (ID yok, validation var)
 * - Response: Client'a giden veriler (ID var, validation gereksiz)
 * 
 * @AllArgsConstructor: Tüm fieldları içeren constructor oluşturur
 * - Mapping işlemini kolaylaştırır
 * - Örnek: new OfferResponse(id, name, description, ...)
 */
@Getter
@Setter
@AllArgsConstructor
public class OfferResponse {

    /**
     * Offer ID
     * 
     * Response'da ID her zaman döndürülür
     * Client bu ID ile offer'ı referans edebilir
     */
    private Long id;

    /**
     * Offer Adı
     */
    private String name;

    /**
     * Offer Açıklaması
     */
    private String description;

    /**
     * Fiyat Bilgisi
     */
    private BigDecimal price;

    /**
     * Periyod Uzunluğu (Ay)
     */
    private Integer period;

    /**
     * Durum (ACTIVE/INACTIVE)
     */
    private String status;

    /**
     * Oluşturulma Zamanı
     * 
     * ISO-8601 formatında JSON'a serialize edilir
     * Örnek: "2026-01-27T10:30:00Z"
     */
    private Instant createdAt;

    /**
     * Güncellenme Zamanı
     */
    private Instant updatedAt;
}
