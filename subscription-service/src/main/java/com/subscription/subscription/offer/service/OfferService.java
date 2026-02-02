package com.subscription.subscription.offer.service;

import com.subscription.subscription.offer.dto.CreateOfferRequest;
import com.subscription.subscription.offer.dto.OfferResponse;
import java.util.List;

/**
 * OfferService Interface - Offer Business Logic Contract
 * 
 * Service Layer Nedir?
 * - İş mantığının (business logic) bulunduğu katman
 * - Controller ile Repository arasında köprü
 * - Transaction yönetimi burada yapılır
 * - Karmaşık operasyonları koordine eder
 * 
 * Interface Kullanım Nedenleri:
 * 1. Abstraction: Implementation detaylarını gizler
 * 2. Testing: Mock implementasyon oluşturmayı kolaylaştırır
 * 3. Loose Coupling: Bağımlılıkları azaltır
 * 4. Multiple Implementation: Farklı implementasyonlar olabilir
 * 
 * Katman Mimarisi:
 * Controller -> Service (Interface) -> Service (Implementation) -> Repository -> Database
 * 
 * Bu interface, offer ile ilgili tüm business operasyonları tanımlar.
 */
public interface OfferService {
    
    /**
     * Yeni bir offer oluşturur
     * 
     * İş Mantığı:
     * 1. Request validasyonu (controller'da @Valid ile yapılır)
     * 2. Entity oluşturma
     * 3. Database'e kaydetme
     * 4. Response DTO'ya dönüştürme
     * 
     * @param request Yeni offer bilgileri
     * @return Oluşturulan offer'ın bilgileri
     */
    OfferResponse createOffer(CreateOfferRequest request);
    
    /**
     * ID ile offer getirir
     * 
     * @param id Offer ID
     * @return Offer bilgileri
     * @throws IllegalArgumentException Offer bulunamazsa
     */
    OfferResponse getOfferById(Long id);
    
    /**
     * Tüm offer'ları listeler
     * 
     * @return Tüm offer listesi (ACTIVE ve INACTIVE)
     */
    List<OfferResponse> getAllOffers();
    
    /**
     * Sadece aktif offer'ları listeler
     * 
     * Kullanım Senaryosu:
     * - Müşterilere gösterilecek paketler
     * - Yeni abonelik oluşturma sayfası
     * 
     * @return Aktif offer listesi
     */
    List<OfferResponse> getActiveOffers();
    
    /**
     * Mevcut bir offer'ı günceller
     * 
     * @param id Güncellenecek offer ID
     * @param request Yeni offer bilgileri
     * @return Güncellenmiş offer bilgileri
     * @throws IllegalArgumentException Offer bulunamazsa
     */
    OfferResponse updateOffer(Long id, CreateOfferRequest request);
    
    /**
     * Bir offer'ı siler
     * 
     * Not: Dikkatli kullanılmalı!
     * - Aktif abonelikleri olan offer'lar silinmemeli
     * - Alternatif: status'u INACTIVE yaparak pasifleştir
     * 
     * @param id Silinecek offer ID
     * @throws IllegalArgumentException Offer bulunamazsa
     */
    void deleteOffer(Long id);
}
