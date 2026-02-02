package com.subscription.subscription.offer.controller;

import com.subscription.subscription.offer.dto.CreateOfferRequest;
import com.subscription.subscription.offer.dto.OfferResponse;
import com.subscription.subscription.offer.service.OfferService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * OfferController - Offer RESTful API Endpoints
 * 
 * Controller Nedir?
 * - Presentation layer (sunum katmanı)
 * - HTTP isteklerini karşılar
 * - Request'i işler ve Response döner
 * - Service layer'ı çağırır
 * 
 * @RestController Annotation:
 * - @Controller + @ResponseBody birleşimi
 * - Tüm methodlar otomatik olarak JSON döner
 * - RESTful web service'ler için kullanılır
 * 
 * @RequestMapping:
 * - Bu controller'ın base URL'ini belirler
 * - Örnek: /api/v1/offers
 * - Versioning için "v1" kullanıyoruz
 * 
 * RESTful API Best Practices:
 * - GET: Veri okuma
 * - POST: Yeni kayıt oluşturma
 * - PUT: Mevcut kaydı güncelleme
 * - DELETE: Kayıt silme
 * - HTTP status code'ları kullan (200, 201, 404, 500, vb.)
 */
@RestController
@RequestMapping("/api/v1/offers")
public class OfferController {

    /**
     * Service Dependency Injection
     * 
     * Constructor injection kullanıyoruz (best practice)
     */
    private final OfferService offerService;

    public OfferController(OfferService offerService) {
        this.offerService = offerService;
    }

    /**
     * Yeni Offer Oluşturma Endpoint
     * 
     * @PostMapping:
     * - HTTP POST metodunu dinler
     * - Base URL: POST /api/v1/offers
     * 
     * @Valid:
     * - Request body validasyonunu aktif eder
     * - CreateOfferRequest içindeki @NotBlank, @NotNull vb. kontrol edilir
     * - Validation hatası varsa MethodArgumentNotValidException fırlatılır
     * 
     * @RequestBody:
     * - HTTP request body'sini Java objesine dönüştürür (JSON -> Object)
     * - Jackson kütüphanesi otomatik deserialization yapar
     * 
     * ResponseEntity:
     * - HTTP response'u özelleştirmek için kullanılır
     * - Status code, header ve body içerir
     * - ResponseEntity.ok(): 200 OK döner
     * 
     * Örnek Request:
     * POST /api/v1/offers
     * Content-Type: application/json
     * {
     *   "name": "Premium Monthly",
     *   "description": "Premium features",
     *   "price": 99.99,
     *   "period": 1
     * }
     * 
     * Örnek Response:
     * HTTP 200 OK
     * {
     *   "id": 1,
     *   "name": "Premium Monthly",
     *   "description": "Premium features",
     *   "price": 99.99,
     *   "period": 1,
     *   "status": "ACTIVE",
     *   "createdAt": "2026-01-27T10:30:00Z",
     *   "updatedAt": "2026-01-27T10:30:00Z"
     * }
     */
    @PostMapping
    public ResponseEntity<OfferResponse> createOffer(@Valid @RequestBody CreateOfferRequest request) {
        return ResponseEntity.ok(offerService.createOffer(request));
    }

    /**
     * ID ile Offer Getirme Endpoint
     * 
     * @GetMapping("/{id}"):
     * - HTTP GET metodunu dinler
     * - URL: GET /api/v1/offers/{id}
     * - {id}: Path variable (dinamik parametre)
     * 
     * @PathVariable:
     * - URL'deki dinamik parametreyi method parametresine bağlar
     * - Örnek: /api/v1/offers/5 -> id = 5
     * 
     * Örnek: GET /api/v1/offers/1
     */
    @GetMapping("/{id}")
    public ResponseEntity<OfferResponse> getOffer(@PathVariable Long id) {
        return ResponseEntity.ok(offerService.getOfferById(id));
    }

    /**
     * Tüm Offer'ları Listeleme Endpoint
     * 
     * URL: GET /api/v1/offers
     * 
     * List<OfferResponse>:
     * - JSON array olarak serialize edilir
     * 
     * Örnek Response:
     * [
     *   { "id": 1, "name": "Premium", ... },
     *   { "id": 2, "name": "Basic", ... }
     * ]
     */
    @GetMapping
    public ResponseEntity<List<OfferResponse>> getAllOffers() {
        return ResponseEntity.ok(offerService.getAllOffers());
    }

    /**
     * Aktif Offer'ları Listeleme Endpoint
     * 
     * @GetMapping("/active"):
     * - Base URL'e "/active" eklenir
     * - URL: GET /api/v1/offers/active
     * 
     * Kullanım Senaryosu:
     * - Müşterilere gösterilecek paketler
     * - INACTIVE paketler filtrelenir
     */
    @GetMapping("/active")
    public ResponseEntity<List<OfferResponse>> getActiveOffers() {
        return ResponseEntity.ok(offerService.getActiveOffers());
    }

    /**
     * Offer Güncelleme Endpoint
     * 
     * @PutMapping("/{id}"):
     * - HTTP PUT metodunu dinler
     * - URL: PUT /api/v1/offers/{id}
     * - PUT: Kaynağın tamamını günceller (idempotent)
     * 
     * Idempotent Nedir?
     * - Aynı işlemi birden fazla kez yapmanın sonucu değişmez
     * - PUT idempotent'tir (aynı isteği 10 kez göndersen sonuç aynı)
     * - POST idempotent değildir (her seferinde yeni kayıt oluşur)
     * 
     * Örnek:
     * PUT /api/v1/offers/1
     * {
     *   "name": "Updated Name",
     *   "description": "Updated Description",
     *   "price": 149.99,
     *   "period": 1
     * }
     */
    @PutMapping("/{id}")
    public ResponseEntity<OfferResponse> updateOffer(
            @PathVariable Long id, 
            @Valid @RequestBody CreateOfferRequest request) {
        return ResponseEntity.ok(offerService.updateOffer(id, request));
    }

    /**
     * Offer Silme Endpoint
     * 
     * @DeleteMapping("/{id}"):
     * - HTTP DELETE metodunu dinler
     * - URL: DELETE /api/v1/offers/{id}
     * 
     * ResponseEntity<Void>:
     * - Response body yok
     * - Sadece status code döner
     * 
     * noContent():
     * - HTTP 204 No Content döner
     * - Başarılı silme işlemi için standard response
     * 
     * Örnek: DELETE /api/v1/offers/1
     * Response: HTTP 204 No Content
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOffer(@PathVariable Long id) {
        offerService.deleteOffer(id);
        return ResponseEntity.noContent().build();
    }
}
