package com.subscription.subscription.subscription.controller;

import com.subscription.subscription.subscription.dto.CreateSubscriptionRequest;
import com.subscription.subscription.subscription.dto.UpdateSubscriptionRequest;
import com.subscription.subscription.subscription.dto.SubscriptionResponse;
import com.subscription.subscription.subscription.service.SubscriptionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * SubscriptionController - Subscription RESTful API Endpoints
 * 
 * @RestController:
 * - REST API controller (@Controller + @ResponseBody)
 * - Tüm methodlar JSON döner
 * - HTTP request/response yönetimi
 * 
 * @RequestMapping:
 * - Base URL: /api/v1/subscriptions
 * - Version kontrolü (v1, v2, ...)
 * 
 * Security:
 * - SecurityContextHolder: Spring Security'nin authentication bilgilerini tutar
 * - Authentication: Giriş yapmış kullanıcı bilgisi
 * - JWT token parse edildikten sonra SecurityContext'e set edilir
 * 
 * Flow:
 * 1. Client JWT token ile istek atar
 * 2. API Gateway veya Security Filter token'ı parse eder
 * 3. SecurityContext'e authentication bilgisi set edilir
 * 4. Controller'da SecurityContextHolder.getContext().getAuthentication() ile alınır
 * 5. Customer ID extract edilir ve business logic'e geçilir
 */
@RestController
@RequestMapping("/api/v1/subscriptions")
public class SubscriptionController {

    /**
     * Service Dependency Injection
     */
    private final SubscriptionService subscriptionService;

