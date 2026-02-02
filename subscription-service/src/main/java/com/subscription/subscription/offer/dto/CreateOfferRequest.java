package com.subscription.subscription.offer.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

/**
 * CreateOfferRequest DTO (Data Transfer Object)
 * 
 * Bu sınıf, yeni bir offer oluşturmak için gelen request verilerini temsil eder.
 * 
 * DTO Nedir?
 * - Katmanlar arası veri taşıma nesnesi
 * - Entity'den farklıdır (Entity database'e karşılık gelir, DTO sadece veri taşır)
 * - Client'tan gelen veya client'a giden verileri temsil eder
 * 
 * DTO Kullanım Nedenleri:
 * 1. Entity'nin tüm alanlarını expose etmemek (güvenlik)
 * 2. Validation kurallarını merkezi yönetmek
 * 3. API contract'ını entity'den ayırmak (entity değişse API etkilenmez)
 * 
 * Validation Annotations (Jakarta Bean Validation):
 * @NotNull: Alan null olamaz
 * @NotBlank: String alanlar null, boş string veya sadece whitespace olamaz
 * @DecimalMin: Minimum değer kontrolü
 * @Positive: Pozitif sayı kontrolü
 * 
 * Bu validationlar @Valid annotation'ı ile controller'da otomatik çalışır
 */
@Getter
@Setter
public class CreateOfferRequest {

    /**
     * Offer Adı
     * 
     * @NotBlank: Boş olamaz, en az bir karakter içermeli
     * Örnek: "Premium Monthly Package"
     */
    @NotBlank(message = "Offer adı boş olamaz")
    private String name;

    /**
     * Offer Açıklaması
     * 
     * Paketin detaylı açıklaması
     */
    @NotBlank(message = "Açıklama boş olamaz")
    private String description;

    /**
     * Fiyat
     * 
     * @NotNull: Null olamaz (BigDecimal için)
     * @DecimalMin: Minimum değer kontrolü (0.01'den küçük olamaz)
     * Örnek: 99.99
     */
    @NotNull(message = "Fiyat belirtilmelidir")
    @DecimalMin(value = "0.01", message = "Fiyat 0.01'den büyük olmalıdır")
    private BigDecimal price;

    /**
     * Periyod Uzunluğu (Ay)
     * 
     * @NotNull: Null olamaz
     * @Positive: Pozitif bir sayı olmalı (0'dan büyük)
     * Örnek: 1 (aylık), 12 (yıllık), 3 (üç aylık)
     */
    @NotNull(message = "Periyod uzunluğu belirtilmelidir")
    @Positive(message = "Periyod uzunluğu pozitif bir sayı olmalıdır")
    private Integer period;
}
