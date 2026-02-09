package com.subscription.subscription.offer.service.impl;

import com.subscription.subscription.offer.dto.CreateOfferRequest;
import com.subscription.subscription.offer.dto.OfferResponse;
import com.subscription.subscription.offer.model.Offer;
import com.subscription.subscription.offer.repository.OfferRepository;
import com.subscription.subscription.offer.service.OfferService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * OfferServiceImpl - OfferService Interface'inin Implementasyonu
 * 
 * @Service Annotation:
 * - Bu sınıfın bir Spring Service Bean olduğunu belirtir
 * - @Component'in özelleşmiş halidir
 * - Component scanning ile otomatik bulunur
 * - Business logic katmanı için kullanılır
 * 
 * Dependency Injection:
 * - Constructor injection kullanıyoruz (best practice)
 * - Field injection (@Autowired private OfferRepository...) yerine tercih edilir
 * - Test edilebilirliği artırır
 * - Immutability sağlar (final ile)
 * 
 * Transaction Management:
 * - @Transactional kullanılabilir (gerektiğinde)
 * - Spring otomatik transaction yönetimi yapar
 * - Rollback desteği sağlar
 * 
 * Cache Stratejisi:
 * - getAllOfferIds(): Redis'te cache'lenir (@Cacheable)
 * - create/update/delete: Cache invalidation (@CacheEvict)
 */
@Service
@Slf4j
public class OfferServiceImpl implements OfferService {

    /**
     * Repository Dependency
     * 
     * final: Immutable, bir kez atandıktan sonra değiştirilemez
     * Constructor injection ile Spring tarafından otomatik enjekte edilir
     */
    private final OfferRepository offerRepository;

    /**
     * Constructor - Dependency Injection
     * 
     * Spring Boot 4.3+ ile @Autowired annotation'ına gerek yok
     * Spring otomatik olarak bu constructor'ı bulur ve dependency'leri enjekte eder
     * 
     * @param offerRepository Offer database işlemleri için
     */
    public OfferServiceImpl(OfferRepository offerRepository) {
        this.offerRepository = offerRepository;
    }

    /**
     * Yeni Offer Oluşturma
     * 
     * İşlem Adımları:
     * 1. Request DTO'dan Entity oluşturma (Builder pattern ile)
     * 2. Default değerler atama (status, timestamps)
     * 3. Database'e kaydetme
     * 4. Entity'yi Response DTO'ya dönüştürme
     * 
     * Builder Pattern:
     * - Lombok @Builder annotation'ı ile otomatik oluşturulur
     * - Okunabilir ve zincirleme syntax
     * - Örnek: Offer.builder().name("Premium").price(99.99).build()
     * 
     * Cache:
     * - Yeni offer oluşturulduğunda offerIds cache'i temizlenir
     */
    @Override
    @CacheEvict(cacheNames = "offerIds", allEntries = true)
    public OfferResponse createOffer(CreateOfferRequest request) {
        log.info("Creating new offer, evicting offerIds cache");
        // Entity oluştur
        Offer offer = Offer.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .period(request.getPeriod())
                .status("ACTIVE")  // Default olarak aktif
                .createdAt(Instant.now())  // Oluşturulma zamanı
                .updatedAt(Instant.now())  // İlk güncellenme zamanı
                .build();

        // Database'e kaydet
        // save() methodu: Yeni kayıt ise INSERT, mevcut ise UPDATE yapar
        Offer savedOffer = offerRepository.save(offer);
        
        // Entity'yi Response DTO'ya dönüştür ve döndür
        return mapToResponse(savedOffer);
    }

    /**
     * ID ile Offer Getirme
     * 
     * Optional Pattern:
     * - findById() Optional<Offer> döndürür
     * - Optional: Değer olabilir veya olmayabilir (null safety)
     * - orElseThrow(): Değer yoksa exception fırlat
     */
    @Override
    public OfferResponse getOfferById(Long id) {
        Offer offer = offerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Offer not found: " + id));
        return mapToResponse(offer);
    }

