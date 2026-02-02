package com.subscription.subscription.subscription.service;

import com.subscription.subscription.subscription.dto.CreateSubscriptionRequest;
import com.subscription.subscription.subscription.dto.UpdateSubscriptionRequest;
import com.subscription.subscription.subscription.dto.SubscriptionResponse;
import java.util.List;

/**
 * SubscriptionService Interface - Subscription Business Logic Contract
 * 
 * Service Layer:
 * - Business logic (iş mantığı) katmanı
 * - Transaction management
 * - Orchestration (birden fazla repository/service koordinasyonu)
 * - Validation (complex business rules)
 * 
 * Interface Pattern:
 * - Abstraction sağlar
 * - Testing kolaylığı (mock/stub)
 * - Multiple implementation imkanı
 * - Loose coupling (gevşek bağlılık)
 * 
 * Subscription Yaşam Döngüsü:
 * 1. Create: Yeni abonelik oluştur
 * 2. Active: Hizmet kullanımda
 * 3. Update: Status değiştir (iptal, askıya al)
 * 4. Renew: Otomatik yenileme (scheduled job)
 */
public interface SubscriptionService {
    
    /**
     * Yeni Abonelik Oluşturma
     * 
     * Business Logic:
     * 1. Offer'ın varlığını kontrol et
     * 2. Duplicate subscription kontrolü (aynı customer + offer)
     * 3. nextRenewalDate hesapla (now + offer.period ay)
     * 4. Subscription oluştur ve kaydet
     * 5. Response döndür
     * 
     * İleride Eklenebilecekler:
     * - Payment entegrasyonu (önce ödeme, sonra abonelik)
     * - Email notification (hoş geldiniz maili)
     * - Event publishing (Kafka/RabbitMQ ile diğer servislere bildir)
     * - Promo code / discount uygulaması
     * 
     * @param customerId JWT token'dan çıkarılan customer ID
     * @param request Offer ID içeren request
     * @return Oluşturulan subscription bilgileri
     * @throws IllegalArgumentException Offer bulunamazsa veya duplicate subscription
     */
    SubscriptionResponse createSubscription(Long customerId, CreateSubscriptionRequest request);
    
    /**
     * ID ile Subscription Getirme
     * 
     * @param id Subscription ID
     * @return Subscription bilgileri
     * @throws IllegalArgumentException Subscription bulunamazsa
     */
    SubscriptionResponse getSubscriptionById(Long id);
    
    /**
     * Müşterinin Aboneliklerini Listeleme
     * 
     * Sadece aktif abonelikleri döndürür
     * 
     * Kullanım Senaryosu:
     * - "Aboneliklerim" sayfası
     * - Dashboard
     * - Subscription management panel
     * 
     * @param customerId Müşteri ID
     * @return Müşterinin aktif subscription listesi
     */
    List<SubscriptionResponse> getCustomerSubscriptions(Long customerId);
    
    /**
     * Subscription Status Güncelleme
     * 
     * Business Logic:
     * 1. Subscription'ı bul
     * 2. Status'u güncelle (ACTIVE/DEACTIVE/SUSPEND)
     * 3. updatedAt timestamp'ini güncelle
     * 4. Kaydet ve response döndür
     * 
     * İleride Eklenebilecekler:
     * - State transition validation (örn: DEACTIVE'den ACTIVE'e geçiş yasak)
     * - Audit log (kim, ne zaman, ne değiştirdi)
     * - Event publishing (status değişikliği bildirimi)
     * - Refund logic (iptal edilirse para iadesi)
     * 
     * @param id Subscription ID
     * @param request Yeni status içeren request
     * @return Güncellenmiş subscription bilgileri
     * @throws IllegalArgumentException Subscription bulunamazsa
     */
    SubscriptionResponse updateSubscription(Long id, UpdateSubscriptionRequest request);
    
    /**
     * Subscription Yenileme (Renewal)
     * 
     * Business Logic:
     * 1. Subscription'ı bul
     * 2. Offer'ı bul (period için)
     * 3. nextRenewalDate'i güncelle (now + period ay)
     * 4. updatedAt'i güncelle
     * 5. Kaydet
     * 
     * Bu method manuel de çağrılabilir, otomatik de (scheduled job)
     * 
     * İleride Eklenebilecekler:
     * - Payment processing (yenileme için ödeme al)
     * - Retry mechanism (ödeme başarısız olursa)
     * - Grace period (ödemesiz kullanım süresi)
     * - Email notification (yenileme başarılı/başarısız)
     * - Kafka event (payment-service'e yenileme bildirimi)
     * 
     * @param id Subscription ID
     * @throws IllegalArgumentException Subscription veya Offer bulunamazsa
     */
    void renewSubscription(Long id);

    /**
     * Subscription'ı Askıya Alma (Suspend)
     * 
     * Ödeme başarısız olduğunda kullanılır
     * 
     * @param id Subscription ID
     * @throws IllegalArgumentException Subscription bulunamazsa
     */
    void suspendSubscription(Long id);
    
    /**
     * Subscription Renewal Process - Payment ile birlikte
     * 
     * İşlem Adımları:
     * 1. Subscription bilgilerini getir
     * 2. Payment Service'e ödeme isteği gönder (Feign Client)
     * 3. Ödeme başarılıysa: Kafka event ile subscription otomatik yenilenir
     * 4. Ödeme başarısızsa: Subscription SUSPEND durumuna geçer
     * 
     * @param id Subscription ID
     * @return Ödeme sonucu bilgisi
     */
    Object processSubscriptionRenewal(Long id);
    
    /**
     * Yenilenmesi Gereken Abonelikleri Getirme
     * 
     * SubscriptionRenewalJob tarafından kullanılır
     * 
     * Kriterleri:
     * - nextRenewalDate < now (yenileme tarihi geçmiş)
     * - status = ACTIVE (sadece aktif abonelikler)
     * 
     * Scheduled Job Flow:
     * 1. Job her gün 00:00'da çalışır
     * 2. Bu method ile yenilenmesi gereken abonelikleri getir
     * 3. Her subscription için renewSubscription(id) çağır
     * 4. Log tut (başarılı/başarısız yenilemeler)
     * 
     * @return Yenilenmesi gereken subscription listesi
     */
    List<SubscriptionResponse> getSubscriptionsToRenew();
}
