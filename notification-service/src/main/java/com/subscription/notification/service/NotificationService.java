package com.subscription.notification.service;

import com.subscription.notification.model.*;
import com.subscription.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * Notification Service
 * 
 * Bildirim gönderme ve yönetim işlemlerini yapar:
 * - Email gönderimi (SMTP üzerinden)
 * - SMS gönderimi (placeholder - Twilio, AWS SNS vb. entegre edilecek)
 * - Push notification (placeholder - FCM, APNS vb. entegre edilecek)
 * - Bildirim kaydı ve durum takibi
 * - Hata yönetimi ve retry mekanizması
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final JavaMailSender mailSender;

    /**
     * Bildirimi belirtilen kanal üzerinden gönderir
     * 
     * İşlem Akışı:
     * 1. Bildirim kanalına göre (EMAIL/SMS/PUSH) ilgili metodu çağır
     * 2. Başarılı ise status'u SENT yap ve gönderim zamanını kaydet
     * 3. Hata oluşursa status'u FAILED yap ve hata mesajını kaydet
     * 4. Retry count'u artır
     * 5. Her durumda veritabanına kaydet
     * 
     * @param notification Gönderilecek bildirim (recipient, subject, content içerir)
     */
    public void sendNotification(Notification notification) {
        try {
            log.info("Sending notification: {} to {}", notification.getType(), notification.getRecipient());
            
            // Kanal tipine göre ilgili gönderim metodunu çağır
            switch (notification.getChannel()) {
                case EMAIL -> sendEmail(notification);
                case SMS -> sendSms(notification);
                case PUSH -> sendPushNotification(notification);
            }
            
            // Başarılı gönderim - durumu güncelle
            notification.setStatus(NotificationStatus.SENT);
            notification.setSentAt(Instant.now());
            log.info("Notification sent successfully: {}", notification.getId());
            
        } catch (Exception e) {
            // Hata durumu - hata bilgilerini kaydet
            log.error("Failed to send notification: {}", notification.getId(), e);
            notification.setStatus(NotificationStatus.FAILED);
            notification.setErrorMessage(e.getMessage());
            notification.setRetryCount(notification.getRetryCount() + 1);
        } finally {
            // Her durumda güncel durumu veritabanına kaydet
            notificationRepository.save(notification);
        }
    }

    /**
     * Email gönderir (SMTP üzerinden Gmail)
     * 
     * SimpleMailMessage kullanarak temel email gönderimi yapar
     * Yapılandırma application.yml'de tanımlıdır
     * 
     * @param notification Gönderilecek bildirim
     */
    private void sendEmail(Notification notification) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(notification.getRecipient());  // Alıcı email
        message.setSubject(notification.getSubject());  // Email konusu
        message.setText(notification.getContent());  // Email içeriği
        
        mailSender.send(message);  // SMTP üzerinden gönder
        log.info("Email sent to: {}", notification.getRecipient());
    }

    /**
     * SMS gönderir
     * 
     * TODO: Üretimde SMS provider entegrasyonu yapılacak
     * Örnek: Twilio, AWS SNS, Vonage vb.
     * 
     * @param notification Gönderilecek bildirim
     */
    private void sendSms(Notification notification) {
        log.info("SMS sending not implemented yet. Would send to: {}", notification.getRecipient());
    }

    /**
     * Push notification gönderir
     * 
     * TODO: Üretimde push notification provider entegrasyonu yapılacak
     * Örnek: Firebase Cloud Messaging (FCM), Apple Push Notification Service (APNS)
     * 
     * @param notification Gönderilecek bildirim
     */
    private void sendPushNotification(Notification notification) {
        log.info("Push notification sending not implemented yet. Would send to: {}", notification.getRecipient());
    }

    /**
     * Yeni bir bildirim oluşturur ve veritabanına kaydeder
     * 
     * Başlangıç durumu PENDING olarak ayarlanır
     * Retry count 0'dan başlar
     * 
     * @param customerId Müşteri ID
     * @param type Bildirim tipi (PAYMENT_SUCCESS, PAYMENT_FAILED vb.)
     * @param channel Gönderim kanalı (EMAIL, SMS, PUSH)
     * @param recipient Alıcı bilgisi (email adresi veya telefon numarası)
     * @param subject Bildirim konusu
     * @param content Bildirim içeriği
     * @param relatedEntityId İlgili entity ID (örn: paymentId, subscriptionId)
     * @return Oluşturulan ve kaydedilen bildirim
     */
    public Notification createNotification(
            Long customerId,
            NotificationType type,
            NotificationChannel channel,
            String recipient,
            String subject,
            String content,
            Long relatedEntityId
    ) {
        Notification notification = Notification.builder()
                .customerId(customerId)
                .type(type)
                .channel(channel)
                .recipient(recipient)
                .subject(subject)
                .content(content)
                .status(NotificationStatus.PENDING)  // Başlangıçta beklemede
                .relatedEntityId(relatedEntityId)  // Payment ID, Subscription ID vb.
                .retryCount(0)  // İlk deneme
                .build();
        
        return notificationRepository.save(notification);
    }
}
