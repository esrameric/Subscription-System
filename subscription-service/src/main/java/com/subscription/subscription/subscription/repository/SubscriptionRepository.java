package com.subscription.subscription.subscription.repository;

import com.subscription.subscription.subscription.model.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * SubscriptionRepository - Subscription Data Access Layer
 * 
 * JpaRepository ile otomatik CRUD operasyonları:
 * - save(), findById(), findAll(), deleteById(), count(), vb.
 * 
 * Custom Query Methods:
 * Spring Data JPA method naming convention kullanarak otomatik query oluşturur
 * 
 * Method Naming Convention:
 * - findBy: SELECT
 * - countBy: COUNT
 * - deleteBy: DELETE
 * - And/Or: Koşul birleştirme
 * - Before/After: Tarih karşılaştırma
 * 
 * Örnek:
 * findByCustomerIdAndStatus -> SELECT * FROM subscriptions WHERE customer_id = ? AND status = ?
 */
@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    
    /**
     * Müşteri ID ve Status'a Göre Abonelikleri Bulma
     * 
     * Query: SELECT * FROM subscriptions WHERE customer_id = ? AND status = ?
     * 
     * Kullanım Senaryosu:
     * - Müşterinin aktif aboneliklerini listeleme
     * - Dashboard'da "Aktif Aboneliklerim" bölümü
     * 
     * @param customerId Müşteri ID
     * @param status ACTIVE, DEACTIVE, SUSPEND
     * @return Filtrelenmiş subscription listesi
     * 
     * Örnek:
     * List<Subscription> activeSubs = repository.findByCustomerIdAndStatus(123L, "ACTIVE");
     */
    List<Subscription> findByCustomerIdAndStatus(Long customerId, String status);
    
    /**
     * Yenileme Tarihi Geçmiş Abonelikleri Bulma
     * 
     * Query: SELECT * FROM subscriptions 
     *        WHERE next_renewal_date < ? AND status = ?
     * 
     * Before: "Önce" anlamına gelir (tarih için < operatörü)
     * 
     * Kullanım Senaryosu:
     * - SubscriptionRenewalJob tarafından kullanılır
     * - Yenilenmesi gereken abonelikleri bulur
     * - Scheduled job her gün çalışır ve bu method ile abonelikleri getirir
     * 
     * @param date Kontrol tarihi (genellikle Instant.now())
     * @param status Genellikle "ACTIVE" (sadece aktif abonelikler yenilenir)
     * @return Yenilenmesi gereken subscription listesi
     * 
     * Örnek:
     * Bugün: 2026-01-27
     * nextRenewalDate: 2026-01-26 olan tüm ACTIVE abonelikleri döner
     * 
     * List<Subscription> toRenew = repository.findByNextRenewalDateBeforeAndStatus(
     *     Instant.now(), 
     *     "ACTIVE"
     * );
     */
    List<Subscription> findByNextRenewalDateBeforeAndStatus(Instant date, String status);
    
    /**
     * Müşteri ve Offer'a Göre Abonelik Bulma
     * 
     * Query: SELECT * FROM subscriptions WHERE customer_id = ? AND offer_id = ?
     * 
     * Optional<T>:
     * - Java 8+ ile gelen null-safe container
     * - Değer olabilir veya olmayabilir
     * - orElse(), orElseThrow(), isPresent(), ifPresent() methodları
     * 
     * Kullanım Senaryosu:
     * - Duplicate subscription kontrolü
     * - Aynı müşteri aynı offer'a birden fazla abone olamaz mı? (business rule)
     * - Eğer olamayacaksa, bu method ile kontrol edilir
     * 
     * @param customerId Müşteri ID
     * @param offerId Offer ID
     * @return Optional<Subscription> - Varsa subscription, yoksa empty
     * 
     * Örnek Kullanım:
     * Optional<Subscription> existing = repository.findByCustomerIdAndOfferId(123L, 5L);
     * if (existing.isPresent()) {
     *     throw new IllegalArgumentException("Zaten bu pakete abone oldunuz");
     * }
     * 
     * // veya
     * repository.findByCustomerIdAndOfferId(123L, 5L)
     *     .ifPresent(sub -> {
     *         throw new IllegalArgumentException("Duplicate subscription");
     *     });
     */
    Optional<Subscription> findByCustomerIdAndOfferId(Long customerId, Long offerId);
}