    public SubscriptionController(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    /**
     * Yeni Subscription Oluşturma Endpoint
     * 
     * POST /api/v1/subscriptions
     * 
     * Security Pattern:
     * - Customer ID request body'de GELMİYOR
     * - JWT token'dan alınıyor (güvenlik için)
     * - Kullanıcı sadece kendi adına abonelik oluşturabilir
     * 
     * Authentication.getName():
     * - JWT token'daki "sub" claim'i (subject)
     * - Genellikle email veya username
     * - Token örneği: { "sub": "user@example.com", "customerId": 123 }
     * 
     * hashEmailToId():
     * - Geçici çözüm (demo amaçlı)
     * - Production'da: customer-service'e REST call yapılır
     * - Email ile customer bilgisi çekilir
     * - Örnek: GET http://customer-service/api/v1/customers/by-email/{email}
     * 
     * @Valid:
     * - Request body validation
     * - CreateSubscriptionRequest içindeki @NotNull kontrol edilir
     * 
     * Örnek Request:
     * POST /api/v1/subscriptions
     * Authorization: Bearer eyJhbGc...
     * Content-Type: application/json
     * {
     *   "offerId": 1
     * }
     * 
     * Örnek Response:
     * HTTP 200 OK
     * {
     *   "id": 5,
     *   "customerId": 123,
     *   "offerId": 1,
     *   "nextRenewalDate": "2026-02-26T10:30:00Z",
     *   "status": "ACTIVE",
     *   "createdAt": "2026-01-27T10:30:00Z",
     *   "updatedAt": "2026-01-27T10:30:00Z"
     * }
     */
    @PostMapping
    public ResponseEntity<SubscriptionResponse> createSubscription(
            @Valid @RequestBody CreateSubscriptionRequest request) {
        
        // Spring Security Context'ten authentication bilgisini al
        // SecurityContextHolder: Thread-local storage (her thread için ayrı)
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        // Authentication.getName(): JWT token'daki "sub" claim (email veya username)
        String email = authentication.getName();
        
        // Customer ID'yi email'den türet (geçici çözüm)
        // Production'da: customer-service'den fetch edilmeli
        // RestTemplate veya WebClient ile HTTP call
        // Örnek: customerServiceClient.getCustomerByEmail(email).getId()
        Long customerId = hashEmailToId(email);
        
        // Abonelik oluştur
        return ResponseEntity.ok(subscriptionService.createSubscription(customerId, request));
    }

    /**
     * ID ile Subscription Getirme Endpoint
     * 
     * GET /api/v1/subscriptions/{id}
     * 
     * @PathVariable:
     * - URL'deki {id} parametresini method parametresine map eder
     * 
     * Security Not:
     * - İleride authorization kontrolü eklenebilir
     * - Kullanıcı sadece kendi subscription'ını görebilmeli
     * - Kontrol: subscription.customerId == authenticated.customerId
     * 
     * Örnek: GET /api/v1/subscriptions/5
     */
    @GetMapping("/{id}")
    public ResponseEntity<SubscriptionResponse> getSubscription(@PathVariable Long id) {
        return ResponseEntity.ok(subscriptionService.getSubscriptionById(id));
    }

    /**
     * Kendi Aboneliklerimi Listeleme Endpoint
     * 
     * GET /api/v1/subscriptions/customer/me
     * 
     * "me" Convention:
     * - REST API'larda yaygın kullanılan pattern
     * - "me" = authenticated user
     * - /api/v1/subscriptions/customer/{id} yerine
     * - /api/v1/subscriptions/customer/me kullanıyoruz
     * 
     * Avantajları:
     * + Client customer ID bilmek zorunda değil
     * + Güvenli: Kullanıcı sadece kendi subscription'larını görür
     * + Kolay kullanım
     * 
     * Kullanım Senaryosu:
     * - Dashboard'daki "Aboneliklerim" bölümü
     * - Subscription management sayfası
     * 
     * Örnek Response:
     * [
     *   {
     *     "id": 5,
     *     "customerId": 123,
     *     "offerId": 1,
     *     "nextRenewalDate": "2026-02-26T10:30:00Z",
     *     "status": "ACTIVE",
     *     ...
     *   },
     *   {
     *     "id": 8,
     *     "customerId": 123,
     *     "offerId": 3,
     *     "nextRenewalDate": "2026-03-15T08:00:00Z",
     *     "status": "ACTIVE",
     *     ...
     *   }
     * ]
     */
    @GetMapping("/customer/me")
    public ResponseEntity<List<SubscriptionResponse>> getMySubscriptions() {
        // Authentication bilgisini al
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        Long customerId = hashEmailToId(email);
        
        // Customer'ın tüm aktif aboneliklerini getir
        return ResponseEntity.ok(subscriptionService.getCustomerSubscriptions(customerId));
    }

    /**
     * Subscription Güncelleme Endpoint
     * 
     * PUT /api/v1/subscriptions/{id}
     * 
     * Kullanım Senaryoları:
     * 1. İptal: { "status": "DEACTIVE" }
     * 2. Askıya Alma: { "status": "SUSPEND" }
     * 3. Yeniden Aktifleştirme: { "status": "ACTIVE" }
     * 
     * PUT vs PATCH:
     * - PUT: Kaydın tamamını günceller (idempotent)
     * - PATCH: Sadece belirli alanları günceller
     * - Burada sadece status güncellendiği için PATCH daha uygun olabilir
     * 
     * Security Not:
     * - Authorization kontrolü eklenebilir
     * - Admin her subscription'ı güncelleyebilir
     * - Normal user sadece kendi subscription'ını güncelleyebilir
     * 
     * Örnek:
     * PUT /api/v1/subscriptions/5
     * {
     *   "status": "DEACTIVE"
     * }
     */
    @PutMapping("/{id}")
    public ResponseEntity<SubscriptionResponse> updateSubscription(
            @PathVariable Long id, 
            @Valid @RequestBody UpdateSubscriptionRequest request) {
        return ResponseEntity.ok(subscriptionService.updateSubscription(id, request));
    }

    /**
     * Subscription Yenileme Endpoint (Manuel Renewal)
     * 
     * POST /api/v1/subscriptions/{id}/renew
     * 
     * İşlem adımları:
     * 1. Subscription bilgilerini getir
     * 2. Payment Service'e ödeme isteği gönder
     * 3. Ödeme başarılıysa: subscription yenilenir (Kafka event ile)
     * 4. Ödeme başarısızsa: subscription SUSPEND durumuna geçer
     * 
     * Örnek:
     * POST /api/v1/subscriptions/5/renew
     * Authorization: Bearer eyJhbGc...
     * 
     * Response (Başarılı):
     * {
     *   "message": "Payment processed successfully",
     *   "paymentId": 123,
     *   "status": "SUCCESS"
     * }
     * 
     * Response (Başarısız):
     * {
     *   "message": "Payment failed",
     *   "status": "FAILED",
     *   "reason": "Insufficient funds"
     * }
     */
    @PostMapping("/{id}/renew")
    public ResponseEntity<?> renewSubscription(@PathVariable Long id) {
        return ResponseEntity.ok(subscriptionService.processSubscriptionRenewal(id));
    }

    /**
     * Email'den Customer ID Türetme (Geçici Çözüm)
     * 
     * Bu method demo amaçlıdır, production'da KULLANILMAMALI!
     * 
     * Production Yaklaşımı:
     * 
     * Option 1: JWT Token'da Customer ID Bulundurmak
     * - Token payload: { "sub": "user@example.com", "customerId": 123 }
     * - Authentication principal'dan customerId extract et
     * 
     * Option 2: Customer Service'den Fetch Etmek
     * - RestTemplate/WebClient ile HTTP call
     * - GET http://customer-service/api/v1/customers/by-email/{email}
     * - Response: { "id": 123, "email": "user@example.com", ... }
     * 
     * Option 3: Distributed Cache (Redis)
     * - Email -> Customer ID mapping'i cache'de tut
     * - Cache miss durumunda customer-service'den fetch et
     * 
     * Hash Kullanımının Problemi:
     * - Aynı email her zaman aynı ID'yi üretir (deterministic)
     * - Ama gerçek customer ID ile eşleşme garantisi yok
     * - Collision riski (farklı email'ler aynı hash'i üretebilir)
     * 
     * @param email User email
     * @return Derived customer ID (geçici)
     */
    private Long hashEmailToId(String email) {
        // String.hashCode(): String'i integer'a dönüştürür
        // Math.abs(): Negatif değerleri pozitif yapar
        // (long) cast: int'i long'a dönüştürür
        return Math.abs((long) email.hashCode());
    }
}
