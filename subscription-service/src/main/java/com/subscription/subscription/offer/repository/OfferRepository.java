package com.subscription.subscription.offer.repository;

import com.subscription.subscription.offer.model.Offer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * OfferRepository - Offer Entity için Data Access Layer
 * 
 * Repository Nedir?
 * - Database işlemleri için abstraction layer
 * - CRUD operasyonlarını yönetir
 * - JPA Query methodlarını içerir
 * 
 * JpaRepository Interface'i:
 * - Spring Data JPA tarafından sağlanır
 * - Generic tip: JpaRepository<Entity, ID_Type>
 * - Otomatik olarak şu methodları sağlar:
 *   * save(entity): Kaydet veya güncelle
 *   * findById(id): ID ile bul
 *   * findAll(): Tüm kayıtları getir
 *   * deleteById(id): ID ile sil
 *   * count(): Toplam kayıt sayısı
 * 
 * Custom Query Methods:
 * - Method isim kurallarına göre otomatik SQL query oluşturur
 * - Örnek: findByStatus(String status) -> SELECT * FROM offers WHERE status = ?
 * - Spring Data JPA method naming convention kullanır:
 *   * findBy: SELECT sorgusu
 *   * countBy: COUNT sorgusu
 *   * deleteBy: DELETE sorgusu
 *   * And/Or: Koşul birleştirme
 * 
 * @Repository: Bu interface'in bir Spring Bean olduğunu belirtir
 * - Exception translation yapar (SQLException -> DataAccessException)
 * - Component scanning ile otomatik bulunur
 */
@Repository
public interface OfferRepository extends JpaRepository<Offer, Long> {
    
    /**
     * Duruma göre offer'ları getirir
     * 
     * Method Naming Convention:
     * - findBy: SELECT sorgusu başlatır
     * - Status: Entity'deki "status" field'ına karşılık gelir
     * 
     * Otomatik oluşturulan SQL:
     * SELECT * FROM offers WHERE status = ?
     * 
     * @param status ACTIVE veya INACTIVE
     * @return Status'a göre filtrelenmiş offer listesi
     * 
     * Örnek Kullanım:
     * List<Offer> activeOffers = offerRepository.findByStatus("ACTIVE");
     */
    List<Offer> findByStatus(String status);
}
