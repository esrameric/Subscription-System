package com.subscription.subscription.subscription.service.impl;

import com.subscription.subscription.client.PaymentServiceClient;
import com.subscription.subscription.dto.PaymentRequest;
import com.subscription.subscription.dto.PaymentResponse;
import com.subscription.subscription.offer.model.Offer;
import com.subscription.subscription.offer.repository.OfferRepository;
import com.subscription.subscription.subscription.dto.CreateSubscriptionRequest;
import com.subscription.subscription.subscription.dto.UpdateSubscriptionRequest;
import com.subscription.subscription.subscription.dto.SubscriptionResponse;
import com.subscription.subscription.subscription.model.Subscription;
import com.subscription.subscription.subscription.repository.SubscriptionRepository;
import com.subscription.subscription.subscription.service.SubscriptionService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * SubscriptionServiceImpl - SubscriptionService Interface Implementasyonu
 * 
 * @Service: Spring Service Bean
 * - Component scanning ile otomatik bulunur
 * - Business logic katmanı
 * - @Component'in özelleşmiş hali
 * 
 * @Transactional:
 * - Transaction yönetimi (method level'da eklenir)
 * - ACID özellikleri garantiler
 * - Hata durumunda rollback yapar
 * - Database consistency sağlar
 * 
 * Transaction Örneği:
 * Method içinde 3 database operasyonu var, biri başarısız olursa
 * tümü geri alınır (all or nothing)
 * 
 * Dependencies:
 * - SubscriptionRepository: Subscription CRUD
 * - OfferRepository: Offer bilgileri (period için gerekli)
 * - PaymentServiceClient: Payment Service ile iletişim
 */
@Service
@Slf4j
public class SubscriptionServiceImpl implements SubscriptionService {

    /**
     * Dependency Injection - Constructor Based
     * 
     * Best Practice: Constructor injection
     * - Field injection yerine tercih edilir
     * - Immutability (final)
     * - Test edilebilirlik
     * - Explicit dependencies
     */
    private final SubscriptionRepository subscriptionRepository;
    private final OfferRepository offerRepository;
    private final PaymentServiceClient paymentServiceClient;

    /**
     * Constructor
     * 
     * Spring Boot 4.3+: @Autowired gerekmez
     * Spring otomatik olarak dependencies'i inject eder
     */
    public SubscriptionServiceImpl(
            SubscriptionRepository subscriptionRepository, 
            OfferRepository offerRepository,
            PaymentServiceClient paymentServiceClient) {
        this.subscriptionRepository = subscriptionRepository;
        this.offerRepository = offerRepository;
        this.paymentServiceClient = paymentServiceClient;
    }

    /**
     * Yeni Abonelik Oluşturma
     * 
     * @Transactional: Tüm işlem atomic (hepsi ya da hiçbiri)
     * 
     * İşlem Adımları:
     * 1. Offer validation: Offer var mı, aktif mi?
     * 2. Duplicate check: Aynı müşteri aynı offer'a zaten abone mi?
     * 3. Renewal date calculation: Şimdi + offer periyodu
     * 4. Subscription create & save
     * 5. Response mapping
     * 
     * ChronoUnit:
     * - Java 8+ Date/Time API
     * - Tarih aritmetiği için kullanılır
     * - DAYS, HOURS, MINUTES, SECONDS, vb.
     * 
     * Örnek:
     * Bugün: 2026-01-27
     * Offer period: 1 ay
     * nextRenewalDate: Instant.now().plus(1, ChronoUnit.MONTHS) = 2026-02-27
     */
    @Override
    @Transactional
    public SubscriptionResponse createSubscription(Long customerId, CreateSubscriptionRequest request) {
        // 1. Offer'ı getir ve kontrol et
        Offer offer = offerRepository.findById(request.getOfferId())
                .orElseThrow(() -> new IllegalArgumentException(
                    "Offer not found: " + request.getOfferId()));

        // 2. Duplicate subscription kontrolü
        // Aynı müşterinin aynı offer'a birden fazla aboneliği olmamalı
        if (subscriptionRepository.findByCustomerIdAndOfferId(customerId, request.getOfferId()).isPresent()) {
            throw new IllegalArgumentException(
                "Customer already has an active subscription for this offer");
        }

        // 3. Yenileme tarihini hesapla
        // Instant.plus(MONTHS) desteklenmiyor, ZonedDateTime kullanıyoruz
        // offer.getPeriod() ay cinsinden periyod döndürür
        Instant nextRenewalDate = ZonedDateTime.now(ZoneOffset.UTC)
                .plusMonths(offer.getPeriod())
                .toInstant();
        
        // 4. Yeni subscription entity oluştur
        Subscription subscription = Subscription.builder()
                .customerId(customerId)
                .offerId(request.getOfferId())
                .nextRenewalDate(nextRenewalDate)
                .status("ACTIVE")  // Yeni abonelik her zaman ACTIVE başlar
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        // 5. Database'e kaydet
        Subscription savedSubscription = subscriptionRepository.save(subscription);
        
        // 6. Entity'yi Response DTO'ya dönüştür
        return mapToResponse(savedSubscription);
    }

    /**
     * ID ile Subscription Getirme
     * 
     * Optional.orElseThrow():
     * - Değer varsa döndür
     * - Yoksa exception fırlat
     * - Null kontrolü yerine modern yaklaşım
     */
    @Override
    public SubscriptionResponse getSubscriptionById(Long id) {
        Subscription subscription = subscriptionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                    "Subscription not found: " + id));
        return mapToResponse(subscription);
    }

    /**
     * Müşterinin Aktif Aboneliklerini Listeleme
     * 
     * Stream API:
     * - Functional programming yaklaşımı
     * - map(): Transformation (Entity -> DTO)
     * - collect(): Terminal operation (Stream -> List)
     * 
     * Method Reference:
     * - this::mapToResponse
     * - Kısa syntax: lambda yerine method referansı
     * - Eşdeğeri: .map(s -> mapToResponse(s))
     */
    @Override
    public List<SubscriptionResponse> getCustomerSubscriptions(Long customerId) {
        return subscriptionRepository.findByCustomerIdAndStatus(customerId, "ACTIVE")
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Subscription Status Güncelleme
     * 
     * @Transactional: Database consistency için
     * 
     * İşlem Adımları:
     * 1. Subscription'ı getir
     * 2. Status'u güncelle
     * 3. updatedAt timestamp'ini güncelle
     * 4. Kaydet
     * 
     * Not: save() methodunu çağırmak zorunda değiliz
     * JPA'nın dirty checking özelliği sayesinde transaction sonunda
     * değişiklikler otomatik kaydedilir
     * Ama explicit save() daha okunabilir ve güvenli
     */
    @Override
    @Transactional
    public SubscriptionResponse updateSubscription(Long id, UpdateSubscriptionRequest request) {
        // Subscription'ı getir
        Subscription subscription = subscriptionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                    "Subscription not found: " + id));

        // Status'u güncelle
        subscription.setStatus(request.getStatus());
        
        // Güncelleme zamanını kaydet
        subscription.setUpdatedAt(Instant.now());

        // Database'e kaydet
        Subscription updatedSubscription = subscriptionRepository.save(subscription);
        return mapToResponse(updatedSubscription);
    }

    /**
     * Subscription Yenileme (Renewal)
     * 
     * Bu method:
     * - SubscriptionRenewalJob tarafından çağrılır (otomatik)
     * - Manuel de çağrılabilir (admin operasyonu)
     * 
     * İşlem Adımları:
     * 1. Subscription'ı getir
     * 2. İlgili offer'ı getir (periodDays için)
     * 3. nextRenewalDate'i güncelle (şimdi + periodDays)
     * 4. updatedAt'i güncelle
     * 5. Kaydet
     * 
     * İleride:
     * - Payment processing eklenebilir
     * - Başarısız ödeme durumunda status SUSPEND'e çekilebilir
     * - Notification gönderilir (email, SMS)
     */
    @Override
    @Transactional
    public void renewSubscription(Long id) {
        // Subscription'ı getir
        Subscription subscription = subscriptionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                    "Subscription not found: " + id));

        // Offer'ı getir
        Offer offer = offerRepository.findById(subscription.getOfferId())
                .orElseThrow(() -> new IllegalArgumentException(
                    "Offer not found: " + subscription.getOfferId()));

        // Yenileme tarihini güncelle
        // Yeni tarih = Şimdi + offer period (ay cinsinden)
        subscription.setNextRenewalDate(
            Instant.now().plus(offer.getPeriod(), ChronoUnit.MONTHS)
        );
        
        // Status'u ACTIVE yap (SUSPEND'den geri dönebilir)
        subscription.setStatus("ACTIVE");
        
        // Güncelleme zamanını kaydet
        subscription.setUpdatedAt(Instant.now());

        // Database'e kaydet
        subscriptionRepository.save(subscription);
    }

    /**
     * Subscription'ı Askıya Alma
     * 
     * Ödeme başarısız olduğunda kullanılır
     */
    @Override
    @Transactional
    public void suspendSubscription(Long id) {
        // Subscription'ı getir
        Subscription subscription = subscriptionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                    "Subscription not found: " + id));

        // Status'u SUSPEND yap
        subscription.setStatus("SUSPEND");
        
        // Güncelleme zamanını kaydet
        subscription.setUpdatedAt(Instant.now());

        // Database'e kaydet
        subscriptionRepository.save(subscription);
    }

    /**
     * Subscription Renewal Process - Payment ile birlikte
     * 
     * İşlem Adımları:
     * 1. Subscription'ı getir ve doğrula
     * 2. Offer bilgilerini getir (amount için)
     * 3. Payment Service'e ödeme isteği gönder
     * 4. Ödeme başarılıysa: Kafka event ile otomatik yenilenir
     * 5. Ödeme başarısızsa: Subscription SUSPEND durumuna geçer
     */
    @Override
    @Transactional
    public Object processSubscriptionRenewal(Long id) {
        // Artık bu metodda doğrudan payment-service'e REST çağrısı yapılmaz.
        // Sadece event-driven akış kullanılacak şekilde güncellendi.
        throw new UnsupportedOperationException("processSubscriptionRenewal artık event-driven akış ile yönetiliyor. Lütfen SubscriptionRenewalJob'u kullanın.");
    }

    /**
     * Yenilenmesi Gereken Abonelikleri Getirme
     * 
     * Kullanım: SubscriptionRenewalJob
     * 
     * Query:
     * - nextRenewalDate < now: Yenileme tarihi geçmiş
     * - status = ACTIVE: Sadece aktif abonelikler
     * 
     * Örnek:
     * Bugün: 2026-01-27 10:00
     * nextRenewalDate: 2026-01-26 olan tüm ACTIVE subscriptions
     */
    @Override
    public List<SubscriptionResponse> getSubscriptionsToRenew() {
        return subscriptionRepository
                .findByNextRenewalDateBeforeAndStatus(Instant.now(), "ACTIVE")
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Entity to DTO Mapping Helper
     * 
     * Private helper method:
     * - Code reusability (DRY - Don't Repeat Yourself)
     * - Single point of mapping logic
     * - Easy to maintain
     * 
     * Gelişmiş Versiyon:
     * - MapStruct kullanılabilir (auto-mapping)
     * - ModelMapper kullanılabilir
     * 
     * @param subscription Entity
     * @return Response DTO
     */
    private SubscriptionResponse mapToResponse(Subscription subscription) {
        return new SubscriptionResponse(
                subscription.getId(),
                subscription.getCustomerId(),
                subscription.getOfferId(),
                subscription.getNextRenewalDate(),
                subscription.getStatus(),
                subscription.getCreatedAt(),
                subscription.getUpdatedAt()
        );
    }
}