    /**
     * Tüm Offer'ları Listeleme
     * 
     * Stream API Kullanımı:
     * - Java 8+ Stream API ile functional programming
     * - map(): Her elemanı dönüştürür (Entity -> DTO)
     * - collect(): Stream'i Collection'a dönüştürür
     * 
     * Method Reference:
     * - this::mapToResponse = lambda: offer -> mapToResponse(offer)
     */
    @Override
    public List<OfferResponse> getAllOffers() {
        return offerRepository.findAll()
                .stream()
                .map(this::mapToResponse)  // Her Offer'ı OfferResponse'a dönüştür
                .collect(Collectors.toList());  // List'e topla
    }

    /**
     * Aktif Offer'ları Listeleme
     * 
     * Custom Repository Method kullanımı
     */
    @Override
    public List<OfferResponse> getActiveOffers() {
        return offerRepository.findByStatus("ACTIVE")
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Offer Güncelleme
     * 
     * Update İşlem Adımları:
     * 1. Mevcut offer'ı database'den getir
     * 2. Alanları güncelle
     * 3. updatedAt timestamp'ini güncelle
     * 4. Database'e kaydet
     * 
     * JPA'nın save() methodu:
     * - ID mevcutsa UPDATE, yoksa INSERT yapar
     * - Burada ID mevcut olduğu için UPDATE yapar
     * 
     * Cache:
     * - Offer güncellendiğinde offerIds cache'i temizlenir
     */
    @Override
    @CacheEvict(cacheNames = "offerIds", allEntries = true)
    public OfferResponse updateOffer(Long id, CreateOfferRequest request) {
        log.info("Updating offer id={}, evicting offerIds cache", id);
        // Mevcut offer'ı getir
        Offer offer = offerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Offer not found: " + id));

        // Alanları güncelle
        offer.setName(request.getName());
        offer.setDescription(request.getDescription());
        offer.setPrice(request.getPrice());
        offer.setPeriod(request.getPeriod());
        offer.setUpdatedAt(Instant.now());  // Güncelleme zamanını kaydet

        // Güncellenmiş offer'ı kaydet
        Offer updatedOffer = offerRepository.save(offer);
        return mapToResponse(updatedOffer);
    }

    /**
     * Offer Silme
     * 
     * Dikkat:
     * - Hard delete yapar (kalıcı silme)
     * - Production'da soft delete tercih edilebilir (status = DELETED)
     * - Aktif abonelikleri olan offer'lar silinmeden önce kontrol edilmeli
     * 
     * Cache:
     * - Offer silindiğinde offerIds cache'i temizlenir
     */
    @Override
    @CacheEvict(cacheNames = "offerIds", allEntries = true)
    public void deleteOffer(Long id) {
        log.info("Deleting offer id={}, evicting offerIds cache", id);
        // Offer var mı kontrol et
        if (!offerRepository.existsById(id)) {
            throw new IllegalArgumentException("Offer not found: " + id);
        }
        // Sil
        offerRepository.deleteById(id);
    }

    /**
     * Tüm Offer ID'lerini Getir (Cache'li)
     * 
     * Redis Cache:
     * - Cache name: offerIds
     * - Key: 'all'
     * - İlk çağrıda database'den çeker ve Redis'e kaydeder
     * - Sonraki çağrılarda Redis'ten döner
     * - TTL: Configurable (varsayılan 6 saat)
     * 
     * Performans:
     * - Sadece ID'leri çeker, full entity değil
     * - Küçük veri boyutu ile hızlı cache hit
     * 
     * @return Tüm offer ID'lerinin seti
     */
    @Override
    @Cacheable(cacheNames = "offerIds", key = "'all'")
    public Set<Long> getAllOfferIds() {
        log.info("Fetching all offer IDs from database (cache miss)");
        Set<Long> offerIds = offerRepository.findAllOfferIds();
        log.info("Found {} offer IDs", offerIds.size());
        return offerIds;
    }

    /**
     * Entity to DTO Mapping Helper Method
     * 
     * Private Helper Method:
     * - Tekrar eden kodu azaltır (DRY principle)
     * - Mapping mantığını merkezi yönetir
     * - Değişiklik kolaylığı sağlar
     * 
     * @param offer Entity
     * @return Response DTO
     */
    private OfferResponse mapToResponse(Offer offer) {
        return new OfferResponse(
                offer.getId(),
                offer.getName(),
                offer.getDescription(),
                offer.getPrice(),
                offer.getPeriod(),
                offer.getStatus(),
                offer.getCreatedAt(),
                offer.getUpdatedAt()
        );
    }
}
