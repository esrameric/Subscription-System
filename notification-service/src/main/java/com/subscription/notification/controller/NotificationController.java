package com.subscription.notification.controller;

import com.subscription.notification.model.Notification;
import com.subscription.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Notification REST API Controller
 * 
 * Bildirim geçmişini sorgulama endpoint'leri:
 * - Müşteriye gönderilen tüm bildirimleri listele
 * - Belirli bir bildirimi getir
 * - Tüm bildirimleri listele (admin için)
 * 
 * Base URL: /api/notifications
 */
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationRepository notificationRepository;

    /**
     * Belirli bir müşteriye gönderilen tüm bildirimleri getirir
     * 
     * Kullanım: Müşteri kendi bildirim geçmişini görüntüleyebilir
     * 
     * @param customerId Müşteri ID
     * @return Müşteriye ait tüm bildirimler
     * 
     * Örnek: GET /api/notifications/customer/123
     */
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<Notification>> getCustomerNotifications(@PathVariable Long customerId) {
        return ResponseEntity.ok(notificationRepository.findByCustomerId(customerId));
    }

    /**
     * Belirli bir bildirimin detayını getirir
     * 
     * @param id Bildirim ID
     * @return Bildirim detayı (varsa 200, yoksa 404)
     * 
     * Örnek: GET /api/notifications/456
     */
    @GetMapping("/{id}")
    public ResponseEntity<Notification> getNotification(@PathVariable Long id) {
        return notificationRepository.findById(id)
                .map(ResponseEntity::ok)  // Bulundu - 200 OK
                .orElse(ResponseEntity.notFound().build());  // Bulunamadı - 404 Not Found
    }

    /**
     * Tüm bildirimleri getirir
     * 
     * Admin amaçlı - sistemdeki tüm bildirim geçmişi
     * Üretimde pagination ve filtreleme eklenmeli
     * 
     * @return Tüm bildirimler
     * 
     * Örnek: GET /api/notifications
     */
    @GetMapping
    public ResponseEntity<List<Notification>> getAllNotifications() {
        return ResponseEntity.ok(notificationRepository.findAll());
    }
}
