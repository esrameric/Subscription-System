package com.subscription.notification.repository;

import com.subscription.notification.model.Notification;
import com.subscription.notification.model.NotificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Notification Repository
 * 
 * Bildirim entity'leri için veritabanı işlemlerini yönetir
 * Spring Data JPA otomatik sorgu implementasyonu sağlar
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    /**
     * Belirli bir müşteriye ait tüm bildirimleri getirir
     * Kullanım: Müşteri bildirim geçmişi
     */
    List<Notification> findByCustomerId(Long customerId);
    
    /**
     * Belirli durumdaki tüm bildirimleri getirir
     * Kullanım: Başarısız bildirimleri tekrar gönderme, pending bildirimleri işleme
     */
    List<Notification> findByStatus(NotificationStatus status);
    
    /**
     * Belirli müşteriye ait belirli durumdaki bildirimleri getirir
     * Kullanım: Müşterinin başarısız bildirimlerini filtreleme
     */
    List<Notification> findByCustomerIdAndStatus(Long customerId, NotificationStatus status);
}
